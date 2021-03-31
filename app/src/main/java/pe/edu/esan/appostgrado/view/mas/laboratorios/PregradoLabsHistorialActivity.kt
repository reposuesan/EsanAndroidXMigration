package pe.edu.esan.appostgrado.view.mas.laboratorios

import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.activity_pregrado_labs_historial.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PregradoPrereservasHistorialAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.PrereservaDetalle
import pe.edu.esan.appostgrado.entidades.UsuarioGeneral
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class PregradoLabsHistorialActivity : AppCompatActivity() {

    private val LOG = PregradoLabsHistorialActivity::class.simpleName

    private var mRequestQueue: RequestQueue? = null

    private val TAG = "PregradoLabsHistorialActivity"

    private var URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_LAB_LISTAR_PRERESERVAS_LAB_X_ALUMNO)

    private var prereservasList = ArrayList<PrereservaDetalle>()

    private var promocion: String? = ""
    private var direccion: String? = ""

    private var configuracionID: String? = ""

    private var usuarioEnSesion: UsuarioGeneral? = null

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_labs_historial)

        setSupportActionBar(my_toolbar_historial_lab)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.laboratorios_title)

        my_toolbar_historial_lab.navigationIcon?.setColorFilter(ContextCompat.getColor(this, R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP)
        my_toolbar_historial_lab.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        recycler_view_historial_lab.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        recycler_view_historial_lab.setHasFixedSize(true)

        progress_bar_historial_lab.visibility = View.VISIBLE
        tv_empty_historial_lab.visibility = View.GONE

        tv_promocion_historial_lab.visibility = View.GONE
        tv_direccion_historial_lab.visibility = View.GONE

        val usuarioActual = intent.getStringExtra("codigo_alumno")

        promocion = intent.getStringExtra("promocion")
        direccion = intent.getStringExtra("direccion")
        configuracionID = intent.getStringExtra("id_configuracion")

        val request = JSONObject()

        request.put("CodAlumno", usuarioActual)
        request.put("IdConfiguracion",configuracionID)

        usuarioEnSesion = ControlUsuario.instance.currentUsuarioGeneral

        if(usuarioEnSesion != null){
            listaPrereservasPorAlumnoServicio(URL_TEST, request)
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        listaPrereservasPorAlumnoServicio(URL_TEST, request)
                    }
                }
            )

            controlViewModel.getDataFromRoom()

            /*tv_empty_historial_lab.visibility = View.VISIBLE
            recycler_view_historial_lab.visibility = View.GONE
            progress_bar_historial_lab.visibility = View.GONE
            tv_promocion_historial_lab.visibility = View.GONE
            tv_direccion_historial_lab.visibility = View.GONE*/

        }

    }


    fun listaPrereservasPorAlumnoServicio(url: String, request: JSONObject) {

        val fRequest = Utilitarios.jsObjectEncrypted(request, this)

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            val jsonObjectRequest = object: JsonObjectRequest(
            /*val jsonObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                fRequest,
                { response ->
                    if (!response.isNull("ListarPreReservasxAlumnoResult")) {
                        val jsResponse = Utilitarios.jsArrayDesencriptar(response.getString("ListarPreReservasxAlumnoResult"), this@PregradoLabsHistorialActivity)

                        try {

                            if(jsResponse!!.length() > 0) {
                                for (i in 0..jsResponse.length() - 1) {

                                    val reservaItem = jsResponse.getJSONObject(i)

                                    val nomMaquina = reservaItem.getString("NomMaquina")
                                    val fechaReserva = reservaItem.getString("FechaReserva")
                                    val horaInicio = reservaItem.getString("HoraIni")
                                    val horaFin = reservaItem.getString("HoraFin")
                                    val valTabla = reservaItem.getString("ValTabla")
                                    val ubicacionNombre = reservaItem.getString("UbicacionNombre")

                                    prereservasList.add(
                                        PrereservaDetalle(
                                            nomMaquina,
                                            "${usuarioEnSesion!!.nombre} ${usuarioEnSesion!!.apellido}",
                                            "$horaInicio - $horaFin",
                                            fechaReserva,
                                            ubicacionNombre,
                                            valTabla,0,"")
                                    )
                                }

                                recycler_view_historial_lab.adapter = PregradoPrereservasHistorialAdapter(prereservasList, true, null, null)
                                tv_empty_historial_lab.visibility = View.GONE
                                recycler_view_historial_lab.visibility = View.VISIBLE
                                progress_bar_historial_lab.visibility = View.GONE
                                tv_promocion_historial_lab.visibility = View.VISIBLE
                                tv_direccion_historial_lab.visibility = View.VISIBLE
                                tv_promocion_historial_lab.text = getString(R.string.promocion_mensaje, promocion)
                                tv_direccion_historial_lab.text = getString(R.string.direccion_mensaje, direccion)
                            } else {
                                tv_empty_historial_lab.visibility = View.VISIBLE
                                recycler_view_historial_lab.visibility = View.GONE
                                progress_bar_historial_lab.visibility = View.GONE
                                tv_promocion_historial_lab.visibility = View.GONE
                                tv_direccion_historial_lab.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            tv_empty_historial_lab.visibility = View.VISIBLE
                            tv_empty_historial_lab.text = getString(R.string.error_recuperacion_datos)
                            recycler_view_historial_lab.visibility = View.GONE
                            progress_bar_historial_lab.visibility = View.GONE
                            tv_promocion_historial_lab.visibility = View.GONE
                            tv_direccion_historial_lab.visibility = View.GONE
                        }

                    }
                },
                { error ->
                    if(error.networkResponse.statusCode == 401) {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                listaPrereservasPorAlumnoServicio(url, request)
                            } else {
                                tv_empty_historial_lab.visibility = View.VISIBLE
                                tv_empty_historial_lab.text = getString(R.string.no_respuesta_desde_servidor)
                                recycler_view_historial_lab.visibility = View.GONE
                                progress_bar_historial_lab.visibility = View.GONE
                                tv_promocion_historial_lab.visibility = View.GONE
                                tv_direccion_historial_lab.visibility = View.GONE
                            }
                        }
                    } else {
                        tv_empty_historial_lab.visibility = View.VISIBLE
                        tv_empty_historial_lab.text = getString(R.string.no_respuesta_desde_servidor)
                        recycler_view_historial_lab.visibility = View.GONE
                        progress_bar_historial_lab.visibility = View.GONE
                        tv_promocion_historial_lab.visibility = View.GONE
                        tv_direccion_historial_lab.visibility = View.GONE
                    }
                }
            )
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            {
                override fun getHeaders(): MutableMap<String, String> {
                    return getHeaderForJWT()
                }
            }

            jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            jsonObjectRequest.tag = TAG

            mRequestQueue?.add(jsonObjectRequest)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //Respond to the action bar's Up/Home button
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        mRequestQueue?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
    }
}
