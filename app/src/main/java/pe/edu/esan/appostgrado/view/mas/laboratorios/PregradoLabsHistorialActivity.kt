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
import kotlinx.android.synthetic.main.activity_pregrado_labs_historial.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PregradoPrereservasHistorialAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.PrereservaDetalle
import pe.edu.esan.appostgrado.entidades.UsuarioGeneral
import pe.edu.esan.appostgrado.util.Utilitarios

class PregradoLabsHistorialActivity : AppCompatActivity() {

    private val LOG = PregradoLabsHistorialActivity::class.simpleName

    private var mRequestQueue: RequestQueue? = null

    private val TAG = "PregradoLabsHistorialActivity"

    private var URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_LAB_LISTAR_PRERESERVAS_LAB_X_ALUMNO)

    private var prereservasList = ArrayList<PrereservaDetalle>()

    private var promocion: String = ""
    private var direccion: String = ""

    private var configuracionID: String = ""

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
                        Log.w(LOG, "operationFinishedActivityPublic.observe() was called")
                        Log.w(LOG, "mainSetup() was called")
                        listaPrereservasPorAlumnoServicio(URL_TEST, request)
                    }
                }
            )

            controlViewModel.getDataFromRoom()
            Log.w(LOG, "controlViewModel.getDataFromRoom() was called")

            /*tv_empty_historial_lab.visibility = View.VISIBLE
            recycler_view_historial_lab.visibility = View.GONE
            progress_bar_historial_lab.visibility = View.GONE
            tv_promocion_historial_lab.visibility = View.GONE
            tv_direccion_historial_lab.visibility = View.GONE
            Crashlytics.log(Log.ERROR,"empty_object","currentUsuarioGeneral is empty")*/

        }

    }


    fun listaPrereservasPorAlumnoServicio(url: String, request: JSONObject) {

        Log.i(LOG, url)

        Log.i(LOG, request.toString())

        val fRequest = Utilitarios.jsObjectEncrypted(request, this)

        Log.i(LOG, fRequest.toString())

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                fRequest,
                Response.Listener { response ->
                    Log.i(LOG, response.toString())
                    if (!response.isNull("ListarPreReservasxAlumnoResult")) {
                        val jsResponse = Utilitarios.jsArrayDesencriptar(response.getString("ListarPreReservasxAlumnoResult"), this@PregradoLabsHistorialActivity)

                        Log.i(LOG, jsResponse!!.toString())

                        try {
                            Log.i(LOG, "try block")

                            if(jsResponse.length() > 0) {
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
                            Log.e(LOG, e.message.toString())
                            tv_empty_historial_lab.visibility = View.VISIBLE
                            tv_empty_historial_lab.text = getString(R.string.error_recuperacion_datos)
                            recycler_view_historial_lab.visibility = View.GONE
                            progress_bar_historial_lab.visibility = View.GONE
                            tv_promocion_historial_lab.visibility = View.GONE
                            tv_direccion_historial_lab.visibility = View.GONE
                        }

                    }
                },
                Response.ErrorListener { error ->
                    Log.e(LOG, "Error durante el request de Volley")
                    Log.e(LOG, error.message.toString())
                    tv_empty_historial_lab.visibility = View.VISIBLE
                    tv_empty_historial_lab.text = getString(R.string.no_respuesta_desde_servidor)
                    recycler_view_historial_lab.visibility = View.GONE
                    progress_bar_historial_lab.visibility = View.GONE
                    tv_promocion_historial_lab.visibility = View.GONE
                    tv_direccion_historial_lab.visibility = View.GONE
                }
            )

            jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            jsonObjectRequest.tag = TAG

            mRequestQueue?.add(jsonObjectRequest)
        }

        progress_bar_historial_lab.visibility = View.GONE
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
