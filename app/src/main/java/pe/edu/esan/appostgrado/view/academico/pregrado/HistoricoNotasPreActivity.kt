package pe.edu.esan.appostgrado.view.academico.pregrado

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
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
import kotlinx.android.synthetic.main.activity_historico_notas_pre.*
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.HistoricoNotaPreAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.CursosPre
import pe.edu.esan.appostgrado.entidades.UserEsan
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class HistoricoNotasPreActivity : AppCompatActivity() {

    private val TAG = "HistoricoNotasPreActivity"
    private var requestQueue: RequestQueue? = null

    private val LOG = HistoricoNotasPreActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_notas_pre)

        val toolbar = main_historiconotaspre as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.historico_notas)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        lblMensaje_historiconotaspre.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)

        rvCurso_historiconotaspre.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvCurso_historiconotaspre.adapter = null

        getHistoricoNotas()
    }

    private fun getHistoricoNotas() {
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
        val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

        val request = JSONObject()
        request.put("CodAlumno", usuario.codigo)

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
        if (requestEncriptado != null) {
            onHistoricoNotas(Utilitarios.getUrl(Utilitarios.URL.HISTORICO_NOTAS_PRE), requestEncriptado)
        } else {
            lblMensaje_historiconotaspre.text = resources.getText(R.string.error_encriptar)
            lblMensaje_historiconotaspre.visibility = View.VISIBLE
        }
    }

    private fun onHistoricoNotas(url: String, request: JSONObject) {
        prbCargando_historiconotaspre.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->
                try {
                    val historicoJArray = Utilitarios.jsArrayDesencriptar(response["ListarHistoricoNotasAlumnoPreResult"] as String, this)

                    if (historicoJArray != null) {
                        if (historicoJArray.length() > 0) {
                            val listaCursos = ArrayList<CursosPre>()
                            for (i in 0 until historicoJArray.length()) {
                                val historicoJObject = historicoJArray[i] as JSONObject
                                val proceso = historicoJObject["ProcesoCodigo"] as String
                                val curso = historicoJObject["CursoNombre"] as String
                                val promedio = historicoJObject["Promedio"] as? String ?: ""
                                val estado = historicoJObject["PromedioCondicion"] as String
                                val esIngles = historicoJObject["EsIngles"] as Boolean
                                val credito = historicoJObject["CursoCredito"] as String

                                //val esretirado = (retirado.toUpperCase() == "RETIRADO")
                                listaCursos.add(CursosPre(curso, promedio, estado, proceso, proceso.isNotEmpty(), esIngles, credito))
                            }

                            rvCurso_historiconotaspre.adapter = HistoricoNotaPreAdapter(listaCursos)

                        } else {
                            lblMensaje_historiconotaspre.text = resources.getText(R.string.advertencia_no_informacion)
                            lblMensaje_historiconotaspre.visibility = View.VISIBLE
                        }
                    } else {
                        lblMensaje_historiconotaspre.text = resources.getText(R.string.error_desencriptar)
                        lblMensaje_historiconotaspre.visibility = View.VISIBLE
                    }
                } catch (jex: JSONException) {
                    lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                    lblMensaje_historiconotaspre.visibility = View.VISIBLE
                } catch (ccax: ClassCastException) {
                    lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                    lblMensaje_historiconotaspre.visibility = View.VISIBLE
                }
                prbCargando_historiconotaspre.visibility = View.GONE
            },
            { error ->
                when {
                    error is TimeoutError -> {
                        prbCargando_historiconotaspre.visibility = View.GONE
                        lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                        lblMensaje_historiconotaspre.visibility = View.VISIBLE
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onHistoricoNotas(url, request)
                            } else {
                                prbCargando_historiconotaspre.visibility = View.GONE
                                lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                                lblMensaje_historiconotaspre.visibility = View.VISIBLE
                            }
                        }
                    }
                    else -> {
                        prbCargando_historiconotaspre.visibility = View.GONE
                        lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                        lblMensaje_historiconotaspre.visibility = View.VISIBLE
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
