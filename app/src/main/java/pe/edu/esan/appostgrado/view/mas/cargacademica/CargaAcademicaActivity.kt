package pe.edu.esan.appostgrado.view.mas.cargacademica

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_carga_academica.*
import kotlinx.android.synthetic.main.fragment_cursos.view.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.CargaAcademicaAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.*
import pe.edu.esan.appostgrado.util.MesAgnoDialog
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class CargaAcademicaActivity : AppCompatActivity() {

    private val TAG = "TomarAsistenciaActivity"
    private var requestQueue : RequestQueue? = null

    private var g_mes = 0
    private var g_agno = 0
    private var g_smes = ""

    private val LOG = CargaAcademicaActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carga_academica)

        val toolbar = main_cargacademica as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.carga_academica)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow!!.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        rvHoras_cargacademica.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        lblMensaje_cargacademica.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)

        txtFecha_cargacademica.setOnFocusChangeListener { view, b ->
            if (b) {
                val fechaDialog = MesAgnoDialog()
                fechaDialog.show(supportFragmentManager, "")
                fechaDialog.setOnClickListener(object: MesAgnoDialog.Click{
                    override fun fechaselect(mes: String, m: Int, y: Int) {
                        txtFecha_cargacademica.setText(mes + ", " + y)
                        g_smes = mes
                        g_mes = m
                        g_agno = y
                    }
                })
            }
        }

        txtFecha_cargacademica.setOnClickListener {
            val fechaDialog = MesAgnoDialog()
            fechaDialog.show(supportFragmentManager, "")
            fechaDialog.setOnClickListener(object: MesAgnoDialog.Click{
                override fun fechaselect(mes: String, m: Int, y: Int) {
                    txtFecha_cargacademica.setText(mes + ", " + y)
                    g_smes = mes
                    g_mes = m
                    g_agno = y
                }
            })
        }

        btnConsultar_cargacademica.setOnClickListener {
            if (g_agno != 0 && g_mes != 0) {
                getCargaAcademica()
            }
        }

    }

    private fun getCargaAcademica() {
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
        request.put("codigo", usuario.codigo)
        request.put("periodo", g_agno)
        request.put("mes", g_mes)
        request.put("esAcumulado", if (swAcumulado_cargacademica.isChecked) "1" else "0")

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)

        if (requestEncriptado != null)
            onCargaAcademica(Utilitarios.getUrl(Utilitarios.URL.CARGA_ACADEMICA), requestEncriptado)
        else {
            lblMensaje_cargacademica.visibility = View.VISIBLE
            lblMensaje_cargacademica.text = resources.getString(R.string.error_encriptar)
        }

    }

    private fun onCargaAcademica(url: String, request: JSONObject) {
        rvHoras_cargacademica.visibility = View.GONE
        prbCargando_cargacademica.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->
                try {
                    val cargacademicaJArray = Utilitarios.jsArrayDesencriptar(response["ListarCargaAcademicaResult"] as String, this)
                    if (cargacademicaJArray != null) {
                        if (cargacademicaJArray.length() > 0) {
                            var totalCarga = 0.0
                            val listaCargaAcademica = ArrayList<TipoCargaAcademica>()
                            for (i in 0 until cargacademicaJArray.length()) {
                                val cargacademicaJObject = cargacademicaJArray[i] as JSONObject
                                val tipocarga = cargacademicaJObject["TipoCarga"] as String
                                val totaltipo = cargacademicaJObject["SumaTipoCarga"] as Double
                                val actividadesJArray = cargacademicaJObject["actividadTipo"] as JSONArray
                                val listaActividades = ArrayList<Actividad>()
                                for (j in 0 until actividadesJArray.length()) {
                                    val actividadJObject = actividadesJArray[j] as JSONObject
                                    val actividad = actividadJObject["NombreActividad"] as String
                                    val totalActi = actividadJObject["SumaSesionActividad"] as Double
                                    val detalleActiJArray = actividadJObject["detalleActividad"] as JSONArray
                                    val listaDetalle = ArrayList<ActividadDetalle>()
                                    for (k in 0 until detalleActiJArray.length()) {
                                        val detalleActJObject = detalleActiJArray[k] as JSONObject
                                        val curso = detalleActJObject["CursoNombre"] as String
                                        val fecha = detalleActJObject["Fecha"] as String
                                        val grupo = detalleActJObject["Grupo"] as Int
                                        val promocion = detalleActJObject["PromocionCodigo"] as String
                                        val valorSesion = detalleActJObject["ValorSesion"] as Double
                                        listaDetalle.add(ActividadDetalle(curso, promocion, grupo.toString(), fecha, valorSesion))
                                    }
                                    listaActividades.add(Actividad(actividad, totalActi, false, listaDetalle))
                                }
                                listaCargaAcademica.add(TipoCargaAcademica(tipocarga, totaltipo, listaActividades))
                                totalCarga += totaltipo
                            }
                            lblMensaje_cargacademica.visibility = View.GONE
                            rvHoras_cargacademica.visibility = View.VISIBLE
                            val cargaAcademica = CargaAcademica (g_smes, g_agno.toString(), swAcumulado_cargacademica.isChecked, totalCarga, listaCargaAcademica)
                            rvHoras_cargacademica.adapter = CargaAcademicaAdapter(cargaAcademica)
                        } else {
                            lblMensaje_cargacademica.visibility = View.VISIBLE
                            lblMensaje_cargacademica.text = resources.getText(R.string.advertencia_nocargaacademica)
                        }
                    } else {
                        lblMensaje_cargacademica.visibility = View.VISIBLE
                        lblMensaje_cargacademica.text = resources.getText(R.string.error_desencriptar)
                    }
                } catch (jex: JSONException) {
                    lblMensaje_cargacademica.visibility = View.VISIBLE
                    lblMensaje_cargacademica.text = resources.getText(R.string.error_no_conexion)
                } catch (ccax: ClassCastException) {
                    lblMensaje_cargacademica.visibility = View.VISIBLE
                    lblMensaje_cargacademica.text = resources.getText(R.string.error_no_conexion)
                }
                prbCargando_cargacademica.visibility = View.GONE
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {
                    renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onCargaAcademica(url, request)
                        } else {
                            prbCargando_cargacademica.visibility = View.GONE
                            lblMensaje_cargacademica.visibility = View.VISIBLE
                            lblMensaje_cargacademica.text = resources.getText(R.string.error_no_conexion)
                        }
                    }
                } else {
                    prbCargando_cargacademica.visibility = View.GONE
                    lblMensaje_cargacademica.visibility = View.VISIBLE
                    lblMensaje_cargacademica.text = resources.getText(R.string.error_no_conexion)
                }

            }
        )
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        {
            override fun getHeaders(): MutableMap<String, String> {
                return getHeaderForJWT()
            }
        }
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
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

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
    }
}
