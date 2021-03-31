package pe.edu.esan.appostgrado.view.puntosreunion.pregrado

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_pregrado_cubs_confirmacion.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.helpers.ShowAlertHelper
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken
import java.util.*

class PregradoCubsConfirmacionActivity : AppCompatActivity() {

    private val LOG = PregradoCubsConfirmacionActivity::class.simpleName

    private var mRequestQueue: RequestQueue? = null

    private val TAG = "PregradoCubsConfirmacionActivity"

    private var URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_PP_CONFIRMAR_PRERESERVA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_cubs_confirmacion)

        setSupportActionBar(my_toolbar_confirmacion)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.salas_de_estudio_title)

        my_toolbar_confirmacion.navigationIcon?.setColorFilter(ContextCompat.getColor(this, R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP)
        my_toolbar_confirmacion.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        val codigoAlumno = intent.getStringExtra("codigo_alumno")
        val configuracionId = intent.getStringExtra("configuracionID")

        val request = JSONObject()

        request.put("CodAlumno", codigoAlumno)
        request.put("IdConfiguracion", configuracionId)
        request.put("CodQR", "WEB")

        confirmarPrereservaServicio(URL_TEST, request)

    }

    fun confirmarPrereservaServicio(url: String, request: JSONObject) {

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
                    if (!response.isNull("ConfirmarPreReservaResult")) {
                        val jsResponse = Utilitarios.jsObjectDesencriptar(
                            response.getString("ConfirmarPreReservaResult"),
                            this@PregradoCubsConfirmacionActivity
                        )

                        try {
                            val codRespuesta = jsResponse!!.optString("CodRespuesta")
                            val mensaje = jsResponse.optString("Mensaje")
                            val idReservaRes = jsResponse.optString("IdReservaRes")

                            val confirmacionCorrecta= codRespuesta.toInt() > 0

                            mostrarMensajeConfirmacion(confirmacionCorrecta, mensaje)

                        } catch (e: Exception) {
                            tv_mensaje_confirmacion_prereserva_pp.text =
                                getString(R.string.error_servidor_extraccion_datos)
                            val snack = Snackbar.make(
                                findViewById(android.R.id.content),
                                getString(R.string.error_servidor_extraccion_datos),
                                Snackbar.LENGTH_LONG
                            )
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()
                        }

                    }
                },
                { error ->
                    if(error.networkResponse.statusCode == 401) {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                confirmarPrereservaServicio(url, request)
                            } else {
                                val showAlertHelper = ShowAlertHelper(this)
                                showAlertHelper.showAlertError(
                                    getString(R.string.error),
                                    getString(R.string.error_no_conexion),
                                    null
                                )
                            }
                        }
                    } else {
                        val showAlertHelper = ShowAlertHelper(this)
                        showAlertHelper.showAlertError(
                            getString(R.string.error),
                            getString(R.string.error_no_conexion),
                            null
                        )
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

    fun mostrarMensajeConfirmacion(exitoConfirmacion: Boolean, mensaje: String){

        val language = Locale.getDefault().displayLanguage

        if(exitoConfirmacion){
            tv_mensaje_confirmacion_prereserva_pp.setTextColor(ContextCompat.getColor(this, R.color.green))
            img_confirmacion_reserva_pp.setImageResource(R.drawable.conf_check_img)
            if(language.equals("English")){
                //English
                tv_mensaje_confirmacion_prereserva_pp.text = "The pre-reservation has been confirmed correctly."
            } else {
                tv_mensaje_confirmacion_prereserva_pp.text = mensaje
            }
        } else {
            if(mensaje.contains("No se encuentra en el rango")){
                tv_mensaje_confirmacion_prereserva_pp.setTextColor(ContextCompat.getColor(this, R.color.esan_red))
                img_confirmacion_reserva_pp.setImageResource(R.drawable.conf_x_img)
                if(language.equals("English")){
                    //English
                    tv_mensaje_confirmacion_prereserva_pp.text = "You are not in range for confirmation. Remember that confirmation can be made 10 minutes earlier and 10 minutes after the pre-reservation start time."
                } else {
                    tv_mensaje_confirmacion_prereserva_pp.text = mensaje
                }
            } else if(mensaje.contains("Usted no tiene Pre-reservas pendientes") || mensaje.contains("Usted no tiene pre-reservas pendientes")){
                tv_mensaje_confirmacion_prereserva_pp.setTextColor(ContextCompat.getColor(this, R.color.esan_red))
                img_confirmacion_reserva_pp.setImageResource(R.drawable.conf_x_img)
                if(language.equals("English")){
                    //English
                    tv_mensaje_confirmacion_prereserva_pp.text = "You do not have pending pre-reservations to confirm for today."
                } else {
                    tv_mensaje_confirmacion_prereserva_pp.text = mensaje
                }
            } else {
                tv_mensaje_confirmacion_prereserva_pp.setTextColor(ContextCompat.getColor(this, R.color.esan_red))
                img_confirmacion_reserva_pp.setImageResource(R.drawable.conf_x_img)
                if(language.equals("English")){
                    //English
                    tv_mensaje_confirmacion_prereserva_pp.text = "There was an error during the confirmation process. Please contact ServiceDesk support."
                } else {
                    tv_mensaje_confirmacion_prereserva_pp.text = mensaje
                }
            }
        }


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
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
