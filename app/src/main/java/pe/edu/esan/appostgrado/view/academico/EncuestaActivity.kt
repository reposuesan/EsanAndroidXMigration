package pe.edu.esan.appostgrado.view.academico

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_encuesta.*
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.EncuestaAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.PreguntaEncuesta
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class EncuestaActivity : AppCompatActivity() {

    private val TAG = "CursosPostActivity"
    private var requestQueue : RequestQueue? = null
    private var requestQueueEnviar : RequestQueue? = null

    private var puedeEnviar = false
    private var adapterEncuesta : EncuestaAdapter? = null
    private var contador = 0
    private var totalEncuesta = 0

    private val LOG = EncuestaActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encuesta)

        val toolbar = main_encuesta as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.encuesta)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        rvPreguntas_encuesta.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvPreguntas_encuesta.adapter = null

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        showTitulos()
    }

    private fun showTitulos() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        sendRequest()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
        }
    }

    private fun sendRequest(){
        val user = ControlUsuario.instance.currentUsuario[0]
        when (user) {
            is Alumno -> {
                lblCurso_encuesta.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
                lblProfesor_encuesta.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
                lblCantidad_encuesta.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
                lblPorcentaje_encuesta.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                ControlUsuario.instance.entroEncuesta = true

                if (user.tipoAlumno == Utilitarios.PRE) {

                    val cursoPre = ControlUsuario.instance.currentCursoPre
                    if (cursoPre != null) {

                        totalEncuesta = cursoPre.listProfesores.size

                        lblCurso_encuesta.text = cursoPre.nombreCurso
                        lblProfesor_encuesta.text = cursoPre.listProfesores[contador].nombreCompleto
                        lblCantidad_encuesta.text = String.format(resources.getString(R.string.format_paginacion), contador + 1, totalEncuesta)

                        val request = JSONObject()
                        //request.put("idEncuesta", 2)
                        request.put("idEncuesta", 1)

                        val requestEncriptada = Utilitarios.jsObjectEncrypted(request, this)

                        if (requestEncriptada != null)
                            onPreguntasEncuesta(Utilitarios.getUrl(Utilitarios.URL.PREGUNTAS_ENCUESTA), requestEncriptada)
                    } else {
                        finish()
                    }
                } else {
                    val cursoPost = ControlUsuario.instance.currentCursoPost
                    if (cursoPost != null) {
                        totalEncuesta = cursoPost.listaSeccionEncuesta.size

                        lblCurso_encuesta.text = cursoPost.cursoNombre
                        lblProfesor_encuesta.text = cursoPost.listaSeccionEncuesta[contador].nombreProfesor
                        lblCantidad_encuesta.text = String.format(resources.getString(R.string.format_paginacion), contador + 1, totalEncuesta)

                        val request = JSONObject()
                        // request.put("idEncuesta", 1)
                        request.put("idEncuesta", 2)

                        val requestEncriptada = Utilitarios.jsObjectEncrypted(request, this)

                        if (requestEncriptada != null)
                            onPreguntasEncuesta(Utilitarios.getUrl(Utilitarios.URL.PREGUNTAS_ENCUESTA), requestEncriptada)
                    } else {
                        finish()
                    }
                }
            }
        }
    }

    private fun onPreguntasEncuesta(url: String, request: JSONObject) {
        puedeEnviar = false
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->
                try {
                    val preguntasJArray = Utilitarios.jsArrayDesencriptar(response["ListarEncuestaPreguntaResult"] as String, this)
                    if (preguntasJArray != null) {
                        if (preguntasJArray.length() > 0) {
                            val listaPreguntas = ArrayList<PreguntaEncuesta>()
                            var header = ""
                            //var orden = 0
                            for (i in 0 until preguntasJArray.length()) {
                                val preguntaJObject = preguntasJArray[i] as JSONObject
                                val esTexto = preguntaJObject["EsTexto"] as Boolean
                                val tipoPregunta = if (esTexto) Utilitarios.TipoPreguntaEncuesta.PREGUNTADOS else Utilitarios.TipoPreguntaEncuesta.PREGUNTAUNO
                                val cabecera = (preguntaJObject["GrupoNombre"] as String).trim()
                                val grupoOrden = preguntaJObject["GrupoOrden"] as Int
                                val idEncuesta = preguntaJObject["IdEncuesta"] as Int
                                val idPregunta = preguntaJObject["IdPregunta"] as Int
                                val pregunta = (preguntaJObject["Pregunta"] as String).trim()
                                val preguntaOrden = preguntaJObject["PreguntaOrden"] as Int

                                if (header == "") {
                                    header = cabecera
                                    //orden = grupoOrden

                                    listaPreguntas.add(PreguntaEncuesta(Utilitarios.TipoPreguntaEncuesta.CABECERA, header, grupoOrden, 0, 0, "", 0))
                                    listaPreguntas.add(PreguntaEncuesta(tipoPregunta, "", grupoOrden, idEncuesta, idPregunta, pregunta, preguntaOrden))
                                } else {
                                    if (header == cabecera) {
                                        listaPreguntas.add(PreguntaEncuesta(tipoPregunta, "", grupoOrden, idEncuesta, idPregunta, pregunta, preguntaOrden))
                                    } else {
                                        header = cabecera
                                        //orden = grupoOrden

                                        listaPreguntas.add(PreguntaEncuesta(Utilitarios.TipoPreguntaEncuesta.CABECERA, header, grupoOrden, 0, 0, "", 0))
                                        listaPreguntas.add(PreguntaEncuesta(tipoPregunta, "", grupoOrden, idEncuesta, idPregunta, pregunta, preguntaOrden))
                                    }
                                }
                            }

                            adapterEncuesta = EncuestaAdapter(listaPreguntas) {
                                if (listaPreguntas.size > 0) {
                                    var cantResueltas = 0
                                    var cantFaltan = 0
                                    for (pregunta in listaPreguntas) {
                                        if (pregunta.tipo == Utilitarios.TipoPreguntaEncuesta.PREGUNTAUNO) {
                                            if (pregunta.puntaje.trim() == "0") cantFaltan++
                                            else cantResueltas++
                                        } else if (pregunta.tipo == Utilitarios.TipoPreguntaEncuesta.PREGUNTADOS) {
                                            if (pregunta.respuesta.trim() == "") cantFaltan++
                                            else cantResueltas++
                                        }
                                    }
                                    val porcentaje = (cantResueltas * 100) / (cantFaltan + cantResueltas)
                                    when (porcentaje) {
                                        //in 0..49 -> lblPorcentaje_encuesta.setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                        in 0..99 -> lblPorcentaje_encuesta.setTextColor(ContextCompat.getColor(this, R.color.incompleto))
                                        else -> lblPorcentaje_encuesta.setTextColor(ContextCompat.getColor(this, R.color.completo))
                                    }
                                    lblPorcentaje_encuesta.text = porcentaje.toString() + "%"
                                } else {
                                    lblPorcentaje_encuesta.visibility = View.GONE
                                }
                            }
                            rvPreguntas_encuesta.visibility = View.VISIBLE
                            rvPreguntas_encuesta.adapter = adapterEncuesta
                            puedeEnviar = true
                        }
                    } else {
                        Log.e(LOG,"An error occurred")
                    }
                } catch (jex: JSONException) {

                } catch (ccex: ClassCastException) {

                }
            },
            { error ->
                when {
                    error is TimeoutError || error.networkResponse == null -> {
                        error.printStackTrace()
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onPreguntasEncuesta(url, request)
                            } else {
                                error.printStackTrace()
                            }
                        }
                    }
                    else -> {
                        error.printStackTrace()
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

    private fun setEnviarEncuesta() {
        var esAlumnoPre = false
        val validar = validarCompletoEncuesta()
        if (validar.first) {
            //val request = JSONObject()
            val jsArray = JSONArray()

            for (i in 0 until validar.second.size) {
                val pregunta = validar.second[i]
                if (ControlUsuario.instance.currentUsuario.size == 1) {
                    val user = ControlUsuario.instance.currentUsuario[0]
                    when (user) {
                        is Alumno -> {
                            if (user.tipoAlumno == Utilitarios.PRE) {
                                esAlumnoPre = true
                                if (pregunta.tipo == Utilitarios.TipoPreguntaEncuesta.PREGUNTAUNO) {
                                    val jsonObject = JSONObject()
                                    jsonObject.put("IdActor", ControlUsuario.instance.currentCursoPre!!.listProfesores[contador].idActor)
                                    jsonObject.put("IdPregunta", pregunta.idPregunta)
                                    jsonObject.put("IdRespuesta", pregunta.idPregunta)
                                    jsonObject.put("IdSeccion", ControlUsuario.instance.currentCursoPre!!.idSeccion)
                                    jsonObject.put("Puntaje", pregunta.puntaje)
                                    jsonObject.put("Respuesta", "")
                                    jsonObject.put("CodigoAlumno", user.codigo)
                                    jsonObject.put("IdEncuesta", ControlUsuario.instance.currentCursoPre!!.listProfesores[contador].idEncuesta)
                                    jsonObject.put("IdProgramacion", ControlUsuario.instance.currentCursoPre!!.listProfesores[contador].idProgramacion)
                                    jsArray.put(jsonObject)
                                } else {
                                    val jsonObject = JSONObject()
                                    jsonObject.put("IdActor", ControlUsuario.instance.currentCursoPre!!.listProfesores[contador].idActor)
                                    jsonObject.put("IdPregunta", pregunta.idPregunta)
                                    jsonObject.put("IdRespuesta", pregunta.idPregunta)
                                    jsonObject.put("IdSeccion", ControlUsuario.instance.currentCursoPre!!.idSeccion)
                                    jsonObject.put("Puntaje", "")
                                    jsonObject.put("Respuesta", pregunta.respuesta)
                                    jsonObject.put("CodigoAlumno", user.codigo)
                                    jsonObject.put("IdEncuesta", ControlUsuario.instance.currentCursoPre!!.listProfesores[contador].idEncuesta)
                                    jsonObject.put("IdProgramacion", ControlUsuario.instance.currentCursoPre!!.listProfesores[contador].idProgramacion)
                                    jsArray.put(jsonObject)
                                }
                            } else {
                                esAlumnoPre = false
                                if (pregunta.tipo == Utilitarios.TipoPreguntaEncuesta.PREGUNTAUNO) {
                                    val jsonObject = JSONObject()
                                    jsonObject.put("IdActor", ControlUsuario.instance.currentCursoPost!!.listaSeccionEncuesta[contador].idProfesor)
                                    jsonObject.put("IdPregunta", pregunta.idPregunta)
                                    jsonObject.put("IdRespuesta", pregunta.idPregunta)
                                    jsonObject.put("IdSeccion", ControlUsuario.instance.currentCursoPost!!.listaSeccionEncuesta[contador].idSeccion)
                                    jsonObject.put("Puntaje", pregunta.puntaje)
                                    jsonObject.put("Respuesta", "")
                                    jsonObject.put("CodigoAlumno", user.codigo)
                                    jsonObject.put("IdEncuesta", ControlUsuario.instance.currentCursoPost!!.listaSeccionEncuesta[contador].idEncuesta)
                                    jsonObject.put("IdProgramacion", ControlUsuario.instance.currentCursoPost!!.listaSeccionEncuesta[contador].idProgramacion)
                                    jsArray.put(jsonObject)
                                } else {
                                    val jsonObject = JSONObject()
                                    jsonObject.put("IdActor", ControlUsuario.instance.currentCursoPost!!.listaSeccionEncuesta[contador].idProfesor)
                                    jsonObject.put("IdPregunta", pregunta.idPregunta)
                                    jsonObject.put("IdRespuesta", pregunta.idPregunta)
                                    jsonObject.put("IdSeccion", ControlUsuario.instance.currentCursoPost!!.listaSeccionEncuesta[contador].idSeccion)
                                    jsonObject.put("Puntaje", "")
                                    jsonObject.put("Respuesta", pregunta.respuesta)
                                    jsonObject.put("CodigoAlumno", user.codigo)
                                    jsonObject.put("IdEncuesta", ControlUsuario.instance.currentCursoPost!!.listaSeccionEncuesta[contador].idEncuesta)
                                    jsonObject.put("IdProgramacion", ControlUsuario.instance.currentCursoPost!!.listaSeccionEncuesta[contador].idProgramacion)
                                    jsArray.put(jsonObject)
                                }
                            }
                        }
                    }
                }
            }

            val requestEncriptado = Utilitarios.jsArrayEncrypted(jsArray, this)
            if (requestEncriptado != null)
                onEnviarEncuesta(Utilitarios.getUrl(Utilitarios.URL.REGISTRAR_ENCUESTA), requestEncriptado, esAlumnoPre)
            else {
                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_encriptar), Snackbar.LENGTH_LONG)
                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                snack.show()
            }

        } else {
            val dialog = AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.advertencia))
                    .setMessage(resources.getString(R.string.advertencia_completar_encuesta))
                    .setPositiveButton(resources.getString(R.string.aceptar), null)
                    .create()
            dialog.show()
            dialog.getButton(android.app.Dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.esan_rojo))
            dialog.findViewById<TextView>(android.R.id.message)?.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
        }
    }

    private fun onEnviarEncuesta(url: String, request: JSONObject, esPregrado: Boolean) {
        CustomDialog.instance.showDialogLoad(this)

        requestQueueEnviar = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->
                CustomDialog.instance.dialogoCargando?.dismiss()
                try {
                    val respuesta = Utilitarios.stringDesencriptar(response["RegistrarEncuestaRespuestaByAlumnoResult"] as String, this)

                    if (respuesta != null) {
                        if (respuesta == "true") {
                            lblPorcentaje_encuesta.setTextColor(ContextCompat.getColor(this, R.color.incompleto))
                            lblPorcentaje_encuesta.text = "0 %"

                            verificarEncuestas(esPregrado)
                        } else {
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.advertencia_no_registro_encuesta), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()
                        }
                    } else {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_desencriptar), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                        snack.show()
                    }
                } catch (jex: JSONException) {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_respuesta_server), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                } catch (ccax: ClassCastException) {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_respuesta_server), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
            },
            { error ->
                when {
                    error is TimeoutError || error.networkResponse == null -> {
                        CustomDialog.instance.dialogoCargando?.dismiss()
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_respuesta_server), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onEnviarEncuesta(url, request, esPregrado)
                            } else {
                                CustomDialog.instance.dialogoCargando?.dismiss()
                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_respuesta_server), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        }
                    }
                    else -> {
                        CustomDialog.instance.dialogoCargando?.dismiss()
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_respuesta_server), Snackbar.LENGTH_LONG)
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
        requestQueueEnviar?.add(jsObjectRequest)
    }

    private fun verificarEncuestas(esPregrado: Boolean) {
        contador += 1
        if (contador == totalEncuesta) {
            puedeEnviar = false
            rvPreguntas_encuesta.visibility = View.GONE

            val dialog = AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.mensaje))
                    .setMessage(resources.getString(R.string.mensaje_encuesta_completada))
                    .setCancelable(false)
                    .setPositiveButton(resources.getText(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                        if (esPregrado) {

                        } else {

                        }
                        finish()
                        //recarga
                    })
                    .create()
            dialog.show()
            dialog.getButton(android.app.Dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.esan_rojo))
            dialog.findViewById<TextView>(android.R.id.message)?.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
        } else {
            showTitulos()
        }
    }

    private fun validarCompletoEncuesta(): Pair<Boolean, ArrayList<PreguntaEncuesta>> {
        val listaPreguntas = ArrayList<PreguntaEncuesta>()
        if (adapterEncuesta != null) {
            val preguntas = adapterEncuesta!!.listaPreguntas

            if (preguntas.isNotEmpty()) {
                for (pregunta in preguntas) {
                    if (pregunta.tipo == Utilitarios.TipoPreguntaEncuesta.PREGUNTAUNO) {
                        if (pregunta.puntaje.trim() == "0") {
                            return Pair(false, ArrayList())
                        } else {
                            listaPreguntas.add(pregunta)
                        }
                    } else if (pregunta.tipo == Utilitarios.TipoPreguntaEncuesta.PREGUNTADOS) {
                        if (pregunta.respuesta.trim().isEmpty()) {
                            return Pair(false, ArrayList())
                        } else {
                            listaPreguntas.add(pregunta)
                        }
                    }
                }
                return Pair(true, listaPreguntas)
            } else {
                return Pair(false, ArrayList())
            }
        } else {
            return Pair(false, ArrayList())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_enviarencuesta -> {
                if (puedeEnviar) {
                    setEnviarEncuesta()
                } else {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.advertencia_no_enviar), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                    snack.show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_encuesta, menu)
        return true
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        requestQueueEnviar?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
    }

    override fun onDestroy() {
        if(CustomDialog.instance.dialogoCargando != null){
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
        super.onDestroy()
    }
}
