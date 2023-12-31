package pe.edu.esan.appostgrado.view.horario

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.crashlytics.FirebaseCrashlytics
/*import com.crashlytics.android.Crashlytics*/
import kotlinx.android.synthetic.main.activity_horario_detalle.*
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.HorarioDetalleAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.Horario
import pe.edu.esan.appostgrado.entidades.Profesor
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class HorarioDetalleActivity : AppCompatActivity(), LocationListener {

    private val TAG = "HorarioDetalleActivity"
    private var locationmanager: LocationManager? = null
    private val KEYDIA = "KEY_DIA"
    private val KEYHOY = "KEY_HOY"

    private var longitud = ""
    private var latitud = ""
    private var red = ""

    private var diaSeleccionado: String? = ""
    private var eshoy = false

    private var requestQueue: RequestQueue? = null
    private var requestQueueRegAsisProf: RequestQueue? = null
    private var requestQueueRegCopiaAsisAlumno: RequestQueue? = null

    private var adapterHorario: HorarioDetalleAdapter? = null

    private val LOG = HorarioDetalleActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horario_detalle)

        val toolbar = main_ahorariodetalle as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)

        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras != null) {
                diaSeleccionado = extras[KEYDIA] as String
                eshoy = extras[KEYHOY] as Boolean
            }
        } else {
            diaSeleccionado = savedInstanceState.getString(KEYDIA)
            eshoy = savedInstanceState.getBoolean(KEYHOY)
        }

        toolbar.toolbar_title.text = diaSeleccionado

        rvHorario_ahorariodetalle.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvHorario_ahorariodetalle.adapter = null

        if(ControlUsuario.instance.currentUsuario.size == 1){
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
        getHorario()
    }


    private fun getHorario() {
        var listaHorarioDetalle = ArrayList<Horario>()
        val misPreferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)

        try {
            when (ControlUsuario.instance.currentUsuario[0]) {
                is Alumno -> {
                    listaHorarioDetalle = ControlUsuario.instance.currentListHorarioSelect
                    rvHorario_ahorariodetalle.adapter =
                        HorarioDetalleAdapter(listaHorarioDetalle) { horario, i, tipoClick -> }
                }
                is Profesor -> {
                    if (eshoy) {
                        for (horario in ControlUsuario.instance.currentListHorarioSelect) {
                            val hActual = try {
                                horario.horaConsulta.split(" ")[1]
                            } catch (e: IndexOutOfBoundsException) {
                                ""
                            }

                            val fini = Utilitarios.getStringToDateHHmm(horario.horaInicio)
                            val ffin = Utilitarios.getStringToDateHHmm(horario.horaFin)
                            val factual = Utilitarios.getStringToDateHHmm(hActual)


                            if (fini != null && ffin != null && factual != null) {
                                val finni = Utilitarios.addMinutesToDate(fini, -31)
                                var ffinn = Utilitarios.addMinutesToDate(ffin, 11)
                                if (finni.before(factual) && ffinn.after(factual)) {
                                    horario.tipoHorario = 3
                                } else {
                                    ffinn = Utilitarios.addMinutesToDate(ffinn, -1)
                                    if (ffinn.before(factual)) {
                                        horario.tipoHorario = 4
                                    }
                                }
                                listaHorarioDetalle.add(horario)
                            } else {
                                listaHorarioDetalle.add(horario)
                            }
                        }

                        adapterHorario = HorarioDetalleAdapter(listaHorarioDetalle) { horario, position, tipoClick ->
                            when (tipoClick) {
                                Utilitarios.TipoClick.OnCLick -> {
                                    val usuario = ControlUsuario.instance.currentUsuario[0]
                                    when (usuario) {
                                        is Profesor -> {
                                            val valores = JSONObject()
                                            valores.put("CodProfesor", usuario.codigo)
                                            valores.put("CodSeccion", horario.seccionCodigo)
                                            valores.put("IdHorario", horario.idHorario)
                                            valores.put(
                                                "IdSesion",
                                                if (horario.esPregrado == 1) -1 else horario.idSesion
                                            )

                                            val listaHorarioAdelante = ArrayList<Horario>()
                                            for (i in position + 1 until listaHorarioDetalle.size) {
                                                if (horario.seccionCodigo == listaHorarioDetalle[i].seccionCodigo) {
                                                    listaHorarioAdelante.add(listaHorarioDetalle[i])
                                                }
                                            }
                                            ControlUsuario.instance.copiarListHorario = listaHorarioAdelante

                                            val requestEncriptado = Utilitarios.jsObjectEncrypted(valores, this)
                                            if (requestEncriptado != null) {
                                                ControlUsuario.instance.indexActualiza = position
                                                getConsultarAsistenciaProfesor(
                                                    Utilitarios.getUrl(Utilitarios.URL.ASIS_PROF),
                                                    requestEncriptado,
                                                    valores,
                                                    horario.curso,
                                                    horario
                                                )
                                            }
                                        }
                                    }
                                }
                                Utilitarios.TipoClick.OnLongClick -> {
                                    /*
                                                    val listaCadena = ArrayList<String>()
                                                    val listaEntero = ArrayList<Int>()

                                                    for (h in ControlUsuario.instance.currentListHorarioSelect) {
                                                        if (h.tipoHorario == 3 && h.seccionCodigo == horario.seccionCodigo && h.idHorario == horario.idHorario && h.takeAssist == 0) {
                                                            listaCadena.add(h.horaInicio + " - " + h.horaFin)
                                                            listaEntero.add(h.idSesion)
                                                        }
                                                    }

                                                    val array = arrayOfNulls<String>(listaCadena.size)
                                                    listaCadena.toArray(array)
                                                    var idSesion = -1

                                                    val alertCopiarEn = AlertDialog.Builder (this)
                                                            .setTitle(resources.getString(R.string.seleccionar_horario_pegar_asistencia))
                                                            .setSingleChoiceItems(array, -1, DialogInterface.OnClickListener { dialogInterface, i ->
                                                                idSesion = listaEntero[i]
                                                            })
                                                            .setPositiveButton(resources.getString(R.string.pegar), DialogInterface.OnClickListener { dialogInterface, i ->
                                                                if (idSesion != -1) {
                                                                    //mOnClickListener.onItemClick(codSeccion, idSesion, idHorario, sesion);

                                                                    val request = JSONObject()
                                                                    request.put("CodSeccion", horario.seccionCodigo)
                                                                    request.put("IdSesion", horario.idSesion)
                                                                    request.put("IdHorario", horario.idHorario)
                                                                    request.put("IdNuevaSesion", idSesion)
                                                                    onClonarAsistenciaAlumnos(Utilitarios.getUrl(Utilitarios.URL.REGISTRAR_ASISTENCIA_COPIA), request)
                                                                }
                                                            })
                                                            .setNegativeButton(resources.getString(R.string.cancelar), null)
                                                            .create()

                                                    alertCopiarEn.show()
                                                    alertCopiarEn.getButton(android.app.Dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.esan_rojo))
                                                    */
                                }
                            }
                        }
                        rvHorario_ahorariodetalle.adapter = adapterHorario

                        getConsultarHorarioProfesor(listaHorarioDetalle)
                    } else {
                        listaHorarioDetalle = ControlUsuario.instance.currentListHorarioSelect
                        rvHorario_ahorariodetalle.adapter =
                            HorarioDetalleAdapter(listaHorarioDetalle, { horario, i, tipoClick -> })
                    }
                }
            }
        } catch (e: Exception) {
            val usuario = misPreferencias?.getString("code", "")
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log(
                "E/HorarioDetalleActivity: ArrayList ControlUsuario.instance.currentUsuario has 0 elements, the user is $usuario."
            )
        }
    }


    private fun getConsultarHorarioProfesor(listaHorario: ArrayList<Horario>) {
        var ultimaSeccion = 0
        val listaHorarioSeleccion = ArrayList<Horario>()
        for (horario in listaHorario) {
            if (ultimaSeccion == 0 || ultimaSeccion != horario.idSeccion) {
                listaHorarioSeleccion.add(horario)
                ultimaSeccion = horario.idSeccion
            }
        }

        for (horario in listaHorarioSeleccion) {
            val hActual = try {
                horario.horaConsulta.split(" ")[1]
            } catch (e: IndexOutOfBoundsException) {
                ""
            }

            val fini = Utilitarios.getStringToDateHHmm(horario.horaInicio)
            val ffinal = Utilitarios.getStringToDateHHmm(horario.horaFin)
            val factual = Utilitarios.getStringToDateHHmm(hActual)

            if (fini != null && factual != null) {
                val finni = Utilitarios.addMinutesToDate(fini, -31)
                val ffinn = Utilitarios.addMinutesToDate(ffinal, 11)
                if (finni.before(factual) && ffinn.after(factual)) {
                    if (ControlUsuario.instance.currentUsuario.size == 1) {
                        val usuario = ControlUsuario.instance.currentUsuario[0]
                        when (usuario) {
                            is Profesor -> {
                                val valores = JSONObject()
                                valores.put("CodProfesor", usuario.codigo)
                                valores.put("CodSeccion", horario.seccionCodigo)
                                valores.put("IdHorario", horario.idHorario)
                                valores.put("IdSesion", if (horario.esPregrado == 1) -1 else horario.idSesion)
                                val requestEncriptado = Utilitarios.jsObjectEncrypted(valores, this)
                                if (requestEncriptado != null)
                                    getConsultarAsistenciaProfesor(
                                        Utilitarios.getUrl(Utilitarios.URL.ASIS_PROF),
                                        requestEncriptado,
                                        valores,
                                        horario.curso,
                                        null
                                    )

                            }
                        }
                    }
                }
            }
        }
    }


    private fun getConsultarAsistenciaProfesor(
        url: String,
        request: JSONObject,
        requestSinEncriptar: JSONObject,
        curso: String,
        horario: Horario?
    ) {
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
            Request.Method.POST,
            url,
            request,
            { response ->
                try {

                    val respuesta =
                        Utilitarios.stringDesencriptar(response["ConsultarAsistenciaProfesorResult"] as String, this)
                    if (respuesta != null) {
                        if (respuesta == "false") {
                            showPreguntarTomarAsistencia(requestSinEncriptar, curso, horario)
                        } else {
                            if (horario != null) {
                                ControlUsuario.instance.cambioPantalla = true
                                ControlUsuario.instance.currentHorario = horario

                                val intentTomarAsistencia = Intent(this, TomarAsistenciaActivity::class.java)
                                intentTomarAsistencia.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intentTomarAsistencia)
                            }
                        }
                    } else {
                        val snack = Snackbar.make(
                            findViewById(android.R.id.content),
                            resources.getString(R.string.error_desencriptar),
                            Snackbar.LENGTH_LONG
                        )
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                            Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            .setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                } catch (je: JSONException) {
                    ControlUsuario.instance.indexActualiza = -1
                    val snack = Snackbar.make(
                        findViewById(android.R.id.content),
                        resources.getString(R.string.error_no_conexion),
                        Snackbar.LENGTH_LONG
                    )
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                        Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        .setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
            },
            { error ->
                when {
                    error is TimeoutError || error.networkResponse == null -> {
                        ControlUsuario.instance.indexActualiza = -1
                        val snack = Snackbar.make(
                            findViewById(android.R.id.content),
                            resources.getString(R.string.error_no_conexion),
                            Snackbar.LENGTH_LONG
                        )
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                            Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            .setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                getConsultarAsistenciaProfesor(
                                    url,
                                    request,
                                    requestSinEncriptar,
                                    curso,
                                    horario)
                            } else {
                                ControlUsuario.instance.indexActualiza = -1
                                val snack = Snackbar.make(
                                    findViewById(android.R.id.content),
                                    resources.getString(R.string.error_no_conexion),
                                    Snackbar.LENGTH_LONG
                                )
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                                    Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                                    .setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        }
                    }
                    else -> {
                        ControlUsuario.instance.indexActualiza = -1
                        val snack = Snackbar.make(
                            findViewById(android.R.id.content),
                            resources.getString(R.string.error_no_conexion),
                            Snackbar.LENGTH_LONG
                        )
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                            Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            .setTextColor(ContextCompat.getColor(this, R.color.danger_text))
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
        requestQueue?.add(jsObjectRequest)
    }


    private fun showPreguntarTomarAsistencia(request: JSONObject, curso: String, horario: Horario?) {
        val alerta = AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.confirmar))
            .setMessage(String.format(resources.getString(R.string.preguntar_reg_asistencia), curso))
            .setNegativeButton(resources.getString(R.string.no), null)
            .setPositiveButton(resources.getString(R.string.si), DialogInterface.OnClickListener { dialogInterface, i ->
                request.put("Latitud", latitud)
                request.put("Longitud", longitud)
                request.put("NombreRed", "")

                val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                if (requestEncriptado != null) {
                    onRegistrarAsistenciaProfesor(
                        Utilitarios.getUrl(Utilitarios.URL.REG_ASIS_PROF),
                        requestEncriptado,
                        horario
                    )
                }
            })
            .create()
        alerta.show()
        alerta.getButton(android.app.Dialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(this, R.color.esan_rojo))
        alerta.findViewById<TextView>(android.R.id.message)?.typeface =
            Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)

    }


    private fun onRegistrarAsistenciaProfesor(url: String, request: JSONObject, horario: Horario?) {

        requestQueueRegAsisProf = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
            url,
            request,
            { response ->
                try {
                    val respuesta =
                        Utilitarios.stringDesencriptar(response["GrabarAsistenciaProfesorResult"] as String, this)

                    if (respuesta != null) {

                        if (respuesta == "false") {
                            val snack = Snackbar.make(
                                findViewById(android.R.id.content),
                                resources.getString(R.string.error_no_marco_asistencia),
                                Snackbar.LENGTH_LONG
                            )
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                                Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                                .setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()
                        } else {
                            val snack = Snackbar.make(
                                findViewById(android.R.id.content),
                                resources.getString(R.string.info_si_asistencia_registrada),
                                Snackbar.LENGTH_LONG
                            )
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.info))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                                Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                                .setTextColor(ContextCompat.getColor(this, R.color.info_text))
                            snack.show()
                            if (horario != null) {
                                ControlUsuario.instance.cambioPantalla = true
                                ControlUsuario.instance.currentHorario = horario
                                val intentTomarAsistencia = Intent(this, TomarAsistenciaActivity::class.java)
                                intentTomarAsistencia.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intentTomarAsistencia)
                            }
                        }
                    } else {
                        val snack = Snackbar.make(
                            findViewById(android.R.id.content),
                            resources.getString(R.string.error_desencriptar),
                            Snackbar.LENGTH_LONG
                        )
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                            Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            .setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }

                } catch (jex: JSONException) {
                    val snack = Snackbar.make(
                        findViewById(android.R.id.content),
                        resources.getString(R.string.error_no_conexion),
                        Snackbar.LENGTH_LONG
                    )
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                        Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        .setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
            },
            { error ->
                when {
                    error is TimeoutError || error.networkResponse == null -> {
                        val snack = Snackbar.make(
                            findViewById(android.R.id.content),
                            resources.getString(R.string.error_no_conexion),
                            Snackbar.LENGTH_LONG
                        )
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                            Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            .setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onRegistrarAsistenciaProfesor(url, request, horario)
                            } else {
                                val snack = Snackbar.make(
                                    findViewById(android.R.id.content),
                                    resources.getString(R.string.error_no_conexion),
                                    Snackbar.LENGTH_LONG
                                )
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                                    Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                                    .setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        }
                    }
                    else -> {
                        val snack = Snackbar.make(
                            findViewById(android.R.id.content),
                            resources.getString(R.string.error_no_conexion),
                            Snackbar.LENGTH_LONG
                        )
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                            Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            .setTextColor(ContextCompat.getColor(this, R.color.danger_text))
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
        requestQueueRegAsisProf?.add(jsObjectRequest)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                ControlUsuario.instance.cambioPantalla = true
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ControlUsuario.instance.cambioPantalla = true
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEYDIA, diaSeleccionado)
        outState.putBoolean(KEYHOY, eshoy)
    }


    override fun onLocationChanged(p0: Location) {
        longitud = p0.longitude.toString()
        latitud = p0.latitude.toString()
    }


    override fun onStatusChanged(p0: String, p1: Int, p2: Bundle?) {

    }


    override fun onProviderEnabled(p0: String) {

    }


    override fun onProviderDisabled(p0: String) {
        longitud = ""
        latitud = ""
    }


    override fun onResume() {
        super.onResume()

        try {
            locationmanager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationmanager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10.0f, this)
            locationmanager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10.0f, this)

            val gps = locationmanager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val network = locationmanager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val location: Location? = if (gps != null && network != null) {
                if (gps.time > network.time) {
                    gps
                } else {
                    network
                }
            } else if (gps != null) {
                gps
            } else if (network != null) {
                network
            } else {
                null
            }

            longitud = location?.longitude.toString()
            latitud = location?.latitude.toString()

        } catch (se: SecurityException) {

        } catch (ne: NullPointerException) {

        }

        if (ControlUsuario.instance.recargaHorarioProfesor) {
            ControlUsuario.instance.recargaHorarioProfesor = false
            if (ControlUsuario.instance.indexActualiza != -1) {
                if (ControlUsuario.instance.tomoAsistenciaMasica) {
                    ControlUsuario.instance.tomoAsistenciaMasica = false

                    if (adapterHorario?.listaHorario != null) {
                        val codigoSeccion =
                            adapterHorario?.listaHorario?.get(ControlUsuario.instance.indexActualiza)?.seccionCodigo
                        adapterHorario?.listaHorario!!.filter { horario -> horario.seccionCodigo == codigoSeccion }
                            .map { horario -> horario.takeAssist = 1 }

                        adapterHorario?.notifyDataSetChanged()
                    }

                } else {
                    adapterHorario?.listaHorario?.get(ControlUsuario.instance.indexActualiza)?.takeAssist = 1
                    adapterHorario?.notifyItemChanged(ControlUsuario.instance.indexActualiza)

                }
                ControlUsuario.instance.indexActualiza = -1

                val snack = Snackbar.make(
                    findViewById(android.R.id.content),
                    resources.getString(R.string.info_si_asistencia_registrada),
                    Snackbar.LENGTH_LONG
                )
                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.info))
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface =
                    Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    .setTextColor(ContextCompat.getColor(this, R.color.info_text))
                snack.show()
            }
        }
    }


    override fun onPause() {
        super.onPause()
        try {
            locationmanager?.removeUpdates(this)
        } catch (se: SecurityException) {
        }
        locationmanager = null
    }


    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        requestQueueRegAsisProf?.cancelAll(TAG)
        requestQueueRegCopiaAsisAlumno?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()

        ControlUsuario.instance.pantallaSuspendida = !ControlUsuario.instance.cambioPantalla

    }


    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        if(CustomDialog.instance.dialogoCargando != null){
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
        super.onDestroy()
    }

}
