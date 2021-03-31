package pe.edu.esan.appostgrado.view.puntosreunion.postgrado.mireserva

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
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.activity_prmis_reservas.*
import kotlinx.android.synthetic.main.toolbar_menuprincipal.view.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PRMiReservaAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.PRMiReserva
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken
import java.util.ArrayList

class PRMisReservasActivity : AppCompatActivity() {

    private val TAG = "PRMisReservasActivity"
    private var requestQueue : RequestQueue? = null

    private val LOG = PRMisReservasActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prmis_reservas)

        val toolbar = main_prmisreservas as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.mis_reservas)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        if (ControlUsuario.instance.prconfiguracion == null){
            finish()
        } else {
            lblMensaje_prmisreservas.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
            lblMensaje_prmisreservas.visibility = View.GONE
            rvMisReservas.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this)
            rvMisReservas.adapter = null

            getMisReservas()
        }
    }

    private fun getMisReservas() {
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
                val request = JSONObject()
                request.put("CodAlumno", user.codigo)
                request.put("IdConfiguracion", ControlUsuario.instance.prconfiguracion?.idConfiguracion)

                val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                if (requestEncriptado != null) {
                    onMisReservas(Utilitarios.getUrl(Utilitarios.URL.PR_MIS_RESERVAS), requestEncriptado)
                } else {
                    lblMensaje_prmisreservas.visibility = View.VISIBLE
                    lblMensaje_prmisreservas.text = resources.getString(R.string.error_encriptar)
                }
            }
        }
    }

    private fun onMisReservas(url: String, request: JSONObject) {
        prbCargando_prmisreservas.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
            { response ->
                prbCargando_prmisreservas.visibility = View.GONE
                try {

                    if (!response.isNull("ListarReservasxConfiguracionAlumnoResult")) {
                        val datosMisReservasJArray = Utilitarios.jsArrayDesencriptar(response["ListarReservasxConfiguracionAlumnoResult"] as String, this)

                        if (datosMisReservasJArray != null) {
                            if (datosMisReservasJArray.length() > 0) {
                                var ultimaFecha = ""
                                val listaMisReservas = ArrayList<PRMiReserva>()

                                for (z in 0 until datosMisReservasJArray.length()) {
                                    val miReservaJson = datosMisReservasJArray[z] as JSONObject

                                    val fecha = miReservaJson["FechaReserva"] as String
                                    val hInicio = miReservaJson["HoraIni"] as String
                                    val hFin = miReservaJson["HoraFin"] as String
                                    val estado = miReservaJson["ValTabla"] as String
                                    val cubiculo = miReservaJson["NomCubiculo"] as String
                                    val descripcion = miReservaJson["RefUbicacion"] as String

                                    if (ultimaFecha.isEmpty()) {
                                        ultimaFecha = fecha
                                        listaMisReservas.add(PRMiReserva(1, ultimaFecha))
                                        listaMisReservas.add(PRMiReserva(2, ultimaFecha, cubiculo, hInicio, hFin, descripcion, estado))

                                    } else {
                                        if (ultimaFecha == fecha) {
                                            listaMisReservas.add(PRMiReserva(2, ultimaFecha, cubiculo, hInicio, hFin, descripcion, estado))
                                        } else {
                                            ultimaFecha = fecha
                                            listaMisReservas.add(PRMiReserva(1, ultimaFecha))
                                            listaMisReservas.add(PRMiReserva(2, ultimaFecha, cubiculo, hInicio, hFin, descripcion, estado))
                                        }
                                    }
                                }

                                rvMisReservas.adapter = PRMiReservaAdapter(listaMisReservas)
                            } else {
                                lblMensaje_prmisreservas.visibility = View.VISIBLE
                                lblMensaje_prmisreservas.text = resources.getString(R.string.mensaje_prsin_reservas)
                            }
                        } else {
                            lblMensaje_prmisreservas.visibility = View.VISIBLE
                            lblMensaje_prmisreservas.text = resources.getString(R.string.error_desencriptar)
                        }
                    } else {
                        lblMensaje_prmisreservas.visibility = View.VISIBLE
                        lblMensaje_prmisreservas.text = resources.getString(R.string.error_no_conexion)
                    }
                } catch (jex: JSONException) {
                    lblMensaje_prmisreservas.visibility = View.VISIBLE
                    lblMensaje_prmisreservas.text = resources.getString(R.string.error_no_conexion)
                } catch (caax: ClassCastException) {
                    lblMensaje_prmisreservas.visibility = View.VISIBLE
                    lblMensaje_prmisreservas.text = resources.getString(R.string.error_no_conexion)
                }

            },
            { error ->
                if(error.networkResponse.statusCode == 401) {
                    renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onMisReservas(url, request)
                        } else {
                            prbCargando_prmisreservas.visibility = View.GONE
                            lblMensaje_prmisreservas.visibility = View.VISIBLE
                            lblMensaje_prmisreservas.text = resources.getString(R.string.error_no_conexion)
                        }
                    }
                } else {
                    prbCargando_prmisreservas.visibility = View.GONE
                    lblMensaje_prmisreservas.visibility = View.VISIBLE
                    lblMensaje_prmisreservas.text = resources.getString(R.string.error_no_conexion)
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

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
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
}
