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
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProviders
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_historial_asistencia_profesor.*
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONArray
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.HistoricoAsistenciaProfesorAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.SeccionHistorial
import pe.edu.esan.appostgrado.entidades.UserEsan
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken
import java.util.*
import kotlin.collections.ArrayList

class HistorialAsistenciaProfesorActivity : AppCompatActivity() {

    private val TAG = "HistorialAsistenciaProfesorActivity"
    private var requestQueue: RequestQueue? = null
    private var CodigoSeccion: String? = ""
    private var nombreCurso: String? = ""

    private var adapter : HistoricoAsistenciaProfesorAdapter? = null

    var listaSeccionHistorial = ArrayList<SeccionHistorial>()

    private var siMarcoSeleccionado = false
    private var noMarcoSeleccionado = false
    private var proximasSeccionesSeleccionado = false

    private val LOG = HistorialAsistenciaProfesorActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_asistencia_profesor)

        val toolbar = main_histoasistprof as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.historial_asistencia)
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

        lblMensaje_histoasistprof.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
        lblSeccion_histoasistprof.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
        lblCurso_histoasistprof.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)

        lblSeccion_histoasistprof.text = CodigoSeccion
        lblCurso_histoasistprof.text = nombreCurso

        rvSesiones_histoasistprof.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(
                this,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )
        rvSesiones_histoasistprof.adapter = null

        viewContentDetalle_historialasistencias.visibility = View.GONE

        getHistorialAsistencia(CodigoSeccion)

        viewClickSiMarco_histoasistprof.setOnClickListener {
            if (siMarcoSeleccionado) {
                siMarcoSeleccionado = false
                ((viewClickSiMarco_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.GONE

                viewContentSiMarco_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000))

                adapter = HistoricoAsistenciaProfesorAdapter(listaSeccionHistorial)
                rvSesiones_histoasistprof.adapter = adapter
            } else {
                val listaSesiones = listaSeccionHistorial

                if (listaSesiones.isNotEmpty()) {
                    siMarcoSeleccionado = true
                    noMarcoSeleccionado = false
                    proximasSeccionesSeleccionado = false

                    ((viewClickSiMarco_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.VISIBLE
                    ((viewClickNoMarco_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.GONE
                    ((viewClickProximo_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.GONE

                    viewContentSiMarco_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_grey_80))
                    viewContentNoMarco_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000))
                    viewContentProximas_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000))


                    val nuevaLista = ArrayList<SeccionHistorial>()
                    for (sesion in listaSesiones) {
                        if (sesion.estado == 1) {
                            nuevaLista.add(sesion)
                        }
                    }

                    adapter = HistoricoAsistenciaProfesorAdapter(nuevaLista)
                    rvSesiones_histoasistprof.adapter = adapter
                }
            }
        }

        viewClickNoMarco_histoasistprof.setOnClickListener {
            if (noMarcoSeleccionado) {
                noMarcoSeleccionado = false
                ((viewClickNoMarco_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.GONE

                viewContentNoMarco_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000))

                adapter = HistoricoAsistenciaProfesorAdapter(listaSeccionHistorial)
                rvSesiones_histoasistprof.adapter = adapter
            } else {
                val listaSesiones = listaSeccionHistorial

                if (listaSesiones.isNotEmpty()) {
                    siMarcoSeleccionado = false
                    noMarcoSeleccionado = true
                    proximasSeccionesSeleccionado = false

                    ((viewClickSiMarco_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.GONE
                    ((viewClickNoMarco_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.VISIBLE
                    ((viewClickProximo_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.GONE

                    viewContentSiMarco_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000))
                    viewContentNoMarco_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_grey_80))
                    viewContentProximas_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000))


                    val nuevaLista = ArrayList<SeccionHistorial>()
                    for (sesion in listaSesiones) {
                        if (sesion.estado == 0) {
                            nuevaLista.add(sesion)
                        }
                    }

                    adapter = HistoricoAsistenciaProfesorAdapter(nuevaLista)
                    rvSesiones_histoasistprof.adapter = adapter
                }
            }
        }

        viewClickProximo_histoasistprof.setOnClickListener {
            if (proximasSeccionesSeleccionado) {
                proximasSeccionesSeleccionado = false
                ((viewClickProximo_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.GONE

                viewContentProximas_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000))

                adapter = HistoricoAsistenciaProfesorAdapter(listaSeccionHistorial)
                rvSesiones_histoasistprof.adapter = adapter
            } else {
                val listaSesiones = listaSeccionHistorial

                if (listaSesiones.isNotEmpty()) {
                    siMarcoSeleccionado = false
                    noMarcoSeleccionado = false
                    proximasSeccionesSeleccionado = true

                    ((viewClickSiMarco_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.GONE
                    ((viewClickNoMarco_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.GONE
                    ((viewClickProximo_histoasistprof.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0).visibility = View.VISIBLE

                    viewContentSiMarco_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000))
                    viewContentNoMarco_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000))
                    viewContentProximas_historialasistencias.setBackgroundColor(ContextCompat.getColor(this, R.color.md_grey_80))


                    val nuevaLista = ArrayList<SeccionHistorial>()
                    for (sesion in listaSesiones) {
                        if (sesion.estado == 2) {
                            nuevaLista.add(sesion)
                        }
                    }

                    adapter = HistoricoAsistenciaProfesorAdapter(nuevaLista)
                    rvSesiones_histoasistprof.adapter = adapter
                }
            }
        }
    }

    private fun getHistorialAsistencia(seccion: String?) {
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
        request.put("seccioncodigo", seccion)
        request.put("codigo", usuario.codigo)

        onHistorialAsistencia(Utilitarios.getUrl(Utilitarios.URL.RECOR_ASISTENCIA_PROFESOR), request)
    }

    private fun onHistorialAsistencia(url: String, request: JSONObject) {
        prbCargando_histoasistprof.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->
                try {
                    val recordAsistenciaJArray = response["ListarRecordAsistenciaDocentexSeccionResult"] as JSONArray
                    if (recordAsistenciaJArray.length() > 0 ){
                        var cantidadSiMarco = 0
                        var cantidadNoMarco = 0
                        var cantidadProximo = 0

                        for (r in 0 until recordAsistenciaJArray.length()) {
                            val recordAsistenciaJObject = recordAsistenciaJArray[r] as JSONObject
                            val fecha = recordAsistenciaJObject["FECHA"] as String
                            val hfin = recordAsistenciaJObject["FIN"] as String
                            val hmarco = recordAsistenciaJObject["HORA"] as String
                            val hinicio = recordAsistenciaJObject["INICIO"] as String
                            val marco = recordAsistenciaJObject["MARCOASISTENCIA"] as Int
                            val fechasesion = Utilitarios.getStringToDateddMMyyyyHHmm("$fecha $hfin")
                            var estado = 0
                            if (fechasesion != null) {
                                if (fechasesion.before(Date())) {
                                    if (marco == 1) {
                                        estado = 1
                                        cantidadSiMarco += 1
                                    } else {
                                        estado = 0
                                        cantidadNoMarco += 1
                                    }
                                } else {
                                    if (marco == 1) {
                                        estado = 1
                                        cantidadSiMarco += 1
                                    } else {
                                        cantidadProximo += 1
                                        estado = 2
                                    }
                                }
                            } else {
                                if (marco == 1) {
                                    cantidadSiMarco += 1
                                    estado = 1
                                } else {
                                    cantidadNoMarco += 1
                                    estado = 0
                                }
                            }

                            listaSeccionHistorial.add(SeccionHistorial(fecha, hinicio, hfin, marco, hmarco, estado))
                        }
                        adapter = HistoricoAsistenciaProfesorAdapter(listaSeccionHistorial)
                        rvSesiones_histoasistprof.adapter = adapter
                        getDetalle(listaSeccionHistorial.size, cantidadSiMarco, cantidadNoMarco, cantidadProximo)
                    } else {
                        lblMensaje_histoasistprof.text = resources.getString(R.string.advertencia_nosesiones)
                    }
                } catch (e: Exception) {
                    lblMensaje_histoasistprof.text = resources.getString(R.string.error_respuesta_server)
                }
                prbCargando_histoasistprof.visibility = View.GONE
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {
                    renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onHistorialAsistencia(url, request)
                        } else {
                            prbCargando_histoasistprof.visibility = View.GONE
                            lblMensaje_histoasistprof.text = resources.getString(R.string.error_default)
                        }
                    }
                } else {
                    prbCargando_histoasistprof.visibility = View.GONE
                    lblMensaje_histoasistprof.text = resources.getString(R.string.error_default)
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

    private fun getDetalle(cantSesiones: Int, cantSiMarco: Int, cantNoMarco: Int, cantProximo: Int) {

        labelview00001.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
        labelview00003.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
        labelview00005.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
        labelview00007.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)

        lblSesionesTotales_historialasistencias.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
        lblSiAsistencia_historialasistencias.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
        lblNoAsistencia_historialasistencias.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
        lblProximos_historialasistencias.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)

        lblSesionesTotales_historialasistencias.text = cantSesiones.toString()
        lblSiAsistencia_historialasistencias.text = cantSiMarco.toString()
        lblNoAsistencia_historialasistencias.text = cantNoMarco.toString()
        lblProximos_historialasistencias.text = cantProximo.toString()

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val valor = 10 * displayMetrics.density + .5f
        val anchoTotal = displayMetrics.widthPixels - (valor.toInt() * 2)

        val porcSiMarco = cantSiMarco * 100 / cantSesiones
        val porcNoMarco = cantNoMarco * 100 / cantSesiones
        val porcProximo = cantProximo * 100 / cantSesiones

        val anchoSiMarco = anchoTotal * porcSiMarco / 100
        val anchoNoMarco = anchoTotal * porcNoMarco / 100
        val anchoProximo = anchoTotal * porcProximo / 100

        viewSiAsis_historialasistencias.layoutParams.width = anchoSiMarco
        viewNoAsis_historialasistencias.layoutParams.width = anchoNoMarco
        viewProximo_historialasistencias.layoutParams.width = anchoProximo

        lblPorcSiAsis_historialasistencias.layoutParams.width = if (anchoSiMarco > 130) anchoSiMarco else 130 + (anchoSiMarco * 2)
        lblPorcNoAsis_historialasistencias.layoutParams.width = if (anchoNoMarco > 130) anchoNoMarco else 130 + (anchoNoMarco * 2)
        lblPorcProx_historialasistencias.layoutParams.width = if (anchoProximo > 130) anchoProximo else 130 + (anchoProximo * 2)

        lblPorcSiAsis_historialasistencias.text = "$porcSiMarco %"
        lblPorcNoAsis_historialasistencias.text = "$porcNoMarco %"
        lblPorcProx_historialasistencias.text = "$porcProximo %"

        if (anchoSiMarco > 150)
            lblPorcSiAsis_historialasistencias.setTextColor(ContextCompat.getColor(this, R.color.md_white_1000))
        if (anchoNoMarco > 150)
            lblPorcNoAsis_historialasistencias.setTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        viewContentDetalle_historialasistencias.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("CodSeccion", CodigoSeccion)
        outState.putString("Curso", nombreCurso)
        super.onSaveInstanceState(outState)
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
