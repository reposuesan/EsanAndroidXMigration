package pe.edu.esan.appostgrado.view.mas.mensaje

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_mensaje.*
import kotlinx.android.synthetic.main.fragment_cursos.view.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.MensajeAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.Mensaje
import pe.edu.esan.appostgrado.entidades.Profesor
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class MensajeActivity : AppCompatActivity() {

    private val TAG = "MensajeActivity"

    private val LOG = MensajeActivity::class.simpleName
    private var requestQueue : RequestQueue? = null
    private var requestQueueLeido : RequestQueue? = null
    private var mensajeAdapter: MensajeAdapter ?= null

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensaje)

        val toolbar = main_mensaje as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.mensajes)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        rvMensaje_mensaje.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvMensaje_mensaje.adapter = null
        lblMensaje_mensaje.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        getMensajes()
    }

    private fun getMensajes() {
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
        val usuario = ControlUsuario.instance.currentUsuario[0]

        when(usuario) {
            is Alumno -> {
                val request = JSONObject()

                request.put("Facultad", if (usuario.tipoAlumno == Utilitarios.PRE) "2" else "1")
                request.put("Usuario", usuario.codigo)
                request.put("TotalPorPagina", 20)
                request.put("Pagina", 1)

                val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)

                if (requestEncriptado != null)
                    onMensaje(Utilitarios.getUrl(Utilitarios.URL.MENSAJE), requestEncriptado)
                else {
                    lblMensaje_mensaje.visibility = View.VISIBLE
                    lblMensaje_mensaje.text = resources.getString(R.string.error_encriptar)
                }
            }
            is Profesor -> {
                val request = JSONObject()

                request.put("Facultad", "0")
                request.put("Usuario", usuario.codigo)
                request.put("TotalPorPagina", 10)
                request.put("Pagina", 1)

                val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)

                if (requestEncriptado != null)
                    onMensaje(Utilitarios.getUrl(Utilitarios.URL.MENSAJE), requestEncriptado)
                else {
                    lblMensaje_mensaje.visibility = View.VISIBLE
                    lblMensaje_mensaje.text = resources.getString(R.string.error_encriptar)
                }
            }
            else -> {
                lblMensaje_mensaje.visibility = View.VISIBLE
                lblMensaje_mensaje.text = resources.getString(R.string.error_ingreso)
            }
        }

    }

    private fun onMensaje(url: String, request: JSONObject) {
        prbCargando_mensaje.visibility = View.VISIBLE
        rvMensaje_mensaje.visibility = View.GONE
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->
                try {
                    val mensajeJArray = Utilitarios.jsArrayDesencriptar(response["ListarNotificacionPorUsuarioResult"] as String, this)
                    if (mensajeJArray != null) {
                        if (mensajeJArray.length() > 0) {
                            val ListaMensajes = ArrayList<Mensaje>()
                            for (i in 0 until mensajeJArray.length()) {
                                val mensajeJObject = mensajeJArray[i] as JSONObject
                                val noleido = mensajeJObject["Activo"] as Boolean
                                val mensaje = mensajeJObject["Descripcion"] as String

                                val fechaCreacion = Utilitarios.getStringToStringddMMyyyyHHmmThree(mensajeJObject["FechaCreacion"] as String)
                                val idSeguimiento = mensajeJObject["IdSeguimiento"] as String
                                val titulo = mensajeJObject["Titulo"] as String
                                val urlMensaje = mensajeJObject["URL"] as? String ?: ""

                                ListaMensajes.add(Mensaje(idSeguimiento, titulo, mensaje, fechaCreacion, noleido, urlMensaje))
                            }

                            mensajeAdapter = MensajeAdapter(ListaMensajes) { mensaje, position ->

                                val alertReturn = AlertDialog.Builder(this)
                                    .setTitle(mensaje.titulo)
                                    .setMessage(mensaje.mensaje)
                                    .setPositiveButton(resources.getString(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                                        if (mensaje.noLeido) {
                                            val requestL = JSONObject()
                                            requestL.put("IdSeguimiento", mensaje.key)
                                            val requestEncriptado = Utilitarios.jsObjectEncrypted(requestL, this)

                                            if (requestEncriptado != null)
                                                onMarcarComoLeido(Utilitarios.getUrl(Utilitarios.URL.MENSAJE_MARCARCOMOLEIDO), requestEncriptado, position)
                                        }
                                    })
                                    .setNegativeButton(resources.getString(R.string.cancelar), null)
                                    .create()
                                alertReturn.show()
                                alertReturn.getButton(android.app.Dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.esan_rojo))
                                alertReturn.findViewById<TextView>(android.R.id.message)?.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
                            }
                            rvMensaje_mensaje.visibility = View.VISIBLE
                            rvMensaje_mensaje.adapter = mensajeAdapter
                        } else {
                            lblMensaje_mensaje.visibility = View.VISIBLE
                            lblMensaje_mensaje.text = resources.getText(R.string.advertencia_nocuenta_conmensajes)
                        }
                    } else {
                        lblMensaje_mensaje.visibility = View.VISIBLE
                        lblMensaje_mensaje.text = resources.getText(R.string.error_desencriptar)
                    }
                } catch (jex: JSONException) {
                    lblMensaje_mensaje.visibility = View.VISIBLE
                    lblMensaje_mensaje.text = resources.getText(R.string.error_default)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        lblMensaje_mensaje.setCompoundDrawables(
                            null,
                            resources.getDrawable(R.drawable.ic_refresh_black_24dp),
                            null,
                            null
                        );
                    }
                }
                prbCargando_mensaje.visibility = View.GONE
            },
            { error ->
                if(error is TimeoutError || error.networkResponse == null) {
                    prbCargando_mensaje.visibility = View.GONE
                    lblMensaje_mensaje.visibility = View.VISIBLE
                    lblMensaje_mensaje.text = resources.getText(R.string.error_default)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        lblMensaje_mensaje.setCompoundDrawables(
                            null,
                            resources.getDrawable(R.drawable.ic_refresh_black_24dp),
                            null,
                            null
                        )
                    }
                } else if(error.networkResponse.statusCode == 401) {
                    renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onMensaje(url, request)
                        } else {
                            prbCargando_mensaje.visibility = View.GONE
                            lblMensaje_mensaje.visibility = View.VISIBLE
                            lblMensaje_mensaje.text = resources.getText(R.string.error_default)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                lblMensaje_mensaje.setCompoundDrawables(
                                    null,
                                    resources.getDrawable(R.drawable.ic_refresh_black_24dp),
                                    null,
                                    null
                                )
                            }
                        }
                    }
                } else {
                    prbCargando_mensaje.visibility = View.GONE
                    lblMensaje_mensaje.visibility = View.VISIBLE
                    lblMensaje_mensaje.text = resources.getText(R.string.error_default)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        lblMensaje_mensaje.setCompoundDrawables(
                            null,
                            resources.getDrawable(R.drawable.ic_refresh_black_24dp),
                            null,
                            null
                        )
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

    private fun onMarcarComoLeido(url: String, request: JSONObject, position: Int) {
        requestQueueLeido = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->
                try {
                    val respuesta = Utilitarios.stringDesencriptar(response["DesactivarNotificacionPorIdResult"] as String, this)?.toInt()
                    if (respuesta != null) {
                        if (respuesta > 0) {
                            mensajeAdapter?.actualizarMensajeComoLeido(position)
                        }
                    }
                } catch (jex: JSONException) {

                } catch (caas: ClassCastException) {

                }
            },
            { error ->
                if(error is TimeoutError || error.networkResponse == null) {
                    prbCargando_mensaje.visibility = View.GONE
                    lblMensaje_mensaje.visibility = View.VISIBLE
                    lblMensaje_mensaje.text = resources.getText(R.string.error_default)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        lblMensaje_mensaje.setCompoundDrawables(
                            null,
                            resources.getDrawable(R.drawable.ic_refresh_black_24dp),
                            null,
                            null
                        )
                    }
                } else if(error.networkResponse.statusCode == 401) {
                    renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onMarcarComoLeido(url, request, position)
                        } else {
                            prbCargando_mensaje.visibility = View.GONE
                            lblMensaje_mensaje.visibility = View.VISIBLE
                            lblMensaje_mensaje.text = resources.getText(R.string.error_default)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                lblMensaje_mensaje.setCompoundDrawables(
                                    null,
                                    resources.getDrawable(R.drawable.ic_refresh_black_24dp),
                                    null,
                                    null
                                )
                            }
                        }
                    }
                } else {
                    prbCargando_mensaje.visibility = View.GONE
                    lblMensaje_mensaje.visibility = View.VISIBLE
                    lblMensaje_mensaje.text = resources.getText(R.string.error_default)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        lblMensaje_mensaje.setCompoundDrawables(
                            null,
                            resources.getDrawable(R.drawable.ic_refresh_black_24dp),
                            null,
                            null
                        )
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
        requestQueueLeido?.add(jsObjectRequest)
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
