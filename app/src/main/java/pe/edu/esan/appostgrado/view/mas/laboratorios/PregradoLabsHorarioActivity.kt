package pe.edu.esan.appostgrado.view.mas.laboratorios

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_pregrado_labs_horario.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PregradoPrereservaHorarioAdapter
import pe.edu.esan.appostgrado.entidades.AlumnoPrereservaLab
import pe.edu.esan.appostgrado.entidades.ProgramaDescripcionItem
import pe.edu.esan.appostgrado.entidades.PrereservaHorario
import pe.edu.esan.appostgrado.util.Utilitarios
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PregradoLabsHorarioActivity : AppCompatActivity(), PregradoPrereservaHorarioAdapter.HorarioListener {

    private var listHorasSeleccionadas = HashMap<String, String>()

    private var horasPermitidas: Double = 0.0

    private var codigoAlumno: String = ""
    private var alumnoPrereservaLab: AlumnoPrereservaLab? = null

    private var horarioList = ArrayList<PrereservaHorario>()

    private val LOG = PregradoLabsHorarioActivity::class.simpleName

    private var horaInicioPrereserva: String = ""
    private var horaFinPrereserva: String = ""

    private var horaInicioPantalla: String = ""
    private var horaFinPantalla: String = ""

    private var fechaPrereserva: String = ""

    private var URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_LAB_CONSULTAR_PROG_X_HORARIO)
    private var URL_TEST_VERIF_RESERVA = Utilitarios.getUrl(Utilitarios.URL.PREG_LAB_VERIFICAR_ALUMNO_RESERVA)

    private var mRequestQueue: RequestQueue? = null

    private val TAG = "PregradoLabsHorarioActivity"

    private var configuracionID: String? = ""

    private var programasList = ArrayList<ProgramaDescripcionItem>()

    private var cantHorasSeleccionadas = 0.0

    private var maxCantidadProgPermitidos = 0

    private var spanish = true

    private var horaActualParaHorario: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_labs_horario)

        setSupportActionBar(my_toolbar_horario_lab)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.horario_prereserva_title)

        my_toolbar_horario_lab.navigationIcon?.setColorFilter(
            ContextCompat.getColor(this, R.color.md_white_1000),
            PorterDuff.Mode.SRC_ATOP
        )
        my_toolbar_horario_lab.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))


        if (intent.hasExtra("alumno_prereserva_lab") && intent.hasExtra("codigo_alumno")) {
            alumnoPrereservaLab = intent.getParcelableExtra("alumno_prereserva_lab")
            codigoAlumno = intent.getStringExtra("codigo_alumno")
            configuracionID = alumnoPrereservaLab!!.idConfiguracionLab
            horaActualParaHorario = alumnoPrereservaLab!!.horaActualLab
        }

        determinarYMostrarHorarioData()

        revisar_disponibilidad_lab_button.setOnClickListener {
            if (listHorasSeleccionadas.count() == 0) {
                Log.e(LOG, "Error in HashMap, the button cannot be shown if count is 0")
            } else {

                progress_bar_horario_lab.visibility = View.VISIBLE
                tv_consultando_disp_lab.visibility = View.VISIBLE
                tv_consultando_disp_lab.setTextColor(ContextCompat.getColor(this, R.color.green))
                main_container_horario_lab.visibility = View.GONE

                determinarHoraInicioHoraFinParaRequest()

                val request = JSONObject()

                request.put("HoraIni", horaInicioPrereserva)
                request.put("HoraFin", horaFinPrereserva)

                revisarDisponibilidadLaboratoriosServicio(URL_TEST, request)
            }
        }

        revisar_disponibilidad_lab_button.visibility = View.GONE

        recycler_view_horario_lab.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(this, 2)
        recycler_view_horario_lab.setHasFixedSize(true)

        recycler_view_horario_lab.adapter = PregradoPrereservaHorarioAdapter(horarioList, determinarMaximaCantidadItemPermitidos(horasPermitidas), this)

    }

    
    fun determinarYMostrarHorarioData() {
        val horaInicioRespuestaServidor = alumnoPrereservaLab!!.horaIniResLab.toInt()
        val horaFinRespuestaServidor = alumnoPrereservaLab!!.horaFinResLab.toInt()
        val horaActualRespuestaServidor = alumnoPrereservaLab!!.horaActualLab.toInt()

        //DELETE LATER
        //val horaActualRespuestaServidor = 2300

        maxCantidadProgPermitidos = alumnoPrereservaLab!!.maxCantidadProgPermitidos.toInt()

        val language = Locale.getDefault().displayLanguage

        spanish = !(language.equals("English"))

        horasPermitidas = alumnoPrereservaLab!!.horasDispLab.toDouble()
        tv_horas_disponibles_lab.text = determinarHorasUsadasYHorasDisponibles(horasPermitidas)

        val horasUsadas = alumnoPrereservaLab!!.horasUsadasLab.toDouble()
        tv_horas_usadas_lab.text = determinarHorasUsadasYHorasDisponibles(horasUsadas)


        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val date = java.util.Date()
        fechaPrereserva = simpleDateFormat.format(date)

        if (horaActualRespuestaServidor < horaInicioRespuestaServidor * 100) {
            //LISTAR TODAS LAS HORAS
            generarHorarioLabs(700, 2300)
        } else if (horaInicioRespuestaServidor * 100 <= horaActualRespuestaServidor && horaActualRespuestaServidor < horaFinRespuestaServidor * 100) {
            //LISTAR DESDE LA HORA ACTUAL
            generarHorarioLabs(horaActualRespuestaServidor, 2300)
        } else {
            Log.d(LOG,"Fuera del rango permitido. El alumno desea reservar después de las 23:00 horas.")

            horarioList.clear()
        }

        recycler_view_horario_lab.adapter = PregradoPrereservaHorarioAdapter(horarioList, determinarMaximaCantidadItemPermitidos(horasPermitidas), this)
    }

    override fun onResume() {
        super.onResume()
        progress_bar_horario_lab.visibility = View.GONE
        tv_consultando_disp_lab.visibility = View.GONE
        main_container_horario_lab.visibility = View.VISIBLE

        val request = JSONObject()

        request.put("CodAlumno", codigoAlumno)

        verificarCambioHorarioServicio(URL_TEST_VERIF_RESERVA, request)
    }


    fun generarHorarioLabs(inicio: Int, fin: Int) {
        if (inicio % 100 == 30) {
            (inicio until fin - 70 step 100).forEach {
                horarioList.add(
                    PrereservaHorario(
                        determinarHoraParaScreen(it),
                        determinarHoraParaScreen(it + 70),
                        false
                    )
                )
                horarioList.add(
                    PrereservaHorario(
                        determinarHoraParaScreen(it + 70),
                        determinarHoraParaScreen(it + 100),
                        false
                    )
                )
            }
            horarioList.add(PrereservaHorario(determinarHoraParaScreen(2230), determinarHoraParaScreen(2300), false))
        } else {
            (inicio until fin step 100).forEach {
                horarioList.add(
                    PrereservaHorario(
                        determinarHoraParaScreen(it),
                        determinarHoraParaScreen(it + 30),
                        false
                    )
                )
                horarioList.add(
                    PrereservaHorario(
                        determinarHoraParaScreen(it + 30),
                        determinarHoraParaScreen(it + 100),
                        false
                    )
                )
            }
        }
    }

    fun revisarDisponibilidadLaboratoriosServicio(url: String, request: JSONObject) {

        Log.i(LOG, url)
        Log.i(LOG, request.toString())

        val fRequest = Utilitarios.jsObjectEncrypted(request, this@PregradoLabsHorarioActivity)

        Log.i(LOG, fRequest.toString())

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                fRequest,
                Response.Listener { response ->
                    Log.i(LOG, response.toString())

                    if (!response.isNull("ConsultarProgramasxHorarioResult")) {

                        val jsResponse = Utilitarios.jsArrayDesencriptar(
                            response.getString("ConsultarProgramasxHorarioResult"),
                            this@PregradoLabsHorarioActivity
                        )

                        Log.i(LOG, jsResponse!!.toString())

                        try {
                            //JSON Parsing
                            if (jsResponse.length() > 0) {
                                if (programasList.isEmpty()) {
                                    for (i in 0..jsResponse.length() - 1) {
                                        val programaItem = jsResponse.getJSONObject(i)

                                        val programaId = programaItem.getString("IdPrograma")
                                        val programaNombre = programaItem.getString("NomPrograma")
                                        val programaVersion = programaItem.getString("VerPrograma")

                                        val laboratorioId = programaItem.getInt("IdAmbiente")
                                        /*val laboratorioId = (0..4).random()*/

                                        programasList.add(
                                            ProgramaDescripcionItem(
                                                programaNombre,
                                                programaId,
                                                programaVersion,
                                                false,
                                                laboratorioId
                                            )
                                        )
                                    }
                                }
                                progress_bar_horario_lab.visibility = View.GONE
                                tv_consultando_disp_lab.visibility = View.GONE
                                main_container_horario_lab.visibility = View.VISIBLE

                                cantHorasSeleccionadas = determinarCantidadHorasSeleccionadas()

                                var dispositivo: String? = null

                                val codigoAlumno = intent.getStringExtra("codigo_alumno")

                                try {
                                    val pInfo = packageManager.getPackageInfo(packageName, 0)
                                    val version = pInfo.versionName

                                    dispositivo = "Android-$version"
                                } catch (e: PackageManager.NameNotFoundException) {
                                    Log.e(LOG, e.message.toString())
                                }

                                val intent = Intent(this, PregradoLabsSelectSoftwareActivity::class.java)
                                intent.putExtra("hora_inicio", horaInicioPrereserva)
                                intent.putExtra("hora_fin", horaFinPrereserva)
                                intent.putExtra("cantidad_horas_seleccionadas", cantHorasSeleccionadas)
                                intent.putExtra("codigo_alumno", codigoAlumno)
                                intent.putExtra("id_configuracion", configuracionID)
                                intent.putExtra("dispositivo_data", dispositivo)
                                intent.putExtra("fecha_prereserva", fechaPrereserva)
                                intent.putExtra("programas_list", programasList)
                                intent.putExtra("max_cantidad_prog_permitidos", maxCantidadProgPermitidos)
                                startActivity(intent)
                            } else {
                                Log.e(LOG, "Response is empty")

                                val intent = Intent(this, PregradoLabsSelectSoftwareActivity::class.java)
                                intent.putExtra("empty_response", true)
                                startActivity(intent)
                            }

                        } catch (e: Exception) {
                            Log.e(LOG, e.message.toString())

                            main_container_horario_lab.visibility = View.GONE
                            progress_bar_horario_lab.visibility = View.GONE

                            val params = RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.addRule(RelativeLayout.CENTER_IN_PARENT)
                            tv_consultando_disp_lab.layoutParams = params
                            tv_consultando_disp_lab.textSize = 26.0f
                            tv_consultando_disp_lab.text = getString(R.string.error_recuperacion_datos)
                            tv_consultando_disp_lab.visibility = View.VISIBLE
                            tv_consultando_disp_lab.setTextColor(ContextCompat.getColor(this, R.color.esan_red))
                        }

                    }
                },
                Response.ErrorListener { error ->
                    Log.e(LOG, "Error durante el request de Volley")
                    Log.e(LOG, error.message.toString())

                    main_container_horario_lab.visibility = View.GONE
                    progress_bar_horario_lab.visibility = View.GONE

                    val params = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.addRule(RelativeLayout.CENTER_IN_PARENT)
                    tv_consultando_disp_lab.layoutParams = params
                    tv_consultando_disp_lab.textSize = 26.0f
                    tv_consultando_disp_lab.text = getString(R.string.no_respuesta_desde_servidor)
                    tv_consultando_disp_lab.visibility = View.VISIBLE
                    tv_consultando_disp_lab.setTextColor(ContextCompat.getColor(this, R.color.esan_red))
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
        if (itemSelected && listHorasSeleccionadas.count() <= horasPermitidas * 2) {
            listHorasSeleccionadas.put(position.toString(), rangoHora)
            Log.i(LOG, "Item agregado a lista: $position y $rangoHora")

        } else if (!itemSelected) {
            listHorasSeleccionadas.remove(position.toString())
            Log.i(LOG, "Item removido de lista: $position y $rangoHora")
        } else {
            Log.i(LOG, "Máximo número de horas alcanzado")
        }

        if (listHorasSeleccionadas.count() > 0) {
            revisar_disponibilidad_lab_button.visibility = View.VISIBLE
        } else {
            revisar_disponibilidad_lab_button.visibility = View.GONE
        }
    }


    fun determinarHoraParaScreen(horaRecibida: Int): String {

        return when (horaRecibida) {
            600 -> "06:00"
            630 -> "06:30"
            700 -> "07:00"
            730 -> "07:30"
            800 -> "08:00"
            830 -> "08:30"
            900 -> "09:00"
            930 -> "09:30"
            1000 -> "10:00"
            1030 -> "10:30"
            1100 -> "11:00"
            1130 -> "11:30"
            1200 -> "12:00"
            1230 -> "12:30"
            1300 -> "13:00"
            1330 -> "13:30"
            1400 -> "14:00"
            1430 -> "14:30"
            1500 -> "15:00"
            1530 -> "15:30"
            1600 -> "16:00"
            1630 -> "16:30"
            1700 -> "17:00"
            1730 -> "17:30"
            1800 -> "18:00"
            1830 -> "18:30"
            1900 -> "19:00"
            1930 -> "19:30"
            2000 -> "20:00"
            2030 -> "20:30"
            2100 -> "21:00"
            2130 -> "21:30"
            2200 -> "22:00"
            2230 -> "22:30"
            2300 -> "23:00"
            2330 -> "23:00"
            2400 -> "24:00"
            2430 -> "24:00"
            else -> "Hora Indeterminada"
        }

    }

    fun determinarCantidadHorasSeleccionadas(): Double {
        return listHorasSeleccionadas.count().toDouble() / 2
    }

    fun determinarMaximaCantidadItemPermitidos(value: Double): Int {
        return (value * 2).toInt()
    }

    fun determinarHorasUsadasYHorasDisponibles(numHoras: Double): String {

        if (spanish) {
            return when (numHoras) {
                0.0 -> "0 horas"
                0.5 -> "1/2 hora"
                1.0 -> "1 hora"
                1.5 -> "1 hora y 1/2"
                2.0 -> "2 horas"
                2.5 -> "2 horas y 1/2"
                3.0 -> "3 horas"
                3.5 -> "3 horas y 1/2"
                4.0 -> "4 horas"
                4.5 -> "4 horas y 1/2"
                else -> "O horas"
            }
        } else {
            return when (numHoras) {
                0.0 -> "0 hours"
                0.5 -> "1/2 hour"
                1.0 -> "1 hour"
                1.5 -> "1 and 1/2 hours"
                2.0 -> "2 hours"
                2.5 -> "2 and 1/2 hours"
                3.0 -> "3 hours"
                3.5 -> "3 and 1/2 hours"
                4.0 -> "4 hours"
                4.5 -> "4 and 1/2 hours"
                else -> "O hours"
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


    private fun verificarCambioHorarioServicio(url: String, request: JSONObject) {

        Log.i(LOG, url)
        Log.i(LOG, request.toString())

        val fRequest = Utilitarios.jsObjectEncrypted(request, this@PregradoLabsHorarioActivity)

        Log.i(LOG, fRequest.toString())

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                fRequest,
                Response.Listener { response ->
                    Log.i(LOG, response.toString())
                    if (!response.isNull("VerificarAlumnoReservaResult")) {
                        val jsResponse = Utilitarios.jsObjectDesencriptar(
                            response.getString("VerificarAlumnoReservaResult"),
                            this@PregradoLabsHorarioActivity
                        )

                        Log.i(LOG, jsResponse!!.toString())

                        try {
                            val horasDisp = jsResponse.optString("HorasDisp")
                            val mensaje = jsResponse.optString("Mensaje")
                            //true es error y false es una operación exitosa
                            val indicador = jsResponse.optString("Indicador")
                            val horaIniRes = jsResponse.optString("HoraIniRes")
                            val horaFinRes = jsResponse.optString("HoraFinRes")

                            val newConfiguracionID = jsResponse.optString("IdConfiguracion")

                            val promocion = jsResponse.optString("Promocion")
                            val direccion = jsResponse.optString("TipoMatricula")
                            val horasUsadas = jsResponse.optString("HorasUsadas")
                            val maximaCantidadProgPermitidos = jsResponse.optString("CantProgRes")
                            horaActualParaHorario = jsResponse.optString("HoraActual")

                            if (indicador.toInt() == 0) {
                                if (horaActualParaHorario != alumnoPrereservaLab!!.horaActualLab) {
                                    alumnoPrereservaLab = AlumnoPrereservaLab(
                                        horasDisp,
                                        mensaje,
                                        indicador,
                                        horaIniRes,
                                        horaFinRes,
                                        newConfiguracionID,
                                        promocion,
                                        direccion,
                                        horasUsadas,
                                        maximaCantidadProgPermitidos,
                                        horaActualParaHorario)
                                    horarioList.clear()
                                    listHorasSeleccionadas.clear()
                                    revisar_disponibilidad_lab_button.visibility = View.GONE
                                    determinarYMostrarHorarioData()
                                }
                            } else {
                                main_container_horario_lab.visibility = View.GONE
                                tv_consultando_disp_lab.text = mensaje
                                tv_consultando_disp_lab.visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            Log.e(LOG, e.message.toString())
                            main_container_horario_lab.visibility = View.GONE
                            tv_consultando_disp_lab.text = getString(R.string.error_recuperacion_datos)
                            tv_consultando_disp_lab.visibility = View.VISIBLE
                        }

                        Log.i(LOG, "IdConfiguracion es: $configuracionID")
                    }
                },
                Response.ErrorListener { error ->
                    Log.e(LOG, "Error durante el request de Volley")
                    Log.e(LOG, error.message.toString())
                    main_container_horario_lab.visibility = View.GONE
                    tv_consultando_disp_lab.text = getString(R.string.no_respuesta_desde_servidor)
                    tv_consultando_disp_lab.visibility = View.VISIBLE

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


    override fun onStop() {
        super.onStop()
        mRequestQueue?.cancelAll(TAG)
    }
}
