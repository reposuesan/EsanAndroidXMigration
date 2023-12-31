package pe.edu.esan.appostgrado.view.horario

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.activity_tomar_asistencia.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.AsistenciaAdapter
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class TomarAsistenciaActivity : AppCompatActivity() {

    private val TAG = "TomarAsistenciaActivity"
    private var requestQueue : RequestQueue? = null
    private var requestQueueRegAsis : RequestQueue? = null

    private var listaAlumnos = ArrayList<Alumno>()
    private var adapterAsistencia : AsistenciaAdapter? = null

    private var asistencias = 0
    private var tardanzas = 0
    private var faltas = 0

    private var llenarMasivoAsistencia = false

    private val LOG = TomarAsistenciaActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tomar_asistencia)

        val toolbar = main_tomarasistencia as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.tomar_asistencia)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        sgMarca_tomarasistencia.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.ck_asisa -> marcarTodos("A")
                R.id.ck_asist -> marcarTodos("T")
                else -> marcarTodos("F")
            }
        }
        rvAlumno_tomarasistencia.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)

        getEstadoAsistenciaSeccion(false)

    }

    private fun getEstadoAsistenciaSeccion (laanterior: Boolean) {

        val horario = ControlUsuario.instance.currentHorario
        if (horario != null) {

            var idSesion = "0"
            if (!laanterior) {
                idSesion = horario.idSesion.toString()
            } else {
                val sesionActual = horario.idSesion
                if (sesionActual >= 2) {
                    idSesion = (sesionActual - 1).toString()
                } else {
                    adapterAsistencia = AsistenciaAdapter(listaAlumnos)
                    rvAlumno_tomarasistencia.adapter = adapterAsistencia
                }
            }

            val request = JSONObject()
            request.put("CodSeccion", horario.seccionCodigo)
            request.put("IdHorario", horario.idHorario)
            request.put("IdSesion", idSesion)

            val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)

            onEstadoAsistenciaSeccion(Utilitarios.getUrl(Utilitarios.URL.LISTA_ASISTENCIA), requestEncriptado, laanterior)
        } else {
            finish()
        }
    }

    private fun onEstadoAsistenciaSeccion(url: String, request: JSONObject?, laanterior: Boolean) {
        prbCargando_tomarasistencia.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                url,
                request,
            { response ->
                try {
                    var consultarAnterior = false
                    if (!response.isNull("ListarAsistenciaAlumnosxSeccionResult")) {

                        val listaAlumnoJArray = Utilitarios.jsArrayDesencriptar(response["ListarAsistenciaAlumnosxSeccionResult"] as String, this)
                        if (listaAlumnoJArray!!.length() > 0) {
                            listaAlumnos = ArrayList()

                            for (z in 0 until listaAlumnoJArray.length()) {
                                val alumnoJObject = listaAlumnoJArray[z] as JSONObject

                                val codigo = alumnoJObject["CodAlumno"] as String
                                val email = alumnoJObject["Email"] as String
                                val estadonombre = (alumnoJObject["EstadoNombre"] as String).trim()
                                val nombrecompleto = alumnoJObject["Alumno"] as String
                                val idactor = alumnoJObject["IdAlumno"] as Int
                                val estadoAsistencia = (alumnoJObject["Asistencia"] as String).trim()
                                val cantiFaltas = alumnoJObject["CantFalta"] as Int
                                val procInhabilitado = alumnoJObject["PorcxInasistencia"] as Int
                                val totalSesiones = alumnoJObject["TotalSesiones"] as Int
                                val procActual = cantiFaltas.toFloat() * 100 / totalSesiones.toFloat()

                                consultarAnterior = estadoAsistencia.isEmpty()
                                llenarMasivoAsistencia = estadoAsistencia.isEmpty()

                                val alumno = Alumno(codigo, idactor, nombrecompleto, email, estadonombre, procActual, procInhabilitado)
                                alumno.estadoAsistencia = estadoAsistencia
                                listaAlumnos.add(alumno)

                            }
                            if (laanterior) {
                                for (alumno in listaAlumnos) {
                                    alumno.actualizoEstadoAsistencia = true
                                }
                                adapterAsistencia = AsistenciaAdapter(listaAlumnos)
                                rvAlumno_tomarasistencia.adapter = adapterAsistencia
                            } else {
                                if (!consultarAnterior) {
                                    adapterAsistencia = AsistenciaAdapter(listaAlumnos)
                                    rvAlumno_tomarasistencia.adapter = adapterAsistencia
                                } else {
                                    getEstadoAsistenciaSeccion(true)
                                }
                            }
                            rvAlumno_tomarasistencia.visibility = View.VISIBLE
                            prbCargando_tomarasistencia.visibility = View.GONE
                            lblSMarcaTodo_tomarasistencia.visibility = View.VISIBLE
                            sgMarca_tomarasistencia.visibility = View.VISIBLE
                            asistencia_empty_text_view.visibility = View.GONE
                        } else {
                            rvAlumno_tomarasistencia.visibility = View.GONE
                            prbCargando_tomarasistencia.visibility = View.GONE
                            lblSMarcaTodo_tomarasistencia.visibility = View.GONE
                            sgMarca_tomarasistencia.visibility = View.GONE
                            asistencia_empty_text_view.visibility = View.VISIBLE
                            asistencia_empty_text_view.text = getString(R.string.advertencia_no_informacion)
                        }
                    } else {
                        rvAlumno_tomarasistencia.visibility = View.GONE
                        prbCargando_tomarasistencia.visibility = View.GONE
                        lblSMarcaTodo_tomarasistencia.visibility = View.GONE
                        sgMarca_tomarasistencia.visibility = View.GONE
                        asistencia_empty_text_view.visibility = View.VISIBLE
                        asistencia_empty_text_view.text = getString(R.string.error_intentelo_mas_tarde)
                    }
                } catch (jex: JSONException) {
                    rvAlumno_tomarasistencia.visibility = View.GONE
                    prbCargando_tomarasistencia.visibility = View.GONE
                    lblSMarcaTodo_tomarasistencia.visibility = View.GONE
                    sgMarca_tomarasistencia.visibility = View.GONE
                    asistencia_empty_text_view.visibility = View.VISIBLE
                    asistencia_empty_text_view.text = getString(R.string.error_respuesta_server)
                }
            },
            { error ->
                when {
                    error is TimeoutError || error.networkResponse == null -> {
                        rvAlumno_tomarasistencia.visibility = View.GONE
                        prbCargando_tomarasistencia.visibility = View.GONE
                        lblSMarcaTodo_tomarasistencia.visibility = View.GONE
                        sgMarca_tomarasistencia.visibility = View.GONE
                        asistencia_empty_text_view.visibility = View.VISIBLE
                        asistencia_empty_text_view.text = getString(R.string.error_respuesta_server)
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onEstadoAsistenciaSeccion(url, request, laanterior)
                            } else {
                                rvAlumno_tomarasistencia.visibility = View.GONE
                                prbCargando_tomarasistencia.visibility = View.GONE
                                lblSMarcaTodo_tomarasistencia.visibility = View.GONE
                                sgMarca_tomarasistencia.visibility = View.GONE
                                asistencia_empty_text_view.visibility = View.VISIBLE
                                asistencia_empty_text_view.text = getString(R.string.error_respuesta_server)
                            }
                        }
                    }
                    else -> {
                        rvAlumno_tomarasistencia.visibility = View.GONE
                        prbCargando_tomarasistencia.visibility = View.GONE
                        lblSMarcaTodo_tomarasistencia.visibility = View.GONE
                        sgMarca_tomarasistencia.visibility = View.GONE
                        asistencia_empty_text_view.visibility = View.VISIBLE
                        asistencia_empty_text_view.text = getString(R.string.error_respuesta_server)
                    }
                }

            }
        )
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        {
            override fun getHeaders(): MutableMap<String, String> {
                return getHeaderForJWT()
            }
        }
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }

    private fun marcarTodos(estado: String) {
        if (listaAlumnos.isNotEmpty()) {
            for (alumno in listaAlumnos) {
                alumno.estadoAsistencia = estado
                alumno.actualizoEstadoAsistencia = true
            }

            adapterAsistencia = AsistenciaAdapter(listaAlumnos)
            rvAlumno_tomarasistencia.adapter = adapterAsistencia
        }
    }

    private fun showBackButton() {
        val alertReturn = AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.advertencia))
                .setMessage(resources.getString(R.string.mensaje_salir_sin_enviar_asistencia))
                .setPositiveButton(resources.getString(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                    ControlUsuario.instance.cambioPantalla = false
                    ControlUsuario.instance.pantallaSuspendida = false
                    finish()
                })
                .setNegativeButton(resources.getString(R.string.cancelar), null)
                .create()
        alertReturn.show()
        alertReturn.getButton(android.app.Dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.esan_rojo))
        alertReturn.findViewById<TextView>(android.R.id.message)?.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
    }

    private fun onAsistencia(re: JSONObject, request: JSONObject, url: String) {

        CustomDialog.instance.showDialogLoad(this)
        requestQueueRegAsis = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->
                CustomDialog.instance.dialogoCargando?.dismiss()

                val respuesta = if (llenarMasivoAsistencia)
                    Utilitarios.stringDesencriptar(response["RegistrarAsistenciaMasivaAlumnoResult"] as String, this)
                else
                    Utilitarios.stringDesencriptar(response["RegistrarAsistenciaAlumnoResult"] as String, this)

                if (respuesta != null) {
                    if (respuesta == "true") {
                        if (llenarMasivoAsistencia) {
                            ControlUsuario.instance.tomoAsistenciaMasica = true
                            ControlUsuario.instance.recargaHorarioProfesor = true
                            ControlUsuario.instance.cambioPantalla = false
                            ControlUsuario.instance.pantallaSuspendida = false
                            finish()
                        } else {
                            if (ControlUsuario.instance.copiarListHorario.size > 0) {
                                copiarAsistenciaSesionesSuperiores(0, re)
                            } else {
                                ControlUsuario.instance.recargaHorarioProfesor = true
                                ControlUsuario.instance.cambioPantalla = false
                                ControlUsuario.instance.pantallaSuspendida = false
                                finish()
                            }
                        }
                    } else {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_marco_asistencia_alumnos), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                        snack.show()
                    }
                } else {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_desencriptar), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }

            },
            { error ->
                when {
                    error is TimeoutError || error.networkResponse == null -> {
                        onAsistencia(re, request, url)
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onAsistencia(re, request, url)
                            } else {
                                CustomDialog.instance.dialogoCargando?.dismiss()

                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        }
                    }
                    else -> {
                        CustomDialog.instance.dialogoCargando?.dismiss()

                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                }

            }
        )
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        {
            override fun getHeaders(): MutableMap<String, String> {
                return getHeaderForJWT()
            }
        }
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueueRegAsis?.add(jsObjectRequest)
    }

    private fun copiarAsistenciaSesionesSuperiores(id: Int, request: JSONObject) {
        CustomDialog.instance.showDialogLoad(this)

        if (id < ControlUsuario.instance.copiarListHorario.size) {
            val horario = ControlUsuario.instance.copiarListHorario[id]
            request.put("IdSesion", horario.idSesion)
            request.put("IdHorario", horario.idHorario)

            val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
            if (requestEncriptado != null) {
                copiarAsistencias(requestEncriptado, id, request)
            } else {
                CustomDialog.instance.dialogoCargando?.dismiss()

                ControlUsuario.instance.recargaHorarioProfesor = true
                ControlUsuario.instance.cambioPantalla = false
                ControlUsuario.instance.pantallaSuspendida = false
                finish()
            }
        } else {
            CustomDialog.instance.dialogoCargando?.dismiss()

            ControlUsuario.instance.recargaHorarioProfesor = true
            ControlUsuario.instance.cambioPantalla = false
            ControlUsuario.instance.pantallaSuspendida = false
            finish()
        }

    }

    private fun copiarAsistencias(requestEncriptado: JSONObject, id: Int, request: JSONObject) {
        val url = Utilitarios.getUrl(Utilitarios.URL.REGISTRAR_ASISTENCIA_ALUMNO)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
            url,
            requestEncriptado,
            { response ->
                CustomDialog.instance.dialogoCargando?.dismiss()

                val respuesta = Utilitarios.stringDesencriptar(response["RegistrarAsistenciaAlumnoResult"] as String, this)

                if (respuesta != null) {
                    if (respuesta == "true") {
                        copiarAsistenciaSesionesSuperiores(id + 1, request)
                    }
                }

                ControlUsuario.instance.recargaHorarioProfesor = true
                ControlUsuario.instance.cambioPantalla = false
                ControlUsuario.instance.pantallaSuspendida = false
                finish()

            },
            { error ->
                when {
                    error is TimeoutError || error.networkResponse == null -> {
                        copiarAsistencias(requestEncriptado, id, request)
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                copiarAsistencias(requestEncriptado, id, request)
                            } else {
                                CustomDialog.instance.dialogoCargando?.dismiss()

                                ControlUsuario.instance.recargaHorarioProfesor = true
                                ControlUsuario.instance.cambioPantalla = false
                                ControlUsuario.instance.pantallaSuspendida = false
                                finish()
                            }
                        }
                    }
                    else -> {
                        CustomDialog.instance.dialogoCargando?.dismiss()

                        ControlUsuario.instance.recargaHorarioProfesor = true
                        ControlUsuario.instance.cambioPantalla = false
                        ControlUsuario.instance.pantallaSuspendida = false
                        finish()
                    }
                }

            }
        )
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        {
            override fun getHeaders(): MutableMap<String, String> {
                return getHeaderForJWT()
            }
        }
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueueRegAsis?.add(jsObjectRequest)
    }

    private fun setAsistencia() {
        val valores = validarAsistenciaCompleta()

        if (valores.puedeenviar) {
            val asistenciaAlumnos = valores.asistencias
            if (asistenciaAlumnos != null) {
                val alertConfirmar = AlertDialog.Builder(this)
                        .setTitle(resources.getString(R.string.confirmar))
                        .setMessage(String.format(resources.getString(R.string.mensaje_confirmar_asistencia),
                                ControlUsuario.instance.currentHorario?.curso,
                                ControlUsuario.instance.currentHorario?.section,
                                asistencias, tardanzas, faltas))
                        .setPositiveButton(resources.getString(R.string.enviar), DialogInterface.OnClickListener { dialogInterface, i ->
                            if (asistenciaAlumnos.length() > 0) {
                                val requestEncriptado = Utilitarios.jsObjectEncrypted(asistenciaAlumnos, this)
                                if (requestEncriptado != null) {
                                    onAsistencia(asistenciaAlumnos, requestEncriptado, if (llenarMasivoAsistencia) Utilitarios.getUrl(Utilitarios.URL.REGISTRAR_ASISTENCIA_MASIVA) else Utilitarios.getUrl(Utilitarios.URL.REGISTRAR_ASISTENCIA_ALUMNO))
                                }
                            } else {
                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_asistencia), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        })
                        .setNegativeButton(resources.getString(R.string.cancelar), null)
                        .create()

                alertConfirmar.show()
                alertConfirmar.getButton(android.app.Dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.esan_rojo))
                alertConfirmar.findViewById<TextView>(android.R.id.message)?.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            }
        } else {
            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.advertencia_completar_asistencia), Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
            snack.show()
        }

    }

    private fun validarAsistenciaCompleta() : EstadoAsistencia {
        val respuesta = EstadoAsistencia(false, null)

        val asisnteciaAlumnoJs = JSONObject()

        asistencias = 0
        tardanzas = 0
        faltas = 0

        var Asistencia = ""
        var Tardanza = ""
        var Falta = ""

        val listaAlumno = adapterAsistencia?.listaAlumno

        if (listaAlumno != null) {
            if (listaAlumno.isNotEmpty()) {
                for (alumno in listaAlumno) {
                    if (alumno.estadoAsistencia.isEmpty() && alumno.estado == "A") {
                        return respuesta
                    } else {

                        when (alumno.estadoAsistencia) {
                            "A" -> {
                                asistencias += 1
                                if (alumno.actualizoEstadoAsistencia)
                                    Asistencia += alumno.codigo + "-"
                            }
                            "T" -> {
                                tardanzas += 1
                                if (alumno.actualizoEstadoAsistencia)
                                    Tardanza += alumno.codigo + "-"
                            }
                            "F" -> {
                                faltas += 1
                                if (alumno.actualizoEstadoAsistencia)
                                    Falta += alumno.codigo + "-"
                            }
                        }
                    }
                }

                asisnteciaAlumnoJs.put("CodSeccion", ControlUsuario.instance.currentHorario?.seccionCodigo)
                asisnteciaAlumnoJs.put("IdSesion", ControlUsuario.instance.currentHorario?.idSesion)
                asisnteciaAlumnoJs.put("IdHorario", ControlUsuario.instance.currentHorario?.idHorario)
                asisnteciaAlumnoJs.put("ListaAlumnosAsistio", eliminarUltimoCaracter(Asistencia))
                asisnteciaAlumnoJs.put("ListaAlumnosTarde", eliminarUltimoCaracter(Tardanza))
                asisnteciaAlumnoJs.put("ListaAlumnosFalta", eliminarUltimoCaracter(Falta))

                respuesta.puedeenviar = true
                respuesta.asistencias = asisnteciaAlumnoJs
                return respuesta
            }
        }
        return respuesta
    }

    override fun onDestroy() {
        if(CustomDialog.instance.dialogoCargando != null){
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
        super.onDestroy()
    }

    private fun eliminarUltimoCaracter(valor: String): String {
        return if (valor.isNotEmpty()) valor.substring(0, valor.length - 1) else ""
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_alumnoasistencia, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showBackButton()
                return true
            }
            R.id.action_enviarasistencia -> {
                setAsistencia()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showBackButton()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        requestQueueRegAsis?.cancelAll(TAG)
    }

    class EstadoAsistencia (var puedeenviar: Boolean, var asistencias: JSONObject?)
}
