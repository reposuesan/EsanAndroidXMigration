package pe.edu.esan.appostgrado.view.academico.postgrado

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_cursos_post.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.CursoPostAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.*
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.academico.EncuestaActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class CursosPostActivity : AppCompatActivity() {

    private var requestQueue : RequestQueue? = null
    private var requestQueueEncuesta : RequestQueue? = null
    private var requestQueueAsistencia : RequestQueue? = null
    private val KEY_CODIGO = "KEY_CODIGO_PROGRAMA"
    private var codigoPromo: String? = ""

    private var anchoPantalla: Int = 0
    private var densidad: Float = 0f

    var codigo = ""
    private val TAG = "CursosPostActivity"
    private val LOG = CursosPostActivity::class.simpleName
    private var cursoAdapter : CursoPostAdapter? = null

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cursos_post)

        val toolbar = main_acursospost as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.cursos)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        lblMensaje_acursospost.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
        rvCurso_acursospost.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvCurso_acursospost.adapter = null

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras != null) {
                codigoPromo = extras[KEY_CODIGO] as String
            }
        } else {
            codigoPromo = savedInstanceState.getString(KEY_CODIGO)
        }

        if (codigoPromo != "") {
            getEncuestaPorPrograma()
        }
    }


    override fun onResume() {
        super.onResume()
        if (ControlUsuario.instance.entroEncuesta) {
            ControlUsuario.instance.entroEncuesta = false
            getEncuestaPorPrograma()
        }
    }


    private fun getEncuestaPorPrograma (/*codigoPromocion: String*/) {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        Log.w(LOG, "operationFinishedActivityPublic.observe() was called")
                        Log.w(LOG, "sendRequest() was called")
                        sendRequest()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
            Log.w(LOG, "controlViewModel.getDataFromRoom() was called")
        }
    }


    private fun sendRequest(){
        val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

        val request = JSONObject()
        request.put("CodAlumno", usuario.codigo)
        request.put("CodPromocion", codigoPromo)

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
        Log.i(LOG, requestEncriptado.toString())
        if (requestEncriptado != null)
            onEncuestaPorPrograma(Utilitarios.getUrl(Utilitarios.URL.VALIDA_ENCUESTA_PROGRAMA), requestEncriptado, codigoPromo)
        else {
            lblMensaje_acursospost.visibility = View.VISIBLE
            lblMensaje_acursospost.text = resources.getString(R.string.error_encriptar)
        }
    }


    //ESTE MÉTODO RECUPERA LAS ENCUESTAS PENDIENTES DE CADA PROGRAMA
    private fun onEncuestaPorPrograma (url: String, request: JSONObject, codigoPromocion: String?) {

        requestQueueEncuesta = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest (
                url,
                request,
            { response ->
                Log.i(LOG, response.toString())
                try {
                    val listaEncuestas = ArrayList<SeccionEncuestaPos>()
                    val encuestaJArray = Utilitarios.jsArrayDesencriptar(response["ListarEncuestasProfesorPostResult"] as String, this)
                    /*val encuestaJArray = response["ListarEncuestasProfesorPostResult"] as JSONArray*/
                    if (encuestaJArray != null) {
                        if (encuestaJArray.length() > 0) {
                            for (i in 0 until encuestaJArray.length()) {
                                val encuestaJObject = encuestaJArray[i] as JSONObject
                                val evaluo = encuestaJObject["Evaluo"] as Int
                                /*val encuestaVigente = encuestaJObject["Vigente"] as Int*/
                                //Si evaluo es 0, entonces el usuario no ha evaluado
                                val encuestar = evaluo == 0
                                /*val encuestar = (encuestaVigente == 1  && evaluo == 0)*/
                                if (encuestar) {
                                    val idSeccion = encuestaJObject["IdSeccion"] as Int
                                    val idProfesor = encuestaJObject["IdProfesor"] as Int
                                    val idEncuesta = encuestaJObject["IdEncuesta"] as Int
                                    val idProgramacion = encuestaJObject["IdProgramacion"] as Int
                                    val nombreCompleto = encuestaJObject["nombreCompleto"] as String
                                    /*val encuestaVigente = encuestaJObject["Vigente"] as Int*/

                                    //Si vigente es 1, entonces la encuesta está vigente
                                    /*val encuestaVigente = vigente == 1*/

                                    /*val encuestar = (encuestaVigente == 1  && evaluo == 0)*/

                                    listaEncuestas.add(SeccionEncuestaPos(codigoPromocion, idSeccion, idProfesor, nombreCompleto, idEncuesta, idProgramacion, encuestar))
                                }
                            }
                        }
                        showCursos(codigoPromocion, listaEncuestas)
                    } else {
                        lblMensaje_acursospost.visibility = View.VISIBLE
                        lblMensaje_acursospost.text = resources.getText(R.string.error_desencriptar)
                    }
                } catch (jex: JSONException) {
                    lblMensaje_acursospost.visibility = View.VISIBLE
                    lblMensaje_acursospost.text = resources.getText(R.string.error_respuesta_server)
                } catch (ccex: ClassCastException) {
                    lblMensaje_acursospost.visibility = View.VISIBLE
                    lblMensaje_acursospost.text = resources.getText(R.string.error_respuesta_server)
                }
            },
            { error ->
                Log.e(LOG, error.message.toString())
                lblMensaje_acursospost.visibility = View.VISIBLE
                lblMensaje_acursospost.text = resources.getText(R.string.error_respuesta_server)
            }
        )
        jsObjectRequest.tag = TAG
        requestQueueEncuesta?.add(jsObjectRequest)
    }


    private fun showCursos(codigoPromocion: String?, listaEncuesta: ArrayList<SeccionEncuestaPos>) {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

            val request = JSONObject()
            request.put("CodAlumno", usuario.codigo)
            request.put("CodPromocion", codigoPromocion)

            val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
            if (requestEncriptado != null)
                onCursos(Utilitarios.getUrl(Utilitarios.URL.CURSOS_POST), requestEncriptado, listaEncuesta)
            else {
                lblMensaje_acursospost.visibility = View.VISIBLE
                lblMensaje_acursospost.text = resources.getString(R.string.error_desencriptar)
            }
        } else {
            lblMensaje_acursospost.visibility = View.VISIBLE
            lblMensaje_acursospost.text = resources.getString(R.string.error_ingreso)
        }
    }


    //ESTE MÉTODO DEVUELVE LOS CURSOS Y LOS ORDENA POR PROGRAMA
    private fun onCursos(url: String, request: JSONObject, listaEncuesta: ArrayList<SeccionEncuestaPos>) {

        Log.i(LOG, url)
        Log.i(LOG, request.toString())

        prbCargando_acursospost.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    try {
                        /*val cursosJArray = response["ListarHistoricoNotasAlumnoPostResult"] as JSONArray*/
                        val cursosJArray = Utilitarios.jsArrayDesencriptar(response["ListarHistoricoNotasAlumnoPostResult"] as String, this)
                        if (cursosJArray != null) {
                            if (cursosJArray.length() > 0) {

                                var ultimoModulo = 0
                                val listaModuloCursos = ArrayList<CursoPostModulo>()
                                var listaCursos = ArrayList<CursosPost?>()

                                val language = Locale.getDefault().displayLanguage

                                for (cursoItemFromArray in 0 until cursosJArray.length()) {

                                    val cursoJson = cursosJArray[cursoItemFromArray] as JSONObject

                                    Log.i(LOG, cursoJson.toString())

                                    val nombreCurso = cursoJson["cursoNombre"] as String
                                    val idCurso = cursoJson["IdCurso"] as Int
                                    var promedioCondicion = (cursoJson["PromedioCondicion"] as String).toUpperCase()
                                    val promedioFinal = cursoJson["PromedioFinal"] as String
                                    val idSeccion = cursoJson["idseccion"] as Int
                                    val codigoSeccion = cursoJson["SeccionCodigo"] as String
                                    val modulo = cursoJson["Modulo"] as Int
                                    val cursoActual = cursoJson["CursoActual"] as Boolean

                                    if (promedioCondicion.contains("NO REGISTRADA")){
                                        if(language.equals("English")) {
                                            promedioCondicion = "PENDING GRADE"
                                        } else {
                                            promedioCondicion = "NOTA NO\nREGISTRADA"
                                        }
                                    }


                                    val cursoPost = CursosPost(modulo, idCurso, nombreCurso, promedioCondicion, promedioFinal, idSeccion, codigoSeccion, cursoActual)

                                    val lstEncuesta = ArrayList<SeccionEncuestaPos>()

                                    for (encuesta in listaEncuesta) {
                                        if (encuesta.idSeccion == idSeccion) {
                                            cursoPost.encuesta = true
                                            lstEncuesta.add(encuesta)
                                            /*listaEncuesta.remove(encuesta)*/
                                        }
                                    }

                                    cursoPost.listaSeccionEncuesta = lstEncuesta

                                    if (cursoItemFromArray == 0) {

                                        Log.i(LOG, "Primer modulo en JSON Response: $modulo")
                                        ultimoModulo = modulo
                                        listaCursos = ArrayList()
                                        listaCursos.add(cursoPost)

                                    } else {
                                        if (ultimoModulo == modulo) {

                                            Log.i(LOG, "Seguimos en el mismo modulo")
                                            listaCursos.add(cursoPost)
                                        } else {

                                            listaModuloCursos.add(CursoPostModulo(ultimoModulo, listaCursos))

                                            Log.i(LOG, "Siguiente Modulo en JSON Response: $modulo")
                                            ultimoModulo = modulo
                                            listaCursos = ArrayList()
                                            listaCursos.add(cursoPost)
                                        }
                                    }
                                }

                                Log.i(LOG, "Agregar ultimo modulo")

                                listaModuloCursos.add(CursoPostModulo(ultimoModulo, listaCursos))
                                cursoAdapter = CursoPostAdapter(listaModuloCursos , {codigoSeccion ->

                                    Log.i(LOG, "Asistencia")
                                    Log.i(LOG, codigoSeccion)

                                    this.getAsistenciaCurso(codigoSeccion)

                                }) { posAdapter, posModulo, posCurso, cursosPost, encuestar ->
                                    if (encuestar) {
                                        Log.i(LOG, "IR A ENCUESTA")
                                        /*cursosPost.listaSeccionEncuesta*/
                                        ControlUsuario.instance.currentCursoPost = cursosPost
                                        /*ControlUsuario.instance.encuestasPost = cursosPost.listaSeccionEncuesta*/
                                        val intentEncuesta = Intent(this, EncuestaActivity::class.java)
                                        intentEncuesta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        startActivity(intentEncuesta)

                                    } else {
                                        //SI LA LISTA ESTÁ EXPANDIDA
                                        if (cursosPost.expandible) {
                                            listaModuloCursos[posModulo].listaCursos[posCurso]?.cargando = true
                                            /*updateAdater(posAdapter)*/

                                            val totalNotas = listaModuloCursos[posModulo].listaCursos[posCurso]?.detalleNotas?.size

                                            if (totalNotas != null)
                                                for (x in (totalNotas - 1) downTo 0) {
                                                    listaModuloCursos[posModulo].listaCursos.removeAt(posCurso + x + 1)
                                                    /*removeAdapter(posCurso + x + 1)*/
                                                }

                                            listaModuloCursos[posModulo].listaCursos[posCurso]?.cargando = false
                                            listaModuloCursos[posModulo].listaCursos[posCurso]?.expandible = false
                                            /*updateAdater(posAdapter)*/
                                            refreshAdapter()
                                        //SI LA LISTA NO ESTÁ EXPANDIDA
                                        } else {
                                            listaModuloCursos[posModulo].listaCursos[posCurso]?.cargando = true
                                            /*updateAdater(posAdapter)*/
                                            refreshAdapter()

                                            if (ControlUsuario.instance.currentUsuario.size == 1) {
                                                val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

                                                val req = JSONObject()
                                                req.put("CodAlumno", usuario.codigo)
                                                req.put("CodSeccion", cursosPost.seccionCodigo)

                                                val requestEncriptado = Utilitarios.jsObjectEncrypted(req, this)
                                                if (requestEncriptado != null) {

                                                    val jsObjectRequest = JsonObjectRequest(
                                                        Request.Method.POST,
                                                        Utilitarios.getUrl(Utilitarios.URL.NOTAS_POST),
                                                        requestEncriptado,
                                                        Response.Listener { response ->
                                                            Log.i(LOG, response.toString())
                                                            try {
                                                                /*val notasJArray = response["ListarDetalleNotaHistoricoResult"] as JSONArray*/
                                                                val notasJArray = Utilitarios.jsArrayDesencriptar(response["ListarDetalleNotaHistoricoResult"] as String, this)
                                                                if (notasJArray != null) {
                                                                    if (notasJArray.length() > 0) {
                                                                        val notasCurso = ArrayList<NotasPost>()
                                                                        for (i in 0 until notasJArray.length()) {
                                                                            val notasJson = notasJArray[i] as JSONObject
                                                                            val tipo = notasJson["NotaNombre"] as String
                                                                            val peso = notasJson["Peso"] as String
                                                                            val nota = notasJson["Nota"] as String

                                                                            notasCurso.add(NotasPost(tipo, peso, nota))
                                                                        }
                                                                        listaModuloCursos[posModulo].listaCursos[posCurso]?.detalleNotas = notasCurso
                                                                        listaModuloCursos[posModulo].listaCursos[posCurso]?.cargando = false
                                                                        listaModuloCursos[posModulo].listaCursos[posCurso]?.expandible = true
                                                                        /*updateAdater(posAdapter)*/

                                                                        var contadorAdd = posCurso
                                                                        /*contadorPos = posAdapter*/
                                                                        for (x in 0 until notasCurso.size) {
                                                                            /*contadorAdd += 1
                                                                            contadorPos += 1*/
                                                                            listaModuloCursos[posModulo].listaCursos.add(++contadorAdd, null)
                                                                            /*insertAdapter(++contadorPos)*/
                                                                            Log.w(LOG, "contadorAdd: $contadorAdd")
                                                                            Log.w(LOG, "contadorPos: $posAdapter")
                                                                            Log.w(LOG, "contadorAdd - contadorPos: $contadorAdd  - $posAdapter")
                                                                        }

                                                                    } else {
                                                                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.no_mayor_detalle), Snackbar.LENGTH_LONG)
                                                                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.info))
                                                                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                                                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.info_text))
                                                                        snack.show()
                                                                        listaModuloCursos[posModulo].listaCursos[posCurso]?.cargando = false
                                                                        /*updateAdater(posAdapter)*/
                                                                    }
                                                                    refreshAdapter()
                                                                } else {
                                                                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_desencriptar), Snackbar.LENGTH_LONG)
                                                                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                                                    snack.show()
                                                                }
                                                            } catch (jex: JSONException) {
                                                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_intentelo_mas_tarde), Snackbar.LENGTH_LONG)
                                                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                                                snack.show()
                                                            } catch (ccax: ClassCastException) {
                                                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_intentelo_mas_tarde), Snackbar.LENGTH_LONG)
                                                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                                                snack.show()
                                                            }
                                                        },
                                                        Response.ErrorListener { error ->
                                                            Log.e(LOG,error.message.toString())
                                                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                                                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                                            snack.show()
                                                            listaModuloCursos[posModulo].listaCursos[posCurso]?.cargando = false
                                                            /*updateAdater(posAdapter)*/
                                                            refreshAdapter()
                                                        }
                                                    )
                                                    jsObjectRequest.tag = TAG
                                                    requestQueue?.add(jsObjectRequest)
                                                } else {
                                                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_encriptar), Snackbar.LENGTH_LONG)
                                                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                                    snack.show()
                                                }
                                            } else {
                                                val alerta = AlertDialog.Builder(this)
                                                    .setTitle(resources.getString(R.string.error))
                                                    .setMessage(resources.getString(R.string.error_ingreso))
                                                    .create()
                                                alerta.show()
                                                listaModuloCursos[posModulo].listaCursos[posCurso]?.cargando = false
                                                /*updateAdater(posAdapter)*/
                                                refreshAdapter()
                                            }
                                        }
                                    }
                                }

                                rvCurso_acursospost.adapter = cursoAdapter

                            } else {
                                lblMensaje_acursospost.visibility = View.VISIBLE
                                lblMensaje_acursospost.text = resources.getText(R.string.error_curso_no)
                            }
                        } else {
                            lblMensaje_acursospost.visibility = View.VISIBLE
                            lblMensaje_acursospost.text = resources.getText(R.string.error_desencriptar)
                        }
                    } catch (jex: JSONException) {
                        lblMensaje_acursospost.visibility = View.VISIBLE
                        lblMensaje_acursospost.text = resources.getText(R.string.error_desencriptar)
                    } catch (ccax: ClassCastException) {
                        lblMensaje_acursospost.visibility = View.VISIBLE
                        lblMensaje_acursospost.text = resources.getText(R.string.error_desencriptar)
                    }

                    prbCargando_acursospost.visibility = View.GONE
                },
                Response.ErrorListener { error ->
                    Log.e(LOG, error.message.toString())
                    prbCargando_acursospost.visibility = View.GONE
                    lblMensaje_acursospost.visibility = View.VISIBLE
                    lblMensaje_acursospost.text = resources.getText(R.string.error_no_conexion)
                }
        )
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }


    private fun getAsistenciaCurso (codigoSeccion: String) {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

            val request = JSONObject()
            request.put("CodAlumno", usuario.codigo)
            request.put("CodSeccion", codigoSeccion)

            val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
            Log.i(LOG, requestEncriptado.toString())
            if (requestEncriptado != null)
                onAsistenciaCurso(Utilitarios.getUrl(Utilitarios.URL.ASIS_ALUMNO_POST), requestEncriptado)
            else {
                lblMensaje_acursospost.visibility = View.VISIBLE
                lblMensaje_acursospost.text = resources.getString(R.string.error_encriptar)
            }
        } else {
            lblMensaje_acursospost.visibility = View.VISIBLE
            lblMensaje_acursospost.text = resources.getString(R.string.error_ingreso)
        }
    }


    //ESTE MÉTODO DEVUELVE LA ASISTENCIA EN CADA CURSO
    private fun onAsistenciaCurso(url: String, request: JSONObject) {
        val displaymetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaymetrics)
        anchoPantalla = displaymetrics.widthPixels
        densidad = displaymetrics.density

        Log.i(LOG, url)
        Log.i(LOG, request.toString())

        CustomDialog.instance.showDialogLoad(this)
        requestQueueAsistencia = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    CustomDialog.instance.dialogoCargando?.dismiss()
                    try {
                        val asistenciaJsonPost = Utilitarios.jsObjectDesencriptar(response["ObtenerAsistenciaAlumnoSeccionResult"] as String, this)
                        if (asistenciaJsonPost != null) {
                            val curso = asistenciaJsonPost["Curso"] as String
                            val porcInasistencia = asistenciaJsonPost["PorcxInasistencia"] as Int
                            val cantFaltas = asistenciaJsonPost["CantFalta"] as Double
                            val cantAsistencia = asistenciaJsonPost["CantAsistencias"] as Double
                            val cantTardanza = asistenciaJsonPost["CantTardanzas"] as Double
                            val cantSesiones = asistenciaJsonPost["CantidadSesiones"] as Int
                            val SeccionCodigo = asistenciaJsonPost["SeccionCodigo"] as String

                            val dialogoAsistencia = AsistenciaPostDialog()
                            val bundle = Bundle()

                            bundle.putString("Curso", "[$SeccionCodigo] $curso")
                            bundle.putInt("PorcxInasistencia", porcInasistencia)
                            bundle.putInt("CantFalta", cantFaltas.roundToInt())
                            bundle.putInt("CantAsistencias", cantAsistencia.roundToInt())
                            bundle.putInt("CantTardanzas", cantTardanza.roundToInt())
                            bundle.putInt("CantidadSesiones", cantSesiones)
                            bundle.putInt("AnchoPantalla", anchoPantalla)
                            bundle.putFloat("Densidad", densidad)

                            dialogoAsistencia.arguments = bundle
                            dialogoAsistencia.show(supportFragmentManager, "Asistencia Dialog")
                        } else {
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_desencriptar), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                            snack.show()
                        }
                    } catch (jex: JSONException) {
                        Log.e(LOG, jex.toString())
                    } catch (caas: ClassCastException) {
                        Log.e(LOG, caas.toString())
                    }
                },
                Response.ErrorListener { error ->
                    CustomDialog.instance.dialogoCargando?.dismiss()

                }
        )
        jsObjectRequest.tag = TAG
        requestQueueAsistencia?.add(jsObjectRequest)
    }

    /*private fun updateAdater (position: Int) {
        cursoAdapter?.notifyItemChanged(position)
    }

    private fun removeAdapter (position: Int) {
        cursoAdapter?.notifyItemRemoved(position)
    }

    private fun insertAdapter (position: Int) {
        cursoAdapter?.notifyItemInserted(position)
    }*/


    private fun refreshAdapter () {
        cursoAdapter?.notifyDataSetChanged()
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {

                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CODIGO, codigoPromo)
    }


    override fun onStop() {
        requestQueue?.cancelAll(TAG)
        requestQueueEncuesta?.cancelAll(TAG)
        requestQueueAsistencia?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
        super.onStop()
    }


    override fun onDestroy() {
        if(CustomDialog.instance.dialogoCargando != null){
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
        super.onDestroy()
    }
}
