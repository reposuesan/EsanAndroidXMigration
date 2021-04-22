package pe.edu.esan.appostgrado.view.puntosreunion.postgrado.confirmar

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.activity_prconfirmar.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class PRConfirmarActivity : AppCompatActivity() {

    private val TAG = "PRConfirmarActivity"
    private var requestQueue : RequestQueue? = null

    private val LOG = PRConfirmarActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prconfirmar)

        val toolbar = main_prconfirmar as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.confirmar_reserva)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

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
        if (ControlUsuario.instance.prconfiguracion == null){
            finish()
        } else {

            lblMensajeConfirmador_prconfirmar.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.MEDIUM)
            lblMensaje_prconfirmar.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)

            getConfirmarReserva()
        }
    }

    private fun getConfirmarReserva() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val user = ControlUsuario.instance.currentUsuario[0]
            when (user) {
                is Alumno -> {
                    val request = JSONObject()
                    request.put("CodAlumno", user.codigo)
                    request.put("IdConfiguracion", ControlUsuario.instance.prconfiguracion?.idConfiguracion)
                    request.put("CodQR", if (user.tipoAlumno == Utilitarios.PRE) "MI" else "WEB")
                    request.put("IdReservaConfirmar", 0)
                    request.put("IdReservaEliminar", 0)

                    val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                    if (requestEncriptado != null) {
                        onConfirmarReserva(Utilitarios.getUrl(Utilitarios.URL.PR_CONFIRMAR_RESERVA), requestEncriptado, user.tipoAlumno)
                    } else {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_encriptar), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                }
            }
        } else {
            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_ingreso), Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
            snack.show()
        }
    }

    private fun onConfirmarReserva(url: String, request: JSONObject, tipoAlumno: String) {
        prbCargando_prconfirmar.visibility = View.VISIBLE
        viewContenedor_prconfirmar.visibility = View.GONE

        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
            { response ->
                prbCargando_prconfirmar.visibility = View.GONE
                try {
                    if (!response.isNull("ConfirmarReservaAlumnoResult")) {
                        val jsRespuesta = Utilitarios.stringDesencriptar(response["ConfirmarReservaAlumnoResult"] as String, this)
                        if (jsRespuesta != null) {
                            val arrayRespuesta = jsRespuesta.split("|")
                            val idRespuesta = arrayRespuesta[0].toInt()
                            val idReserva = arrayRespuesta[1].toInt()
                            val mensaje = arrayRespuesta[2]
                            if (idRespuesta == 1) {
                                lblMensaje_prconfirmar.text = mensaje
                                lblMensaje_prconfirmar.setTextColor(Color.rgb(0,130, 255))
                                if (tipoAlumno == Utilitarios.PRE) {
                                    if (idReserva > 0) {
                                        viewContenedor_prconfirmar.visibility = View.VISIBLE
                                        lblMensajeConfirmador_prconfirmar.text = resources.getString(R.string.mensaje_confirmar_qr)

                                        btnScanQr_prconfirmar.visibility = View.VISIBLE
                                        imgCodeQr_prconfirmar.visibility = View.VISIBLE

                                        val urlQR = Utilitarios.getQRUrl(400, idReserva.toString())
                                        Glide.with(this)
                                                .load(urlQR)
                                                .into(object:SimpleTarget<Drawable>() {

                                                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                                        imgCodeQr_prconfirmar.setImageDrawable(resource)
                                                    }

                                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                                        super.onLoadFailed(errorDrawable)
                                                        lblMensajeConfirmador_prconfirmar.text = resources.getString(R.string.error_cargaqr)
                                                    }
                                                })
                                    }
                                }
                            } else if (idRespuesta == 0) {
                                lblMensaje_prconfirmar.text = mensaje
                                lblMensaje_prconfirmar.setTextColor(Color.BLACK)

                                if (tipoAlumno == Utilitarios.PRE) {
                                    if (idReserva == 0) {
                                        viewContenedor_prconfirmar.visibility = View.VISIBLE
                                        lblMensajeConfirmador_prconfirmar.text = resources.getString(R.string.mensaje_confirmar_porqr)

                                        btnScanQr_prconfirmar.visibility = View.VISIBLE
                                        imgCodeQr_prconfirmar.visibility = View.GONE

                                        btnScanQr_prconfirmar.setOnClickListener {
                                            val intentConfirmarQR = Intent(this, PRConfirmarQRActivity::class.java)
                                            intentConfirmarQR.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intentConfirmarQR)
                                        }
                                    } else {
                                        viewContenedor_prconfirmar.visibility = View.VISIBLE
                                        lblMensajeConfirmador_prconfirmar.text = resources.getString(R.string.mensaje_confirmar_qr)

                                        btnScanQr_prconfirmar.visibility = View.GONE
                                        imgCodeQr_prconfirmar.visibility = View.VISIBLE

                                        val urlQR = Utilitarios.getQRUrl(400, idReserva.toString())
                                        Glide.with(this)
                                                .load(urlQR)
                                                .into(object:SimpleTarget<Drawable>() {
                                                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                                        imgCodeQr_prconfirmar.setImageDrawable(resource)
                                                    }

                                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                                        super.onLoadFailed(errorDrawable)
                                                        lblMensajeConfirmador_prconfirmar.text = resources.getString(R.string.error_cargaqr)
                                                    }
                                                })
                                    }
                                }
                            }
                        } else {
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_desencriptar), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()
                        }
                    } else {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                } catch (jex: JSONException) {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                } catch (caax : ClassCastException) {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
            },
            { error ->
                when {
                    error is TimeoutError || error.networkResponse == null -> {
                        prbCargando_prconfirmar.visibility = View.GONE
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onConfirmarReserva(url, request, tipoAlumno)
                            } else {
                                prbCargando_prconfirmar.visibility = View.GONE
                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        }
                    }
                    else -> {
                        prbCargando_prconfirmar.visibility = View.GONE
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
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

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
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
}
