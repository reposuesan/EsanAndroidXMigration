package pe.edu.esan.appostgrado.view.academico.docente

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.activity_resultado_encuesta.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.ResultadoEncuestaAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.GrupoPregunta
import pe.edu.esan.appostgrado.entidades.ResultadoEncuesta
import pe.edu.esan.appostgrado.entidades.UserEsan
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class ResultadoEncuestaActivity : AppCompatActivity() {

    private val TAG = "ResultadoEncuestaActivity"
    private var requestQueue: RequestQueue? = null
    private var CodigoSeccion: String? = ""
    private var nombreCurso: String? = ""

    private val LOG = ResultadoEncuestaActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado_encuesta)

        val toolbar = main_resultadoencuesta as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.resultado_encuesta)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        if (savedInstanceState == null) {
            val extras = intent.extras
            CodigoSeccion = if (extras != null) extras.getString("CodSeccion") else ""
            nombreCurso = if (extras != null) extras.getString("Curso") else ""
        } else {
            CodigoSeccion = savedInstanceState.getString("CodSeccion")
            nombreCurso = savedInstanceState.getString("Curso")
        }
        lblMensaje_resultadoencuesta.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
        rvResultadoEncuesta.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)

        getResultadoEncuesta(CodigoSeccion)
    }

    private fun getResultadoEncuesta(seccion: String?) {

        if (ControlUsuario.instance.currentUsuario.size == 1) {
           sendRequest(seccion)
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        sendRequest(seccion)
                    }
                }
            )

            controlViewModel.getDataFromRoom()
        }

    }

    private fun sendRequest(seccion: String?){
        val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

        val request = JSONObject()
        request.put("Codigo", usuario.codigo)
        request.put("Seccion", seccion)
        request.put("IdEncuesta", 0)
        request.put("IdProgramacion", 0)

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
        if (requestEncriptado != null) {
            onResultadoEncuestaCabecera(Utilitarios.getUrl(Utilitarios.URL.RESULTADO_ENCUESTA_CABECERA), requestEncriptado)
        } else {
            lblMensaje_resultadoencuesta.visibility = View.VISIBLE
            lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_encriptar)
        }
    }

    private fun onResultadoEncuestaCabecera(url: String, request: JSONObject) {
        prbCargando_resultadoencuesta.visibility = View.VISIBLE
        lblMensaje_resultadoencuesta.visibility = View.GONE
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                url,
                request,
            { response ->
                try {
                    val cabeceraEncuestaJObject = Utilitarios.jsObjectDesencriptar(response["ObtenerEncuestaPorProfesorResult"] as String, this)

                    if (cabeceraEncuestaJObject != null) {
                        val ap = cabeceraEncuestaJObject["APIndiceAprobacion"] as Double
                        val dp = cabeceraEncuestaJObject["DPIndiceDesaprobacion"] as Double
                        val avg = cabeceraEncuestaJObject["PromedioEvaluacion"] as Double

                        val totalPorEvaluar = cabeceraEncuestaJObject["TotalEvaluadores"] as Int
                        val siEvaluaron = cabeceraEncuestaJObject["TotalEvaluaron"] as Int
                        val noEvaluaron = cabeceraEncuestaJObject["TotalNoEvaluaron"] as Int

                        val resultadoEncuesta = ResultadoEncuesta(Utilitarios.TipoFila.CABECERA, resources.getString(R.string.resumen), totalPorEvaluar, siEvaluaron, noEvaluaron, ap, dp, avg)

                        onResultadoEncuestaDetalle(Utilitarios.getUrl(Utilitarios.URL.RESULTADO_ENCUESTA_DETALLE), request, resultadoEncuesta)
                    } else {
                        lblMensaje_resultadoencuesta.text = resources.getString(R.string.no_existe_resultado_encuesta)
                        lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                        prbCargando_resultadoencuesta.visibility = View.GONE
                    }
                } catch (jex: JSONException) {
                    lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_respuesta_server)
                    lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                    prbCargando_resultadoencuesta.visibility = View.GONE
                } catch (cca: ClassCastException) {
                    lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_respuesta_server)
                    lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                    prbCargando_resultadoencuesta.visibility = View.GONE
                }
            },
            { error ->
                when {
                    error is TimeoutError -> {
                        lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_respuesta_server)
                        lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                        prbCargando_resultadoencuesta.visibility = View.GONE
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onResultadoEncuestaCabecera(url, request)
                            } else {
                                lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_respuesta_server)
                                lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                                prbCargando_resultadoencuesta.visibility = View.GONE
                            }
                        }
                    }
                    else -> {
                        lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_respuesta_server)
                        lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                        prbCargando_resultadoencuesta.visibility = View.GONE
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

    private fun onResultadoEncuestaDetalle(url: String, request: JSONObject, resultadoEncuesta: ResultadoEncuesta) {

        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                url,
                request,
            { response ->
                try {
                    val resultadoEncuestaJArray = Utilitarios.jsArrayDesencriptar(response["ListarEncuestaRespuestaPorProfesorResult"] as String, this)
                    if (resultadoEncuestaJArray != null) {
                        if (resultadoEncuestaJArray.length() > 0) {
                            var ultimoGrupo = ""
                            val grupoPreguntas = ArrayList<GrupoPregunta>()
                            for (i in 0 until resultadoEncuestaJArray.length()) {
                                val detalleJObject = resultadoEncuestaJArray[i] as JSONObject
                                val grupo = detalleJObject["GrupoNombre"] as String
                                val pregunta = detalleJObject["Pregunta"] as String
                                val ap = detalleJObject["APIndiceAprobacion"] as Double
                                val dp = detalleJObject["DPIndiceDesaprobacion"] as Double
                                val avg = detalleJObject["AVGPuntaje"] as Double

                                if (ultimoGrupo == "") {
                                    ultimoGrupo = grupo
                                    grupoPreguntas.add(GrupoPregunta(Utilitarios.TipoFila.CABECERA, ultimoGrupo))
                                    grupoPreguntas.add(GrupoPregunta(Utilitarios.TipoFila.DETALLE, pregunta, ap, dp, avg))
                                } else {
                                    if (ultimoGrupo == grupo) {
                                        grupoPreguntas.add(GrupoPregunta(Utilitarios.TipoFila.DETALLE, pregunta, ap, dp, avg))
                                    } else {
                                        ultimoGrupo = grupo
                                        grupoPreguntas.add(GrupoPregunta(Utilitarios.TipoFila.CABECERA, ultimoGrupo))
                                        grupoPreguntas.add(GrupoPregunta(Utilitarios.TipoFila.DETALLE, pregunta, ap, dp, avg))
                                    }
                                }
                            }
                            resultadoEncuesta.preguntas = grupoPreguntas

                            val displaymetrisc = DisplayMetrics()
                            windowManager.defaultDisplay.getMetrics(displaymetrisc)


                            rvResultadoEncuesta.adapter = ResultadoEncuestaAdapter(resultadoEncuesta, displaymetrisc.densityDpi)
                            prbCargando_resultadoencuesta.visibility = View.GONE
                        } else {
                            lblMensaje_resultadoencuesta.text = resources.getString(R.string.no_existe_resultado_encuesta)
                            lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                            prbCargando_resultadoencuesta.visibility = View.GONE
                        }
                    } else {
                        lblMensaje_resultadoencuesta.text = resources.getString(R.string.no_existe_resultado_encuesta)
                        lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                        prbCargando_resultadoencuesta.visibility = View.GONE
                    }
                } catch (jex: JSONException) {
                    lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_respuesta_server)
                    lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                    prbCargando_resultadoencuesta.visibility = View.GONE
                } catch (cce: ClassCastException) {
                    lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_respuesta_server)
                    lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                    prbCargando_resultadoencuesta.visibility = View.GONE
                }
            },
            { error ->
                when {
                    error is TimeoutError -> {
                        lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_respuesta_server)
                        lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                        prbCargando_resultadoencuesta.visibility = View.GONE
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onResultadoEncuestaDetalle(url, request, resultadoEncuesta)
                            } else {
                                lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_respuesta_server)
                                lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                                prbCargando_resultadoencuesta.visibility = View.GONE
                            }
                        }
                    }
                    else -> {
                        lblMensaje_resultadoencuesta.text = resources.getString(R.string.error_respuesta_server)
                        lblMensaje_resultadoencuesta.visibility = View.VISIBLE
                        prbCargando_resultadoencuesta.visibility = View.GONE
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("CodSeccion", CodigoSeccion)
        outState.putString("Curso", nombreCurso)
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
    }
}
