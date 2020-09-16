package pe.edu.esan.appostgrado.view.puntosreunion.pregrado


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
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
import kotlinx.android.synthetic.main.activity_pregrado_cubs_horario.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PregradoPrereservaHorarioAdapter
import pe.edu.esan.appostgrado.entidades.GrupoAlumnosPrereserva
import pe.edu.esan.appostgrado.entidades.PrereservaHorario
import pe.edu.esan.appostgrado.util.Utilitarios
import java.text.SimpleDateFormat
import java.util.*

class PregradoCubsHorarioActivity : AppCompatActivity(), PregradoPrereservaHorarioAdapter.HorarioListener {

    private var listHorasSeleccionadas = HashMap<String, String>()

    private val LOG = PregradoCubsHorarioActivity::class.simpleName

    private var horasPermitidas: Int = 0

    private var codigosAlumnos: String? = null

    private var mRequestQueue: RequestQueue? = null

    private val TAG = "PregradoCubsHorarioActivity"

    private val URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_PP_REGISTRA_PRERESERVA)

    private var horaInicioPrereserva: String = ""
    private var horaFinPrereserva: String = ""

    private var horaInicioPantalla: String = ""
    private var horaFinPantalla: String = ""

    private var horaInicioRespuestaServidor: Int = -1
    private var horaFinRespuestaServidor: Int = -1

    private var horaActualRespuestaServidor: Int = -1

    private val horarioList = ArrayList<PrereservaHorario>()

    private var fechaPrereserva: String = ""

    private var listaAlumnosIntegrantes = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_cubs_horario)

        setSupportActionBar(my_toolbar_horario)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.salas_de_estudio_title)

        my_toolbar_horario.navigationIcon?.setColorFilter(
            ContextCompat.getColor(this, R.color.md_white_1000),
            PorterDuff.Mode.SRC_ATOP
        )
        my_toolbar_horario.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        val grupoPrereserva = intent.getParcelableExtra<GrupoAlumnosPrereserva>("grupoPrereserva")

        horasPermitidas = grupoPrereserva.horasDisp.toInt()
        horaInicioRespuestaServidor = grupoPrereserva.horaIniRes.toInt()
        horaFinRespuestaServidor = grupoPrereserva.horaFinRes.toInt()
        horaActualRespuestaServidor = grupoPrereserva.horaActual.toInt()

        //DELETE LATER
        //horaActualRespuestaServidor = 22

        listaAlumnosIntegrantes = intent.getStringExtra("lista_alumnos_integrantes_grupo")

        codigosAlumnos = intent.getStringExtra("codAlumnos")

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val date = java.util.Date()
        fechaPrereserva = simpleDateFormat.format(date)

        tv_fecha_prereserva.text = getString(R.string.fecha_de_prereserva_text, fechaPrereserva)

        val language = Locale.getDefault().displayLanguage

        Log.i(LOG, language)

        var mensaje = grupoPrereserva.mensajeVerificacion
        val horasDisp = grupoPrereserva.horasDisp.toInt()

        if(language.equals("English")){
            //English
            if (mensaje.contains("alumnos ingresados son correctos")){
                mensaje = "The entered student codes can proceed to pre-reserve."
            } else if (mensaje.contains("El alumno con")){
                mensaje = "Some students of this group have spent an amount of their available daily hours already. For details, please check their records."
            } else if (mensaje.contains("Verificar que los códigos")) {
                mensaje = "Validate that the codes entered are correct before proceeding."
            } else{
                mensaje = ""
            }
            tv_mensaje_disponibilidad_horas.text = getString(R.string.mensaje_horario_uso_y_disponibilidad, mensaje, horasDisp)
        } else {
            //Spanish
            tv_mensaje_disponibilidad_horas.text = getString(R.string.mensaje_horario_uso_y_disponibilidad, mensaje, horasDisp)
        }

        ver_integrantes_button.setOnClickListener { showIntegrantesDialog() }

        crear_grupo_y_prereservar_button.setOnClickListener {

            if (listHorasSeleccionadas.count() == 0) {
                val snack = Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.debe_seleccionar_cantidad_horas_minima_mensaje),
                    Snackbar.LENGTH_LONG
                )
                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                snack.show()
            } else {
                determinarHoraInicioHoraFinParaRequest()

                val request = JSONObject()

                val cantHoras = listHorasSeleccionadas.count()
                val idConfig = grupoPrereserva.idConfig.toInt()

                var dispositivo: String? = null

                val codigoAlumno = intent.getStringExtra("codigo_alumno")

                try {
                    val pInfo = packageManager.getPackageInfo(packageName, 0)
                    val version = pInfo.versionName

                    dispositivo = "Android-$version"
                } catch (e: PackageManager.NameNotFoundException) {

                }

                request.put("FechaReserva", fechaPrereserva)
                request.put("HoraInicio", horaInicioPrereserva)
                request.put("HoraFin", horaFinPrereserva)
                request.put("CantHoras", cantHoras)
                request.put("CodAlumno", codigoAlumno)
                request.put("CodAlumnos", codigosAlumnos)
                request.put("EstEmergencia", 0)
                request.put("IdConfiguracion", idConfig)
                request.put("TipoCubiculo", 0)
                request.put("Dispositivo", dispositivo)

                progress_bar_horario_cubs.visibility = View.VISIBLE
                main_container_horario_cubs.visibility = View.GONE
                empty_text_view_horario.visibility = View.GONE

                registraPrereservaServicio(URL_TEST, request)

            }
        }

        recycler_view_horario.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(this, 2)
        recycler_view_horario.setHasFixedSize(true)

        generarListaHorarios()

        recycler_view_horario.adapter = PregradoPrereservaHorarioAdapter(horarioList, horasPermitidas, this)


        crear_grupo_y_prereservar_button.visibility = View.GONE

        progress_bar_horario_cubs.visibility = View.GONE
        main_container_horario_cubs.visibility = View.VISIBLE
        empty_text_view_horario.visibility = View.GONE

    }

    fun showIntegrantesDialog(){
        val builder = AlertDialog.Builder(this@PregradoCubsHorarioActivity, R.style.EsanAlertDialogInformation)

        builder.setTitle(getString(R.string.integrantes_cub))
            .setMessage(listaAlumnosIntegrantes)
            .setPositiveButton(getString(R.string.positive_dialog)) { dialog, which ->
            }
            .setOnCancelListener {
            }
            .show()
    }

    fun generarListaHorarios() {

        if (horaActualRespuestaServidor < horaInicioRespuestaServidor) {
            //LISTAR TODAS LAS HORAS
            generarListaSegundoFormato(7, 23)
        } else {
            //LISTAR DESDE LA HORA ACTUAL + 1
            generarListaSegundoFormato(horaActualRespuestaServidor + 1, 23)
        }

    }

    fun generarListaSegundoFormato(inicio: Int, fin: Int) {

        if(inicio > 22){
            horarioList.clear()
        } else {
            for (i in inicio..(fin - 1)) {
                horarioList.add(PrereservaHorario(determinarHoraParaScreenSegundoFormato(i), determinarHoraParaScreenSegundoFormato(i + 1), false))
            }
        }
    }


    fun registraPrereservaServicio(url: String, request: JSONObject) {

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
                    if (!response.isNull("RegistraPreReservaResult")) {
                        val jsResponse = Utilitarios.jsObjectDesencriptar(
                            response.getString("RegistraPreReservaResult"),
                            this@PregradoCubsHorarioActivity
                        )
                        Log.i(LOG, jsResponse!!.toString())
                        try {
                            val mensaje = jsResponse.getString("Mensaje")
                            val idCubiculo = jsResponse.getString("IdCubiculo")

                            val success = idCubiculo.toInt() > 0

                            showWarningOrSuccessMessage(mensaje, success)

                        } catch (e: Exception) {
                            /*val snack = Snackbar.make(
                                findViewById(android.R.id.content),
                                getString(R.string.error_servidor_extraccion_datos),
                                Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()*/
                            progress_bar_horario_cubs.visibility = View.GONE
                            main_container_horario_cubs.visibility = View.GONE
                            empty_text_view_horario.visibility = View.VISIBLE
                            empty_text_view_horario.text = getString(R.string.error_servidor_extraccion_datos)
                        }
                    }
                },
                Response.ErrorListener { error ->
                    Log.e(LOG, "Error durante el request de Volley")
                    Log.e(LOG, error.message.toString())

                    /*val showAlertHelper = ShowAlertHelper(this)
                    showAlertHelper.showAlertError(
                        getString(R.string.error),
                        getString(R.string.error_no_conexion),
                        null
                    )*/
                    progress_bar_horario_cubs.visibility = View.GONE
                    main_container_horario_cubs.visibility = View.GONE
                    empty_text_view_horario.visibility = View.VISIBLE
                    empty_text_view_horario.text = getString(R.string.no_respuesta_desde_servidor)

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

    fun showWarningOrSuccessMessage(mensaje: String, successfulResponse: Boolean){

        progress_bar_horario_cubs.visibility = View.GONE
        main_container_horario_cubs.visibility = View.VISIBLE
        empty_text_view_horario.visibility = View.GONE

        if(successfulResponse){
            val intent = Intent(this, PregradoCubsDetalleActivity::class.java)
            intent.putExtra("mensaje_prereserva_realizada", mensaje)
            intent.putExtra("rango_horario", "$horaInicioPantalla - $horaFinPantalla")
            intent.putExtra("fecha", fechaPrereserva)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        } else {
            var errorMensaje: String = mensaje
            val language = Locale.getDefault().displayLanguage

            if(language.equals("English")) {
                //English
                if (mensaje.contains("se cruza con otra pre-reserva de su grupo")) {
                    errorMensaje = "The hours selected are already in use in another pre-reservation. Please check the record section in the study rooms main screen for details."
                } else if (mensaje.contains("Ocurrió un error")) {
                    errorMensaje = "An error occurred processing your request."
                } else if (mensaje.contains("se cruza con otra pre")){
                    errorMensaje = "The hours selected are already in use, therefore they are not available to pre-reserve."
                } else {
                    errorMensaje = "An error occurred. Please contact ServiceDesk support."
                }
            }

            val snack = Snackbar.make(findViewById(android.R.id.content), errorMensaje, Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 3
            snack.show()
        }
    }

    fun determinarHoraInicioHoraFinParaRequest() {

        var minKey: Int = 25000
        var maxKey: Int = -1

        for ((key, value) in listHorasSeleccionadas) {
            if (key.toInt() < minKey) {
                minKey = key.toInt()
            }

            if (key.toInt() > maxKey) {
                maxKey = key.toInt()
            }
        }

        horaInicioPantalla = listHorasSeleccionadas.getValue(minKey.toString()).substring(0, 5)
        horaFinPantalla = listHorasSeleccionadas.getValue(maxKey.toString()).substring(
            listHorasSeleccionadas.getValue(maxKey.toString()).length - 5,
            listHorasSeleccionadas.getValue(maxKey.toString()).length
        )

        horaInicioPrereserva = horaInicioPantalla
        horaFinPrereserva = horaFinPantalla
    }


    override fun itemClick(position: Int, itemSelected: Boolean, rangoHora: String) {

        if (itemSelected && listHorasSeleccionadas.count() <= horasPermitidas) {
            listHorasSeleccionadas.put(position.toString(), rangoHora)
            Log.i(LOG, "Item agregado a lista: $position y $rangoHora")

        } else if (!itemSelected) {
            listHorasSeleccionadas.remove(position.toString())
            Log.i(LOG, "Item removido de lista: $position y $rangoHora")
        } else {
            Log.i(LOG, "Máximo número de horas alcanzado")
        }

        if (listHorasSeleccionadas.count() > 0) {
            crear_grupo_y_prereservar_button.visibility = View.VISIBLE
        } else {
            crear_grupo_y_prereservar_button.visibility = View.GONE
        }
    }


    fun determinarHoraParaScreenSegundoFormato(horaRecibida: Int): String {

        return when (horaRecibida) {
            0 -> "00:00"
            6 -> "06:00"
            7 -> "07:00"
            8 -> "08:00"
            9 -> "09:00"
            10 -> "10:00"
            11 -> "11:00"
            12 -> "12:00"
            13 -> "13:00"
            14 -> "14:00"
            15 -> "15:00"
            16 -> "16:00"
            17 -> "17:00"
            18 -> "18:00"
            19 -> "19:00"
            20 -> "20:00"
            21 -> "21:00"
            22 -> "22:00"
            23 -> "23:00"
            24 -> "00:00"
            else -> "Hora Indeterminada"
        }
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
