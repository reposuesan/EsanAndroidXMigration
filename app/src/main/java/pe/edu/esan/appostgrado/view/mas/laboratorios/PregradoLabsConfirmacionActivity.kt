package pe.edu.esan.appostgrado.view.mas.laboratorios

import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.MenuItem
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_pregrado_labs_confirmacion.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.util.Utilitarios
import java.util.*

class PregradoLabsConfirmacionActivity : AppCompatActivity() {

    private val LOG = PregradoLabsConfirmacionActivity::class.simpleName

    private var mRequestQueue: RequestQueue? = null

    private val TAG = "PregradoCubsConfirmacionActivity"

    private var URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_LAB_CONFIRMAR_PRERESERVA_LABORATORIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_labs_confirmacion)

        setSupportActionBar(my_toolbar_confirmacion_lab)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.laboratorios_title)

        my_toolbar_confirmacion_lab.navigationIcon?.setColorFilter(ContextCompat.getColor(this, R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP)
        my_toolbar_confirmacion_lab.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        val codigoAlumno = intent.getStringExtra("codigo_alumno")

        val request = JSONObject()

        request.put("CodAlumno", codigoAlumno)

        confirmarPrereservaServicio(URL_TEST, request)
    }


    fun confirmarPrereservaServicio(url: String, request: JSONObject){

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
                    if (!response.isNull("ConfirmarPreReservaLaboratorioResult")) {
                        val jsResponse = Utilitarios.jsObjectDesencriptar(response.getString("ConfirmarPreReservaLaboratorioResult"), this@PregradoLabsConfirmacionActivity)

                        Log.i(LOG, jsResponse!!.toString())

                        try {
                            val rpta = jsResponse.optString("Rpta")
                            val mensaje = jsResponse.optString("Mensaje")

                            val esConfirmacionCorrecta= rpta.toInt() > 0

                            mostrarMensajeConfirmacion(esConfirmacionCorrecta, mensaje)

                        } catch (e: Exception) {
                            Log.e(LOG, e.message.toString())
                            tv_mensaje_confirmacion_prereserva_lab.text = getString(R.string.error_recuperacion_datos)
                        }

                    }
                },
                Response.ErrorListener { error ->
                    Log.e(LOG, "Error durante el request de Volley")
                    Log.e(LOG, error.message.toString())
                    tv_mensaje_confirmacion_prereserva_lab.text = getString(R.string.no_respuesta_desde_servidor)
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

    }

    fun mostrarMensajeConfirmacion(exitoConfirmacion: Boolean, mensaje: String) {

        val language = Locale.getDefault().displayLanguage

        if (exitoConfirmacion) {
            tv_mensaje_confirmacion_prereserva_lab.setTextColor(ContextCompat.getColor(this, R.color.green))
            img_confirmacion_reserva_labs.setImageResource(R.drawable.conf_check_img)
            if (language.equals("English")) {
                //English
                tv_mensaje_confirmacion_prereserva_lab.text = "The pre-reservation has been confirmed successfully."
            } else {
                tv_mensaje_confirmacion_prereserva_lab.text = mensaje
            }
        } else {
            if (mensaje.contains("No se encuentra en el rango")) {
                tv_mensaje_confirmacion_prereserva_lab.setTextColor(ContextCompat.getColor(this, R.color.esan_red))
                img_confirmacion_reserva_labs.setImageResource(R.drawable.conf_x_img)
                if (language.equals("English")) {
                    //English
                    tv_mensaje_confirmacion_prereserva_lab.text =
                        "You are not in range for confirmation. Remember that confirmation can be made 10 minutes earlier and 10 minutes after the pre-reservation start time."
                } else {
                    tv_mensaje_confirmacion_prereserva_lab.text = mensaje
                }
            } else if (mensaje.contains("Usted no tiene pre-reservas pendientes")) {
                tv_mensaje_confirmacion_prereserva_lab.setTextColor(ContextCompat.getColor(this, R.color.esan_red))
                img_confirmacion_reserva_labs.setImageResource(R.drawable.conf_x_img)
                if (language.equals("English")) {
                    //English
                    tv_mensaje_confirmacion_prereserva_lab.text =
                        "You do not have pending pre-reservations to confirm for today."
                } else {
                    tv_mensaje_confirmacion_prereserva_lab.text = mensaje
                }
            } else {
                tv_mensaje_confirmacion_prereserva_lab.setTextColor(ContextCompat.getColor(this, R.color.esan_red))
                img_confirmacion_reserva_labs.setImageResource(R.drawable.conf_x_img)
                if (language.equals("English")) {
                    //English
                    tv_mensaje_confirmacion_prereserva_lab.text =
                        "There was an error during the confirmation process. Please contact ServiceDesk support."
                } else {
                    tv_mensaje_confirmacion_prereserva_lab.text = mensaje
                }
            }
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
    }
}
