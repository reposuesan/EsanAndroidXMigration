package pe.edu.esan.appostgrado.view.puntosreunion.postgrado.confirmar

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.view.SurfaceHolder
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.activity_prconfirmar_qr.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken
import java.io.IOException

class PRConfirmarQRActivity : AppCompatActivity() {

    private val TAG = "PRConfirmarQRActivity"
    private var requestQueue: RequestQueue? = null

    private val LOG = PRConfirmarQRActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prconfirmar_qr)


        btnCerrar_prconfirmaqr.setOnClickListener {
            finish()
        }

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        if (ControlUsuario.instance.currentUsuario.size == 1) {
            createCameraSource()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        createCameraSource()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
        }

    }

    private fun createCameraSource() {
        val barcodeDetector = BarcodeDetector.Builder(this).build()
        val cameraSource = CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1600, 1024)
                .build()

        svCamara_prconfirmarqr.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder?) {


                if (ActivityCompat.checkSelfPermission(this@PRConfirmarQRActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                try {
                    cameraSource.start(svCamara_prconfirmarqr.holder)
                } catch (ioe: IOException) {
                    ioe.printStackTrace()
                }

            }

            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object: Detector.Processor<Barcode> {
            override fun release() {

            }

            override fun receiveDetections(p0: Detector.Detections<Barcode>?) {
                val barcodes = p0?.detectedItems
                if (barcodes != null){
                    if (barcodes.size() > 0) {

                        //if (barcodes.size() > 0) {
                        val valor = barcodes.valueAt(0)
                        //cameraSource.stop()
                        //getValorObtenido(barcodes.valueAt(0).displayValue)

                        lblTexto_prconfirmaqr.post(Runnable {
                            cameraSource.stop()
                            getValorObtenido(valor)

                        })

                    }
                }
            }
        })
    }

    private fun getValorObtenido(valorQR: Barcode) {

        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val user = ControlUsuario.instance.currentUsuario[0]
            when (user) {
                is Alumno -> {
                    val request = JSONObject()
                    request.put("CodAlumno", user.codigo)
                    request.put("IdConfiguracion", ControlUsuario.instance.prconfiguracion?.idConfiguracion)
                    request.put("CodQR", valorQR.displayValue)
                    request.put("IdReservaConfirmar", 0)
                    request.put("IdReservaEliminar", 0)

                    val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                    if (requestEncriptado != null) {
                        onConfirmarReservaQR(Utilitarios.getUrl(Utilitarios.URL.PR_CONFIRMAR_RESERVA), requestEncriptado, valorQR.displayValue)
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

    private fun onConfirmarReservaQR(url: String, request: JSONObject, idReservaConfirmar: String) {
        prbCargando_prconfirmarqr.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                url,
                request,
            { response ->
                prbCargando_prconfirmarqr.visibility = View.GONE
                try {
                    if (!response.isNull("ConfirmarReservaAlumnoResult")) {
                        val jsRespuesta = Utilitarios.stringDesencriptar(response["ConfirmarReservaAlumnoResult"] as String, this)
                        if (jsRespuesta != null) {
                            val arrayRespuesta = jsRespuesta.split("|")
                            val idRespuesta = arrayRespuesta[0].toInt()
                            val idReserva = arrayRespuesta[1].toInt()
                            val mensaje = arrayRespuesta[2]

                            if (idRespuesta == 1) {
                                val alertaExito = AlertDialog.Builder(this)
                                        .setTitle(resources.getString(R.string.exito))
                                        .setMessage(mensaje)
                                        .setPositiveButton(resources.getString(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i -> finish() })
                                        .create()
                                alertaExito.show()
                            } else if (idRespuesta == 0) {
                                val alertaMensaje = AlertDialog.Builder(this)
                                        .setTitle(resources.getString(R.string.mensaje))
                                        .setMessage(mensaje)
                                        .setPositiveButton(resources.getString(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i -> finish() })
                                        .create()
                                alertaMensaje.show()
                            } else if (idRespuesta == -1) {
                                val alertaMensaje = AlertDialog.Builder(this)
                                        .setTitle(resources.getString(R.string.mensaje))
                                        .setMessage(mensaje)
                                        .setPositiveButton(resources.getString(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                                            setCambiarReserva(idReserva, idReservaConfirmar)
                                        })
                                        .setNegativeButton(resources.getString(R.string.cancelar), DialogInterface.OnClickListener { dialogInterface, i ->  finish() })
                                        .create()
                                alertaMensaje.show()
                            }

                        } else {
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_desencriptar), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()
                            finish()
                        }
                    }
                } catch (jex: JSONException) {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                    finish()
                }
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {
                    renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onConfirmarReservaQR(url, request, idReservaConfirmar)
                        } else {
                            prbCargando_prconfirmarqr.visibility = View.GONE
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                            snack.show()
                            finish()
                        }
                    }
                } else {
                    prbCargando_prconfirmarqr.visibility = View.GONE
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                    finish()
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

    private fun setCambiarReserva(idReservaEliminar: Int, idReservaConfirmar: String) {

        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val user = ControlUsuario.instance.currentUsuario[0]
            when (user) {
                is Alumno -> {
                    val request = JSONObject()
                    request.put("CodAlumno", user.codigo)
                    request.put("IdConfiguracion", ControlUsuario.instance.prconfiguracion?.idConfiguracion)
                    request.put("CodQR", "")
                    request.put("IdReservaConfirmar", idReservaConfirmar)
                    request.put("IdReservaEliminar", idReservaEliminar)

                    val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                    if (requestEncriptado != null) {
                        onCambiarReserva(Utilitarios.getUrl(Utilitarios.URL.PR_CONFIRMAR_RESERVA), requestEncriptado)
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

    private fun onCambiarReserva(url: String, request: JSONObject) {
        prbCargando_prconfirmarqr.visibility = View.VISIBLE
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                url,
                request,
            { response ->
                prbCargando_prconfirmarqr.visibility = View.GONE
                try {
                    if (!response.isNull("ConfirmarReservaAlumnoResult")) {
                        val jsRespuesta = Utilitarios.stringDesencriptar(response["ConfirmarReservaAlumnoResult"] as String, this)
                        if (jsRespuesta != null) {
                            val arrayRespuesta = jsRespuesta.split("|")
                            val idRespuesta = arrayRespuesta[0].toInt()
                            val mensaje = arrayRespuesta[1]

                            if (idRespuesta == 1) {
                                val alertaExito = AlertDialog.Builder(this)
                                        .setTitle(resources.getString(R.string.exito))
                                        .setMessage(mensaje)
                                        .setPositiveButton(resources.getString(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i -> finish() })
                                        .create()
                                alertaExito.show()
                            } else if (idRespuesta == 0) {
                                val alertaMensaje = AlertDialog.Builder(this)
                                        .setTitle(resources.getString(R.string.mensaje))
                                        .setMessage(mensaje)
                                        .setPositiveButton(resources.getString(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i -> finish() })
                                        .create()
                                alertaMensaje.show()
                            }

                        } else {
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_desencriptar), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()
                            finish()
                        }
                    }
                } catch (jex: JSONException) {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                    finish()
                }
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {
                    renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onCambiarReserva(url, request)
                        } else {
                            prbCargando_prconfirmarqr.visibility = View.GONE
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                            snack.show()
                            finish()
                        }
                    }
                } else {
                    prbCargando_prconfirmarqr.visibility = View.GONE
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                    finish()
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
}
