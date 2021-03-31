package pe.edu.esan.appostgrado.view.mas.laboratorios

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.appcompat.app.AlertDialog
import android.text.Html
import android.text.method.LinkMovementMethod

import android.util.Log
import android.view.*

import android.widget.CheckBox

import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_pregrado_labs_principal.*
import kotlinx.android.synthetic.main.fragment_cursos.view.*

import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.AlumnoPrereservaLab
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken
import pe.edu.esan.appostgrado.view.MenuPrincipalActivity
import java.util.*


class PregradoLabsPrincipalActivity : AppCompatActivity() {

    private var mRequestQueue: RequestQueue? = null

    private val LOG = PregradoLabsPrincipalActivity::class.simpleName

    private var codigoAlumno: String = ""

    private val TAG = "PregPrereservaPrincipalActivity"

    private var URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_LAB_VERIFICAR_ALUMNO_RESERVA)
    private var URL_TEST_VERIFICAR_TYC = Utilitarios.getUrl(Utilitarios.URL.PREG_LAB_VERIFICAR_TIPO_POLITICA)
    private var URL_TEST_ACEPTAR_TYC = Utilitarios.getUrl(Utilitarios.URL.PREG_LAB_ACEPTAR_TIPO_POLITICA)

    private var configuracionID: String = ""

    private var alumnoPrereservaLab: AlumnoPrereservaLab? = null

    private var terminosCondicionesChecked = false

    private var idTipoPolitica = ""

    private var urlForPolicy = ""

    private var hideMenu = true

    private var promocion: String = ""
    private var direccion: String = ""

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_labs_principal)

        setSupportActionBar(my_toolbar_principal_lab)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.laboratorios_title)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        my_toolbar_principal_lab.navigationIcon?.setColorFilter(
            ContextCompat.getColor(this, R.color.md_white_1000),
            PorterDuff.Mode.SRC_ATOP
        )

        my_toolbar_principal_lab.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        realizar_prereserva_opcion_lab.setOnClickListener {
            if (terminosCondicionesChecked) {
                val intent = Intent(this, PregradoLabsHorarioActivity::class.java)
                intent.putExtra("alumno_prereserva_lab", alumnoPrereservaLab)
                intent.putExtra("codigo_alumno", codigoAlumno)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                val snack = Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.debe_aceptar_tyc),
                    Snackbar.LENGTH_SHORT
                )
                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    .setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                snack.show()
            }
        }

        confirmar_prereserva_opcion_lab.setOnClickListener {
            if (terminosCondicionesChecked) {
                val intent = Intent(this, PregradoLabsConfirmacionActivity::class.java)
                intent.putExtra("alumno_prereserva_lab", alumnoPrereservaLab)
                intent.putExtra("codigo_alumno", codigoAlumno)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                val snack = Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.debe_aceptar_tyc),
                    Snackbar.LENGTH_SHORT
                )
                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    .setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                snack.show()
            }
        }

        ver_historial_prereservas_opcion_lab.setOnClickListener {
            if (terminosCondicionesChecked) {
                val intent = Intent(this, PregradoLabsHistorialActivity::class.java)
                intent.putExtra("alumno_prereserva_lab", alumnoPrereservaLab)
                intent.putExtra("codigo_alumno", codigoAlumno)
                intent.putExtra("promocion", promocion)
                intent.putExtra("direccion", direccion)
                intent.putExtra("id_configuracion", configuracionID)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                val snack = Snackbar.make(findViewById(android.R.id.content), getString(R.string.debe_aceptar_tyc), Snackbar.LENGTH_SHORT)
                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                snack.show()
            }
        }

        checkbox_terminos_condiciones.visibility = View.GONE
        checkbox_terminos_condiciones.isChecked = false

        terminosCondicionesChecked = false

        linear_layout_container_lab.visibility = View.GONE
        empty_text_view_lab.visibility = View.GONE
        //progress_bar_principal_labs.visibility = View.GONE
        progress_bar_principal_labs.visibility = View.VISIBLE

    }

    override fun onResume() {
        super.onResume()
        if(ControlUsuario.instance.currentUsuario.size == 1 && ControlUsuario.instance.currentUsuarioGeneral != null){
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
        codigoAlumno = if (ControlUsuario.instance.currentUsuarioGeneral != null) {
            ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPre
        } else {
            ""
        }

        val request = JSONObject()

        request.put("CodAlumno", codigoAlumno)

        verificarAlumnoReservaServicio(URL_TEST, request)
    }


    fun setCheckboxTextClickable(url: String){
        val checkBoxText = getString(R.string.acepto_tyc_checkbox_text_href, url)

        tv_for_checkbox_tyc.text = Html.fromHtml(checkBoxText)
        tv_for_checkbox_tyc.movementMethod = LinkMovementMethod.getInstance()

    }

    fun showTYCConfirmationDialog(){
        val alertDialog = AlertDialog.Builder(this, R.style.EsanAlertDialog)

        alertDialog.setTitle(getString(R.string.acepta_tyc_dialog_title))
        alertDialog.setMessage(getString(R.string.mensaje_tyc_dialog))

        alertDialog.setNegativeButton(getString(R.string.cancelar_dialog_text), DialogInterface.OnClickListener { dialog, which ->
            terminosCondicionesChecked = false
            checkbox_terminos_condiciones.isChecked = false
            dialog.dismiss()
        })

        alertDialog.setPositiveButton(getString(R.string.aceptar_dialog_text),DialogInterface.OnClickListener { dialog, which ->

            val requestTYC = JSONObject()

            val tipoAlumno = (ControlUsuario.instance.currentUsuario[0] as Alumno).tipoAlumno

            if(tipoAlumno == Utilitarios.PRE){
                requestTYC.put("CodUsuario", codigoAlumno)
                requestTYC.put("IdFacultad",2)
                requestTYC.put("Rol", "A")
                requestTYC.put("IdTipoPolitica", idTipoPolitica.toInt())

                var dispositivo: String? = null

                try {
                    val pInfo = packageManager.getPackageInfo(packageName, 0)
                    val version = pInfo.versionName

                    dispositivo = "Android-$version"
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

                requestTYC.put("Dispositivo", dispositivo)

                aceptarTipoPolitica(URL_TEST_ACEPTAR_TYC, requestTYC)
            } else {
                Log.e(LOG, "Error: Tipo de usuario incorrecto")
            }
        })

        alertDialog.setOnCancelListener {
            terminosCondicionesChecked = false
            checkbox_terminos_condiciones.isChecked = false
        }

        alertDialog.show()
    }


    fun onCheckboxClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked
            when (view.id) {
                R.id.checkbox_terminos_condiciones -> {
                    if(checked && !terminosCondicionesChecked){
                        showTYCConfirmationDialog()
                    }
                }
            }
        }
    }


    private fun verificarAlumnoReservaServicio(url: String, request: JSONObject) {

        val fRequest = Utilitarios.jsObjectEncrypted(request, this@PregradoLabsPrincipalActivity)

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            val jsonObjectRequest = object: JsonObjectRequest(
            /*val jsonObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                fRequest,
                { response ->
                    if (!response.isNull("VerificarAlumnoReservaResult")) {
                        val jsResponse = Utilitarios.jsObjectDesencriptar(response.getString("VerificarAlumnoReservaResult"), this@PregradoLabsPrincipalActivity)

                        try {
                            val horasDisp = jsResponse!!.optString("HorasDisp")
                            val mensaje = jsResponse.optString("Mensaje")
                            //True es error y false es respuesta exitosa
                            val indicador = jsResponse.optString("Indicador")
                            val horaIniRes = jsResponse.optString("HoraIniRes")
                            val horaFinRes = jsResponse.optString("HoraFinRes")

                            configuracionID = jsResponse.optString("IdConfiguracion")

                            promocion = jsResponse.optString("Promocion")
                            direccion = jsResponse.optString("TipoMatricula")
                            val horasUsadas = jsResponse.optString("HorasUsadas")
                            val maximaCantidadProgPermitidos = jsResponse.optString("CantProgRes")
                            val horaActual = jsResponse.optString("HoraActual")

                            idTipoPolitica = jsResponse.optString("IdTipoPolitica")

                            //if (mensaje.contains("Usted no se encuentra matrículado en el proceso de matrícula actual.")) {
                            //if (indicador.equals("False")) {
                            if (indicador.toInt() == 0) {

                                alumnoPrereservaLab = AlumnoPrereservaLab(
                                    horasDisp,
                                    mensaje,
                                    indicador,
                                    horaIniRes,
                                    horaFinRes,
                                    configuracionID,
                                    promocion,
                                    direccion,
                                    horasUsadas,
                                    maximaCantidadProgPermitidos,
                                    horaActual
                                )

                                val requestTYC = JSONObject()

                                val tipoAlumno = (ControlUsuario.instance.currentUsuario[0] as Alumno).tipoAlumno

                                if(tipoAlumno == Utilitarios.PRE){
                                    requestTYC.put("CodUsuario", codigoAlumno)
                                    requestTYC.put("IdFacultad",2)
                                    requestTYC.put("Rol", "A")
                                    requestTYC.put("IdTipoPolitica", idTipoPolitica.toInt())

                                    verificarTipoPoliticaPortal(URL_TEST_VERIFICAR_TYC, requestTYC)
                                } else {
                                    Log.e(LOG, "Error: Tipo de usuario incorrecto")
                                }
                            } else {
                                linear_layout_container_lab.visibility = View.GONE
                                progress_bar_principal_labs.visibility = View.GONE
                                empty_text_view_lab.text = mensaje
                                empty_text_view_lab.visibility = View.VISIBLE

                                val language = Locale.getDefault().displayLanguage

                                if(language.equals("English")){
                                    if(mensaje.contains("no se encuentra matr")){
                                        empty_text_view_lab.text = "You are not enrolled in the current semester or these services will be available at the beginning of the semester."
                                    } else {
                                        empty_text_view_lab.text = "An error occurred, please contact ServiceDesk."
                                    }
                                }
                            }

                        } catch (e: Exception) {
                            linear_layout_container_lab.visibility = View.GONE
                            progress_bar_principal_labs.visibility = View.GONE
                            empty_text_view_lab.text = getString(R.string.error_recuperacion_datos)
                            empty_text_view_lab.visibility = View.VISIBLE
                            checkbox_terminos_condiciones.visibility = View.GONE
                        }
                    }
                },
                { error ->
                    if(error.networkResponse.statusCode == 401) {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                verificarAlumnoReservaServicio(url, request)
                            } else {
                                linear_layout_container_lab.visibility = View.GONE
                                progress_bar_principal_labs.visibility = View.GONE
                                empty_text_view_lab.text = getString(R.string.no_respuesta_desde_servidor)
                                empty_text_view_lab.visibility = View.VISIBLE
                                checkbox_terminos_condiciones.visibility = View.GONE
                            }
                        }
                    } else {
                        linear_layout_container_lab.visibility = View.GONE
                        progress_bar_principal_labs.visibility = View.GONE
                        empty_text_view_lab.text = getString(R.string.no_respuesta_desde_servidor)
                        empty_text_view_lab.visibility = View.VISIBLE
                        checkbox_terminos_condiciones.visibility = View.GONE
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

    fun verificarTipoPoliticaPortal(url: String, request: JSONObject) {

        val fRequest = Utilitarios.jsObjectEncrypted(request, this@PregradoLabsPrincipalActivity)

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            val jsonObjectRequest = object: JsonObjectRequest(
            /*val jsonObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                fRequest,
                { response ->
                    if (!response.isNull("VerificarTipoPoliticaPortalResult")) {
                        val jsResponse = Utilitarios.jsObjectDesencriptar(response.getString("VerificarTipoPoliticaPortalResult"), this@PregradoLabsPrincipalActivity)

                        try{
                            val tycAccepted = jsResponse!!.optString("AceptoPolitica")
                            val urlPolitica = jsResponse.optString("UrlPolitica")

                            urlForPolicy = urlPolitica
                            setCheckboxTextClickable(urlForPolicy)

                            //Los tyc han sido aceptados por el usuario
                            if(tycAccepted != "0"){
                                terminosCondicionesChecked = true
                                checkbox_terminos_condiciones.visibility = View.VISIBLE
                                checkbox_terminos_condiciones.isChecked = true
                                checkbox_terminos_condiciones.isEnabled = false
                                CompoundButtonCompat.setButtonTintList(checkbox_terminos_condiciones, ColorStateList.valueOf(Color.parseColor("#757575")))
                                linear_layout_container_lab.visibility = View.VISIBLE
                                empty_text_view_lab.visibility = View.GONE
                                progress_bar_principal_labs.visibility = View.GONE

                            } else {
                                //Los tyc no han sido aceptados por el usuario
                                terminosCondicionesChecked = false
                                linear_layout_container_lab.visibility = View.VISIBLE
                                empty_text_view_lab.visibility = View.GONE
                                checkbox_terminos_condiciones.visibility = View.VISIBLE
                                checkbox_terminos_condiciones.isChecked = false
                                checkbox_terminos_condiciones.isEnabled = true
                                progress_bar_principal_labs.visibility = View.GONE
                            }

                        } catch (e: Exception) {
                            linear_layout_container_lab.visibility = View.GONE
                            empty_text_view_lab.text = getString(R.string.error_recuperacion_datos)
                            empty_text_view_lab.visibility = View.VISIBLE
                            checkbox_terminos_condiciones.visibility = View.GONE
                            progress_bar_principal_labs.visibility = View.GONE
                        }

                    }
                },
                { error ->
                    if(error.networkResponse.statusCode == 401) {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                verificarTipoPoliticaPortal(url, request)
                            } else {
                                linear_layout_container_lab.visibility = View.GONE
                                empty_text_view_lab.text = getString(R.string.no_respuesta_desde_servidor)
                                empty_text_view_lab.visibility = View.VISIBLE
                                checkbox_terminos_condiciones.visibility = View.GONE
                                progress_bar_principal_labs.visibility = View.GONE
                            }
                        }
                    } else {
                        linear_layout_container_lab.visibility = View.GONE
                        empty_text_view_lab.text = getString(R.string.no_respuesta_desde_servidor)
                        empty_text_view_lab.visibility = View.VISIBLE
                        checkbox_terminos_condiciones.visibility = View.GONE
                        progress_bar_principal_labs.visibility = View.GONE
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



    fun aceptarTipoPolitica(url: String, request: JSONObject){

        val fRequest = Utilitarios.jsObjectEncrypted(request, this@PregradoLabsPrincipalActivity)

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            val jsonObjectRequest = object: JsonObjectRequest(
            /*val jsonObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                fRequest,
                { response ->
                    if (!response.isNull("AceptarTipoPoliticaResult")) {
                        val jsResponse = Utilitarios.jsObjectDesencriptar(response.getString("AceptarTipoPoliticaResult"), this@PregradoLabsPrincipalActivity)

                        try{
                            val mensaje = jsResponse!!.optString("Mensaje")
                            val rpta = jsResponse.optString("Rpta")

                            if(rpta.toInt() > 0 ){
                                terminosCondicionesChecked = true
                                linear_layout_container_lab.visibility = View.VISIBLE
                                empty_text_view_lab.visibility = View.GONE
                                progress_bar_principal_labs.visibility = View.GONE
                                checkbox_terminos_condiciones.visibility = View.VISIBLE
                                checkbox_terminos_condiciones.isChecked = true
                                checkbox_terminos_condiciones.isEnabled = false
                                CompoundButtonCompat.setButtonTintList(checkbox_terminos_condiciones, ColorStateList.valueOf(Color.parseColor("#757575")))

                                val snack = Snackbar.make(findViewById(android.R.id.content),getString(R.string.politicas_uso_aceptadas_correctamente), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.success))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.success_text))
                                snack.show()
                            } else {
                                terminosCondicionesChecked = false
                                linear_layout_container_lab.visibility = View.GONE
                                empty_text_view_lab.visibility = View.VISIBLE
                                empty_text_view_lab.text = getString(R.string.error_recuperacion_datos)
                                progress_bar_principal_labs.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            linear_layout_container_lab.visibility = View.GONE
                            empty_text_view_lab.text = getString(R.string.error_recuperacion_datos)
                            empty_text_view_lab.visibility = View.VISIBLE
                            checkbox_terminos_condiciones.visibility = View.GONE
                            progress_bar_principal_labs.visibility = View.GONE
                        }

                    }
                },
                { error ->
                    if(error.networkResponse.statusCode == 401) {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                aceptarTipoPolitica(url, request)
                            } else {
                                linear_layout_container_lab.visibility = View.GONE
                                empty_text_view_lab.text = getString(R.string.no_respuesta_desde_servidor)
                                empty_text_view_lab.visibility = View.VISIBLE
                                progress_bar_principal_labs.visibility = View.GONE
                            }
                        }
                    } else {
                        linear_layout_container_lab.visibility = View.GONE
                        empty_text_view_lab.text = getString(R.string.no_respuesta_desde_servidor)
                        empty_text_view_lab.visibility = View.VISIBLE
                        progress_bar_principal_labs.visibility = View.GONE
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_laboratorios, menu)

        if(hideMenu){
            menu.getItem(0).setVisible(false)
        } else{
            menu.getItem(0).setVisible(true)
        }


        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //Respond to the action bar's Up/Home button
                val intent = Intent(this@PregradoLabsPrincipalActivity, MenuPrincipalActivity::class.java)
                intent.putExtra("back_from_pregrado_prereserva_lab", 0)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                return true
            }
            R.id.tyc_menu_action -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(urlForPolicy)
                startActivity(intent)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(this@PregradoLabsPrincipalActivity, MenuPrincipalActivity::class.java)
            intent.putExtra("back_from_pregrado_prereserva_lab", 0)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        mRequestQueue?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
    }
}
