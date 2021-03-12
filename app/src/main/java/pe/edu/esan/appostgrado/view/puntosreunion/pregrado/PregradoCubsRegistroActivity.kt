package pe.edu.esan.appostgrado.view.puntosreunion.pregrado

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.text.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_pregrado_cubs_registro.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.GrupoAlumnosPrereserva
import pe.edu.esan.appostgrado.util.Utilitarios
import java.util.*

class PregradoCubsRegistroActivity : AppCompatActivity() {

    private var mRequestQueue : RequestQueue? = null

    private val LOG = PregradoCubsRegistroActivity::class.simpleName

    private var codigosAlumnos: String = ""

    private var usuarioActual: String = ""

    private val TAG = "PregradoCubsRegistroActivity"

    private var URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_PP_VERIFICAR_GRUPO_ALUMNOS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_cubs_registro)

        setSupportActionBar(my_toolbar_registro)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.salas_de_estudio_title)

        my_toolbar_registro.navigationIcon?.setColorFilter(ContextCompat.getColor(this, R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP)
        my_toolbar_registro.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        if(intent.hasExtra("codigo_alumno")){
            usuarioActual = intent.getStringExtra("codigo_alumno")
        }

        main_container_registro_cubs.visibility = View.VISIBLE
        progress_bar_registro_prereserva_pp.visibility = View.GONE
        empty_text_view_registro.visibility = View.GONE


        registro_sgte_button.setOnClickListener {

            val primerCodigo = edit_text_primer_codigo.text.trim().toString()
            val segundoCodigo = edit_text_segundo_codigo.text.trim().toString()

            codigosAlumnos = "$primerCodigo,$segundoCodigo,$usuarioActual,"

            if(!TextUtils.isEmpty(primerCodigo) && !TextUtils.isEmpty(segundoCodigo)){
                if(primerCodigo.length == 8 && segundoCodigo.length == 8){
                    if (!primerCodigo.equals(segundoCodigo)){
                        if(!primerCodigo.equals(usuarioActual) && !segundoCodigo.equals(usuarioActual)){

                            val request = JSONObject()

                            request.put("CodAlumnos", codigosAlumnos)
                            request.put("CodAlumnoCrea",usuarioActual)

                            main_container_registro_cubs.visibility = View.GONE
                            progress_bar_registro_prereserva_pp.visibility = View.VISIBLE
                            empty_text_view_registro.visibility = View.GONE

                            verificarGrupoAlumnosReservaServicio(URL_TEST, request)
                        } else {
                            val snack = Snackbar.make(findViewById(android.R.id.content),getString(R.string.ingrese_codigos_diferentes_usuario_sesion), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()
                        }
                    } else {
                        val snack = Snackbar.make(findViewById(android.R.id.content),getString(R.string.ingrese_codigos_diferentes_mensaje), Snackbar.LENGTH_SHORT)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                        snack.show()
                    }
                } else {
                   val snack = Snackbar.make(findViewById(android.R.id.content),getString(R.string.ingrese_codigos_ocho_digitos_mensaje), Snackbar.LENGTH_SHORT)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                    snack.show()
                }
            } else {
                val snack = Snackbar.make(findViewById(android.R.id.content),getString(R.string.ingrese_codigos_mensaje), Snackbar.LENGTH_SHORT)
                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                snack.show()
            }

        }

        borrar_codigos_button.setOnClickListener {
            edit_text_primer_codigo.text.clear()
            edit_text_segundo_codigo.text.clear()
            edit_text_primer_codigo.requestFocus()
        }

        fab_registro_prereserva.setOnClickListener {
            showDialogFab()
        }

        edit_text_usuario_sesion_codigo.text = SpannableStringBuilder(usuarioActual)
        edit_text_usuario_sesion_codigo.inputType = InputType.TYPE_NULL
        if (Build.VERSION.SDK_INT >= 26) {
            edit_text_usuario_sesion_codigo.focusable = View.NOT_FOCUSABLE
        }

        edit_text_usuario_sesion_codigo.isEnabled = false
        edit_text_usuario_sesion_codigo.isCursorVisible = false
        edit_text_usuario_sesion_codigo.keyListener = null

    }


    fun showDialogFab(){

        val builder = AlertDialog.Builder(this@PregradoCubsRegistroActivity, R.style.EsanAlertDialogInformation)

        builder.setTitle(getString(R.string.indicaciones_de_uso))
            .setMessage(getString(R.string.recordatorio_registro_prereserva))
            .setPositiveButton(getString(R.string.positive_dialog)) { dialog, which ->
            }
            .setOnCancelListener {
            }
            .show()
    }

    fun verificarGrupoAlumnosReservaServicio(url: String, request: JSONObject){

        val fRequest = Utilitarios.jsObjectEncrypted(request, this)

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                fRequest,
                { response ->
                    if (!response.isNull("VerificarGrupoAlumnosReservaResult")) {
                        val jsResponse = Utilitarios.jsObjectDesencriptar(response.getString("VerificarGrupoAlumnosReservaResult"), this@PregradoCubsRegistroActivity)
                        try {

                            val horasDisp = jsResponse!!.getString("HorasDisp")
                            val mensajeRespuesta = jsResponse.getString("Mensaje")
                            val indicador = jsResponse.getString("Indicador")

                            /*
                            if(horasDisp.toInt() > 0) {
                            if(indicador.equals("False")) {
                            */
                            if(indicador.equals("0")) {
                                /*val mensaje = jsResponse.getString("Mensaje")*/
                                main_container_registro_cubs.visibility = View.VISIBLE
                                progress_bar_registro_prereserva_pp.visibility = View.GONE
                                empty_text_view_registro.visibility = View.GONE

                                val horaIniRes = jsResponse.getString("HoraIniRes")
                                val horaFinRes = jsResponse.getString("HoraFinRes")
                                val idConfig = jsResponse.getString("IdConfiguracion")
                                val horaActual = jsResponse.getString("HoraActual")
                                val listIntegrantes = jsResponse.getString("ListAlumnos")

                                val grupoPrereserva = GrupoAlumnosPrereserva(
                                    horasDisp,
                                    mensajeRespuesta,
                                    indicador,
                                    horaIniRes,
                                    horaFinRes,
                                    idConfig,
                                    horaActual
                                )

                                //Si los datos son correctos entonces el usuario procede a la pantalla de horario
                                val intent = Intent(this, PregradoCubsHorarioActivity::class.java)
                                intent.putExtra("codigo_alumno",usuarioActual)
                                intent.putExtra("grupoPrereserva", grupoPrereserva)
                                intent.putExtra("codAlumnos", codigosAlumnos)
                                intent.putExtra("lista_alumnos_integrantes_grupo", listIntegrantes.replace("|","\n"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)

                            } else {
                                //En caso hubiera algún error en los códigos o si la cantidad de horas disponibles es 0
                                main_container_registro_cubs.visibility = View.VISIBLE
                                progress_bar_registro_prereserva_pp.visibility = View.GONE
                                empty_text_view_registro.visibility = View.GONE
                                showDialogWarning(mensajeRespuesta)
                            }


                        } catch (e: Exception ) {
                            main_container_registro_cubs.visibility = View.GONE
                            progress_bar_registro_prereserva_pp.visibility = View.GONE
                            empty_text_view_registro.visibility = View.VISIBLE
                            empty_text_view_registro.text = getString(R.string.error_servidor_extraccion_datos)

                            /*val snack = Snackbar.make(
                                findViewById(android.R.id.content),
                                getString(R.string.error_servidor_extraccion_datos),
                                Snackbar.LENGTH_LONG
                            )
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()*/
                        }

                    }
                },
                { error ->
                    error.printStackTrace()

                    main_container_registro_cubs.visibility = View.GONE
                    progress_bar_registro_prereserva_pp.visibility = View.GONE
                    empty_text_view_registro.visibility = View.VISIBLE
                    empty_text_view_registro.text = getString(R.string.no_respuesta_desde_servidor)

                    /*val showAlertHelper = ShowAlertHelper(this)
                    showAlertHelper.showAlertError(
                        getString(R.string.error),
                        getString(R.string.error_no_conexion),
                        null
                    )*/

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

    fun showDialogWarning(mensaje: String) {

        var mensajeParaDialog = mensaje
        val builder = AlertDialog.Builder(this@PregradoCubsRegistroActivity, R.style.EsanAlertDialog)

        val language = Locale.getDefault().displayLanguage

        if(language.equals("English")){
            if (mensaje.contains("Superando la cantidad máxima")){
                mensajeParaDialog = "The group of students do not have enough hours to make a pre-reservation"
            } else if (mensaje.contains("no se encuentra matr") ){
                mensajeParaDialog = "A student of this group is not enrolled in the current semester"
            } else if (mensaje.contains("no se encuentran matr")){
                mensajeParaDialog = "Some students of this group are not enrolled in the current semester"
            } else if (mensaje.contains("no existen en la base de datos")){
                mensajeParaDialog = "Some students of this group are not enrolled in the current semester"
            } else {
                mensajeParaDialog = "An error occurred, please contact ServiceDesk"
            }
        }

        builder.setTitle(getString(R.string.registro_de_prereserva_title))
            .setMessage(mensajeParaDialog)
            .setPositiveButton(android.R.string.yes) { dialog, which -> }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()

    }

    override fun onStop() {
        super.onStop()
        mRequestQueue?.cancelAll(TAG)
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
}
