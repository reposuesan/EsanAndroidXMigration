package pe.edu.esan.appostgrado.view.horario

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.ViewModelProviders
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_horario.*
import kotlinx.android.synthetic.main.fragment_horario.view.*
import org.json.JSONArray
import org.json.JSONObject

import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.HorarioAdapter
import pe.edu.esan.appostgrado.adapter.HorarioGrillaAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.Horario
import pe.edu.esan.appostgrado.entidades.HorarioGrilla
import pe.edu.esan.appostgrado.entidades.Profesor
import pe.edu.esan.appostgrado.util.Utilitarios
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HorarioFragment : androidx.fragment.app.Fragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    private val TAG = "HorarioFragment"

    var horarioCambio: Boolean = false
    private var fechaActual: Date? = null
    private var infoDayPlist: Array<String>? = null
    private var esSemanaActual = true
    private var existeHorario = false
    private var semanaActual = 1
    private var diaSemana = 10
    private val ddMMyyyy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var requestQueue: RequestQueue? = null
    private val diasSemanaKey =
        arrayOf("Lunes", "Martes", "Mi\u00E9rcoles", "Jueves", "Viernes", "S\u00E1bado", "Domingo")

    private val LOG = HorarioFragment::class.simpleName

    private var listaHorarioDiasTwo: ArrayList<ArrayList<String>> = ArrayList()
    private var listaGrillaTwo: ArrayList<HorarioGrilla> = ArrayList()

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        infoDayPlist = activity!!.resources.getStringArray(R.array.dias_semana)
        fechaActual = Date()

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view = inflater.inflate(R.layout.fragment_horario, container, false)
        } else {
            view = inflater.inflate(R.layout.fragment_horario_old_version, container, false)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        view.lblMensaje_fhorario.typeface = Utilitarios.getFontRoboto(context!!, Utilitarios.TypeFont.REGULAR)

        view.swHorario_fhorario.setOnRefreshListener(this)
        view.swHorario_fhorario.setColorSchemeResources(
            R.color.s1,
            R.color.s2,
            R.color.s3,
            R.color.s4
        )

        view.rvHorario_fhorario.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(activity!!)
        view.rvHorario_fhorario.adapter = null
        view.lblMensaje_fhorario.typeface = Utilitarios.getFontRoboto(activity!!, Utilitarios.TypeFont.THIN)

        horarioCambio = false
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnAfter_fhorario.setOnClickListener {
            val c = Calendar.getInstance()
            c.time = fechaActual
            c.add(Calendar.DATE, 7)
            fechaActual = c.time

            semanaActual += 1
            getHorario()
        }

        btnBefore_fhorario.setOnClickListener {
            val c = Calendar.getInstance()
            c.time = fechaActual
            c.add(Calendar.DATE, -7)
            fechaActual = c.time

            semanaActual -= 1
            getHorario()
        }

        getHorario()

        lblMensaje_fhorario.setOnClickListener {
            refreshManual()
        }

    }


    private fun getHorario() {
        showCabecera(fechaActual ?: Date())
        showBotones()
        showHorario()
    }


    private fun showBotones() {
        if (showBeforeButton(fechaActual ?: Date())) {
            btnBefore_fhorario.isEnabled = true
            btnBefore_fhorario.setImageResource(R.drawable.ico_before_active)
        } else {
            btnBefore_fhorario.isEnabled = false
            btnBefore_fhorario.setImageResource(R.drawable.ico_before_inactive)
        }

        if (semanaActual == Utilitarios.horarioSemanaVistaMaxima) {
            btnAfter_fhorario.isEnabled = false
            btnAfter_fhorario.setImageResource(R.drawable.ico_after_inactive)
        } else {
            btnAfter_fhorario.isEnabled = true
            btnAfter_fhorario.setImageResource(R.drawable.ico_after_active)
        }

    }


    private fun showBeforeButton(fecha: Date): Boolean {
        val hoy = Date()
        return fecha.after(hoy)
    }


    private fun showHorario() {

        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForFragmentPublic.observe(viewLifecycleOwner,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        sendRequest()
                    }
                }
            )
            controlViewModel.refreshDataForFragment(true)

        }
    }

    private fun sendRequest(){
        val usuario = ControlUsuario.instance.currentUsuario[0]
        val ddMMyyyy = SimpleDateFormat("ddMMyyyy", Locale.getDefault())

        val request = JSONObject()
        when (usuario) {
            is Alumno -> {
                request.put("Codigo", usuario.codigo)
                request.put("Tipo", 1)
                request.put("Fecha", ddMMyyyy.format(fechaActual?.time))
                request.put("Facultad", if (usuario.tipoAlumno == Utilitarios.POS) 1 else 2)
            }
            is Profesor -> {
                request.put("Codigo", usuario.codigo)
                request.put("Tipo", 2)
                request.put("Fecha", ddMMyyyy.format(fechaActual?.time))
                request.put("Facultad", 0)
            }
        }

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, activity!!)
        if (requestEncriptado != null) {
            onHorario(
                Utilitarios.getUrl(Utilitarios.URL.HORARIO_NEW),
                //request,
                requestEncriptado,
                usuario
            )
        } else {
            lblMensaje_fhorario.visibility = View.VISIBLE
            lblMensaje_fhorario.text = activity!!.resources.getString(R.string.error_encriptar)
        }
    }


    private fun onHorario(url: String, request: JSONObject, currentUsuario: Any) {

        Log.i(LOG, url)
        Log.i(LOG, request.toString())

        prbCargando_fhorario.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(activity!!)
        val jsObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            request,
            { response ->
                try {
                    //if (!response.isNull("ListarHorarioAlumnoProfesorxFecha2Result")) {
                    if (!response.isNull("ListarHorarioAlumnoProfesorxFechaResult")) {
                        //val scheduleJArray = response["ListarHorarioAlumnoProfesorxFecha2Result"] as JSONArray
                        val stringOutput = response["ListarHorarioAlumnoProfesorxFechaResult"] as String
                        val scheduleJArray = Utilitarios.jsArrayDesencriptar(stringOutput, activity!!)
                        if (scheduleJArray!!.length() > 0) {

                            ControlUsuario.instance.currentListHorario.clear()
                            for (z in 0 until scheduleJArray.length()) {
                                val scheduleJs = scheduleJArray[z] as JSONObject
                                var ambiente = scheduleJs["Ambiente"] as? String
                                if (ambiente == null) {
                                    ambiente = "-"
                                }
                                val valcurso = scheduleJs["Curso"] as String
                                val esPregrado = scheduleJs["EsPregrado"] as Int
                                val dia = (scheduleJs["Fecha"] as String).substring(
                                    0,
                                    1
                                ) + (scheduleJs["Fecha"] as String).substring(1)
                                val fechaActual =
                                    Utilitarios.getStringToStringddMMyyyyHHmmTwo(scheduleJs["FechaHoraActual"] as String)
                                val horaFin = scheduleJs["Fin"] as String
                                val horaInicio = scheduleJs["Inicio"] as String
                                val idAmbiente = scheduleJs["IdAmbiente"] as Int
                                val idCurso = scheduleJs["IdCurso"] as Int
                                val idHorario = scheduleJs["IdHorario"] as Int
                                val idSeccion = scheduleJs["IdSeccion"] as Int
                                val idSesion = scheduleJs["IdSesion"] as Int
                                val idProfesorReemplazo = scheduleJs["IdactorReemplazo"] as Int
                                val nombreProfesor = scheduleJs["Profesor"] as String
                                val nombreProfesorReemplazo = scheduleJs["ProfesorReemplazo"] as String
                                val promocion = scheduleJs["Promocion"] as String

                                //TODO: QR INTERNATIONAL WEEK
                                /*if(promocion.equals("[OBLIESP/20-2] CICLO 2020-0")){
                                    enableQR()
                                } else {
                                    disableQR()
                                }*/

                                val seccionCodigo = when (currentUsuario) {
                                    is Profesor -> scheduleJs["SeccionCodigo"] as String
                                    else -> ""
                                }

                                val tomoAsistencia = scheduleJs["TomoAsistencia"] as Int

                                val arrayNombre = valcurso.split("|")
                                var numSesionReal = ""
                                var nombreCurso = ""

                                if (arrayNombre.size > 1) {
                                    numSesionReal = arrayNombre[0].trim()
                                    nombreCurso = arrayNombre[1].trim()
                                } else {
                                    nombreCurso = arrayNombre[0].trim()
                                }
                                val codigoSeccion = when (currentUsuario) {
                                    is Alumno -> ""
                                    else -> scheduleJs["CodigoSeccion"] as String
                                }
                                val tipoHorario = when (currentUsuario) {
                                    is Alumno -> 1 //ES ALUMNO
                                    else -> 2 //ES PROFESOR
                                }

                                val horario = Horario(
                                    tipoHorario,
                                    idHorario,
                                    horaInicio,
                                    horaFin,
                                    nombreProfesor,
                                    dia,
                                    idAmbiente,
                                    nombreCurso,
                                    idSeccion,
                                    codigoSeccion,
                                    idSesion,
                                    seccionCodigo,
                                    ambiente,
                                    tomoAsistencia,
                                    esPregrado,
                                    fechaActual,
                                    numSesionReal,
                                    idProfesorReemplazo,
                                    nombreProfesorReemplazo
                                )
                                ControlUsuario.instance.currentListHorario.add(horario)
                                existeHorario = true
                            }
                            showHorarioWithoutAsyncTask(reformatHours(), true)

                            if (view != null) {
                                view!!.rvHorario_fhorario.visibility = View.VISIBLE
                                view!!.lblMensaje_fhorario.visibility = View.GONE
                            }
                        } else {
                            if (view != null) {
                                view!!.rvHorario_fhorario.visibility = View.GONE
                                view!!.lblMensaje_fhorario.visibility = View.VISIBLE
                                view!!.lblMensaje_fhorario.text = context!!.resources.getText(R.string.error_horario_no)
                            }
                        }

                    } else {
                        if (view != null) {
                            view!!.rvHorario_fhorario.visibility = View.GONE
                            view!!.lblMensaje_fhorario.visibility = View.VISIBLE
                            view!!.lblMensaje_fhorario.text = context!!.resources.getText(R.string.error_desencriptar)
                        }
                    }


                } catch (e: Exception) {
                    if (view != null) {
                        view!!.rvHorario_fhorario.visibility = View.GONE
                        view!!.lblMensaje_fhorario.visibility = View.VISIBLE
                        view!!.lblMensaje_fhorario.text = context!!.resources.getText(R.string.error_default)
                    }
                }

                if (view != null) {
                    view!!.swHorario_fhorario.isRefreshing = false
                    view!!.prbCargando_fhorario.visibility = View.GONE
                }
            },
            { volleyError ->

                Log.e(LOG, volleyError.message.toString())
                Log.e(LOG, "Error en Volley request")

                if (view != null) {
                    view!!.rvHorario_fhorario.visibility = View.GONE
                    view!!.prbCargando_fhorario.visibility = View.GONE
                    view!!.swHorario_fhorario.isRefreshing = false
                    view!!.lblMensaje_fhorario.visibility = View.VISIBLE
                    view!!.lblMensaje_fhorario.text = context!!.resources.getString(R.string.error_default)
                }

            }
        )
        jsObjectRequest.tag = TAG

        requestQueue?.add(jsObjectRequest)
    }


    private fun showCabecera(fecha: Date) {
        lblSemanaActual_fhorario.typeface = Utilitarios.getFontRoboto(context!!, Utilitarios.TypeFont.REGULAR)
        lblFecha_fhorario.typeface = Utilitarios.getFontRoboto(context!!, Utilitarios.TypeFont.LIGHT)


        val hoy = Calendar.getInstance()
        val fechahoy = Calendar.getInstance()
        hoy.time = fecha

        val fechaEnviada = ddMMyyyy.format(hoy.time)
        val fechaDeHoy = ddMMyyyy.format(fechahoy.time)

        val diaSemana = hoy.get(Calendar.DAY_OF_WEEK)
        val restarDiaSemana = (if (diaSemana == 1) 6 else diaSemana - 2) * -1

        hoy.add(Calendar.DATE, restarDiaSemana)
        val primerDiaSemana = ddMMyyyy.format(hoy.time)

        hoy.add(Calendar.DATE, 6)
        val ultimoDiaSemana = ddMMyyyy.format(hoy.time)

        if (fechaEnviada.equals(fechaDeHoy)) {

            lblSemanaActual_fhorario.text = activity!!.resources.getString(R.string.semana_actual)
            esSemanaActual = true
        } else {
            lblSemanaActual_fhorario.text = activity!!.resources.getString(R.string.semana)
            esSemanaActual = false
        }

        lblFecha_fhorario.text = "$primerDiaSemana - $ultimoDiaSemana"

    }


    fun getHorarioDiaEspecifico(_diaHorario: String, list: List<Horario>): ArrayList<Horario> {
        val diaHorario = clearDayText(_diaHorario.toLowerCase())
        val listaHorarioDiaEspecifico = ArrayList<Horario>()
        for (z in list) {
            if (clearDayText(z.dia.toLowerCase()) == diaHorario)
                listaHorarioDiaEspecifico.add(z)
        }

        return listaHorarioDiaEspecifico
    }


    private fun clearDayText(scheduleDay: String): String {
        var scheduleDaytoClear = ""
        val specialCharacter = charArrayOf('\u00E1', '\u00E9', '\u00ED', '\u00F3', '\u00FA')
        val normalCharacter = charArrayOf('\u0061', '\u0065', '\u0069', '\u006F', '\u0075')
        var statusPass = 0
        for (caracter in scheduleDay.toCharArray()) {
            statusPass = 0
            for (z in specialCharacter.indices) {
                if (caracter == specialCharacter[z]) {
                    scheduleDaytoClear = scheduleDaytoClear + normalCharacter[z]
                    statusPass = 1
                }
            }
            if (statusPass == 0) {
                scheduleDaytoClear = scheduleDaytoClear + caracter
            }
        }

        return scheduleDaytoClear
    }


    private fun cambiarHorario() {
        if (existeHorario) {
            showHorarioWithoutAsyncTask(reformatHours(), false)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_changehorario -> {
                cambiarHorario()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_changehorario)?.isVisible = true
        super.onPrepareOptionsMenu(menu)
    }


    override fun onRefresh() {
        refreshManual()
    }


    fun refreshManual() {
        if (Utilitarios.isNetworkAvailable(activity!!)) {
            swHorario_fhorario.isRefreshing = true
            showHorario()
        } else {
            rvHorario_fhorario.visibility = View.GONE
            lblMensaje_fhorario.visibility = View.VISIBLE
            lblMensaje_fhorario.text = activity!!.resources.getString(R.string.error_default)
            swHorario_fhorario.isRefreshing = false
        }
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
    }


    override fun onPause() {
        super.onPause()
    }


    override fun onResume() {
        super.onResume()
        if (ControlUsuario.instance.pantallaSuspendida) {
            ControlUsuario.instance.pantallaSuspendida = false
            getHorario()
        }
    }


    override fun onStart() {
        super.onStart()
    }


    override fun onDestroy() {
        super.onDestroy()
    }



    private fun reformatHours(): List<Horario>{

        val listForReformat = ControlUsuario.instance.currentListHorario

        val newListFormatted = ArrayList<Horario>()

        var inicioRango: String
        var finalRango: String
        var diaRango: String
        var tipoHorarioRango: Int
        var idHorarioRango: Int
        var profesorRango: String
        var idAmbienteRango: Int
        var cursoRango: String
        var idSeccionRango: Int
        var seccionCodigoRango: String
        var idSesionRango: Int
        var sectionRango: String
        var aulaRango: String
        var takeAssistRango: Int
        var esPregradoRango: Int
        var horaConsultaRango: String
        var numSesionRealRango: String
        var idProfesorReemRango: Int
        var profesorReemRango: String

        if(listForReformat.size > 0){
            inicioRango = listForReformat[0].horaInicio
            finalRango = listForReformat[0].horaFin
            diaRango = listForReformat[0].dia
            tipoHorarioRango = listForReformat[0].tipoHorario
            idHorarioRango = listForReformat[0].idHorario
            profesorRango = listForReformat[0].profesor
            idAmbienteRango = listForReformat[0].idAmbiente
            cursoRango = listForReformat[0].curso
            idSeccionRango = listForReformat[0].idSeccion
            seccionCodigoRango = listForReformat[0].seccionCodigo
            idSesionRango = listForReformat[0].idSesion
            sectionRango = listForReformat[0].section
            aulaRango = listForReformat[0].aula
            takeAssistRango = listForReformat[0].takeAssist
            esPregradoRango = listForReformat[0].esPregrado
            horaConsultaRango = listForReformat[0].horaConsulta
            numSesionRealRango = listForReformat[0].numSesionReal
            idProfesorReemRango = listForReformat[0].idProfesorReem
            profesorReemRango = listForReformat[0].profesorReem

            if(listForReformat.size > 1){
                for(i in 1 until listForReformat.size){
                    if(listForReformat[i].horaInicio == finalRango && listForReformat[i].dia == diaRango){
                        finalRango = listForReformat[i].horaFin
                    } else {

                        val newHorario = Horario(tipoHorarioRango, idHorarioRango, cambiarFormatoParaHorarioEspecial(inicioRango), cambiarFormatoParaHorarioEspecial(finalRango),
                            profesorRango, diaRango, idAmbienteRango, cursoRango, idSeccionRango, seccionCodigoRango, idSesionRango, sectionRango, aulaRango, takeAssistRango,
                            esPregradoRango, horaConsultaRango, numSesionRealRango, idProfesorReemRango, profesorReemRango)

                        newListFormatted.add(Horario(
                            0, 0, "", "", "",
                            "", 0, "", 0, "", 0, "", "",
                            0, 0, "", "", 0, "", newHorario
                        ))

                        inicioRango = listForReformat[i].horaInicio
                        finalRango = listForReformat[i].horaFin
                        diaRango = listForReformat[i].dia
                        tipoHorarioRango = listForReformat[i].tipoHorario
                        idHorarioRango = listForReformat[i].idHorario
                        profesorRango = listForReformat[i].profesor
                        idAmbienteRango = listForReformat[i].idAmbiente
                        cursoRango = listForReformat[i].curso
                        idSeccionRango = listForReformat[i].idSeccion
                        seccionCodigoRango = listForReformat[i].seccionCodigo
                        idSesionRango = listForReformat[i].idSesion
                        sectionRango = listForReformat[i].section
                        aulaRango = listForReformat[i].aula
                        takeAssistRango = listForReformat[i].takeAssist
                        esPregradoRango = listForReformat[i].esPregrado
                        horaConsultaRango = listForReformat[i].horaConsulta
                        numSesionRealRango = listForReformat[i].numSesionReal
                        idProfesorReemRango = listForReformat[i].idProfesorReem
                        profesorReemRango = listForReformat[i].profesorReem
                    }
                }

                val newHorario = Horario(tipoHorarioRango, idHorarioRango, cambiarFormatoParaHorarioEspecial(inicioRango), cambiarFormatoParaHorarioEspecial(finalRango),
                    profesorRango, diaRango, idAmbienteRango, cursoRango, idSeccionRango, seccionCodigoRango, idSesionRango, sectionRango, aulaRango, takeAssistRango,
                    esPregradoRango, horaConsultaRango, numSesionRealRango, idProfesorReemRango, profesorReemRango)

                newListFormatted.add(Horario(
                    0, 0, "", "", "",
                    "", 0, "", 0, "", 0, "", "",
                    0, 0, "", "", 0, "", newHorario
                ))
            } else if(listForReformat.size == 1){

                val newHorario = Horario(tipoHorarioRango, idHorarioRango, cambiarFormatoParaHorarioEspecial(inicioRango), cambiarFormatoParaHorarioEspecial(finalRango),
                    profesorRango, diaRango, idAmbienteRango, cursoRango, idSeccionRango, seccionCodigoRango, idSesionRango, sectionRango, aulaRango, takeAssistRango,
                    esPregradoRango, horaConsultaRango, numSesionRealRango, idProfesorReemRango, profesorReemRango)

                newListFormatted.add(Horario(
                    0, 0, "", "", "",
                    "", 0, "", 0, "", 0, "", "",
                    0, 0, "", "", 0, "", newHorario
                ))
            }

        } else{
           Log.w(LOG,"ControlUsuario.instance.currentListHorario is empty")
        }

        return generateNewArrangement(newListFormatted)
    }

    private fun cambiarFormatoParaHorarioEspecial(input: String): String{

        var output = ""

        if(input.replace(":","").toInt() % 100 in 1..14){

            val item = input.replace(":","").toInt()

            val hora = item/100

            if(hora < 10){
                output = "0$hora:00"
            } else{
                output = "$hora:00"
            }
        } else if(input.replace(":","").toInt() % 100 in 15..29){
            val item = input.replace(":","").toInt()

            val hora = item/100

            if(hora < 10){
                output = "0$hora:30"
            } else{
                output = "$hora:30"
            }
        } else if(input.replace(":","").toInt() % 100 in 31..45) {

            val item = input.replace(":","").toInt()

            val hora = item/100

            if(hora < 10){
                output = "0$hora:30"
            } else{
                output = "$hora:30"
            }
        } else if(input.replace(":","").toInt() % 100 in 46..59) {
            val item = input.replace(":","").toInt()

            val hora = item/100

            if(hora + 1 < 10){
                output = "0${hora+1}:00"
            } else{
                output = "${hora+1}:00"
            }
        } else {
            output = input
        }

        return output
    }


    private fun generateNewArrangement(list: ArrayList<Horario>): List<Horario> {

        var counter = 0
        val newListFormatted = ArrayList<Horario>()

        for(horario in list){
            val inicioInt = horario.horaInicio.replace(":","").toInt()
            val finInt = horario.horaFin.replace(":","").toInt()

            if(finInt - inicioInt > 50 && (inicioInt % 100 == 0 || inicioInt % 100 == 30)){
                var firstTime = true
                for(iterator in inicioInt until (finInt + 100) step 100){

                    val hora = iterator/ 100

                    if(iterator < finInt){

                        newListFormatted.add(Horario(
                            0, 0, "", "", "",
                            "", 0, "", 0, "", 0, "", "",
                            0, 0, "", "", 0, "", horario
                        ))
                        val itemHorario = newListFormatted[counter]

                        if(inicioInt % 100 == 30 || inicioInt % 100 == 0){
                            if(hora < 10){
                                itemHorario.horaInicio = "0${hora}:00"
                                itemHorario.horaFin = "0${hora}:50"
                            } else {
                                itemHorario.horaInicio = "${hora}:00"
                                itemHorario.horaFin = "${hora}:50"
                            }

                            if(inicioInt % 100 == 30 && firstTime && finInt - inicioInt >= 30){
                                if(hora < 10){
                                    itemHorario.horaInicio = "0${hora}:30"
                                } else {
                                    itemHorario.horaInicio = "${hora}:30"
                                }
                            }

                            firstTime = false
                        }

                        if(finInt - iterator == 30){
                            if(hora < 10){
                                itemHorario.horaInicio = "0${hora}:00"
                                itemHorario.horaFin = "0${hora}:30"
                            } else {
                                itemHorario.horaInicio = "${hora}:00"
                                itemHorario.horaFin = "${hora}:30"
                            }
                        }
                        counter++
                    } else if (iterator == finInt && finInt % 100 == 30){

                        newListFormatted.add(Horario(
                            0, 0, "", "", "",
                            "", 0, "", 0, "", 0, "", "",
                            0, 0, "", "", 0, "", horario
                        ))
                        val itemHorario = newListFormatted[counter]

                        if(hora < 10){
                            itemHorario.horaInicio = "0${hora}:00"
                        } else {
                            itemHorario.horaInicio = "${hora}:00"
                        }
                        counter++
                    }
                }
            } else {
                if(finInt - inicioInt <= 50){
                    if(finInt - inicioInt == 30 && inicioInt % 100 == 0){
                        val hora = inicioInt/100

                        newListFormatted.add(Horario(
                            0, 0, "", "", "",
                            "", 0, "", 0, "", 0, "", "",
                            0, 0, "", "", 0, "", horario
                        ))
                        val itemHorario = newListFormatted[counter]

                        if(hora < 10){
                            itemHorario.horaInicio = "0${hora}:00"
                            itemHorario.horaFin = "0${hora}:30"
                        } else {
                            itemHorario.horaInicio = "${hora}:00"
                            itemHorario.horaFin = "${hora}:30"
                        }
                        counter++
                    }
                }
            }
        }

        return newListFormatted
    }

    private fun defineTipoHorario(listaHorario: List<Horario>, mantener: Boolean): Boolean {
        return if (mantener) {
            if (horarioCambio) {
                horarioGrillaTwo(listaHorario)
            } else {
                horarioDiasSemanaTwo(listaHorario)
            }
        } else {
            if (horarioCambio) {
                horarioDiasSemanaTwo(listaHorario)
            } else {
                horarioGrillaTwo(listaHorario)
            }
        }
    }


    private fun horarioDiasSemanaTwo(list: List<Horario>): Boolean {

        listaHorarioDiasTwo.clear()

        val day_ofWeek = GregorianCalendar()
        var _dayof = Utilitarios.getDiaSemana(day_ofWeek.get(Calendar.DAY_OF_WEEK))


        val dayOfWeek = Utilitarios.getDiaSemana(GregorianCalendar().get(Calendar.DAY_OF_WEEK))

        var contador = 0
        for (i in infoDayPlist!!.indices) {
            val listaHorarioDiaSelect = getHorarioDiaEspecifico(diasSemanaKey[i], list)

            if (listaHorarioDiaSelect.isNotEmpty()) {
                if (esSemanaActual) {
                    if (dayOfWeek == i)
                        diaSemana = contador
                    contador += 1
                } else {
                    diaSemana = 10
                }
                val diaEncontrado = arrayListOf(diasSemanaKey[i], infoDayPlist!![i])
                listaHorarioDiasTwo.add(diaEncontrado)
            }
        }

        return true
    }


    private fun horarioGrillaTwo(listaHorario: List<Horario>): Boolean {
        val hoy = Calendar.getInstance()
        hoy.time = Date()

        val horaActual = Utilitarios.getLongToStringHHmm(hoy.timeInMillis).split(":")
        val horaHoraActual = horaActual[0].toInt()
        val minutosHoraActual = horaActual[1].toInt()

        listaGrillaTwo.clear()

        var HInicio = 0
        var HFin = 0

        //For loop para identificar el rango vertical de horas de la grilla
        for (horario in listaHorario) {
            val inicio = horario.horaInicio.split(":")
            val fin = horario.horaFin.split(":")

            val HHi = inicio[0].toInt()
            val HHf = fin[0].toInt()

            if (HInicio == 0) {
                HInicio = HHi
                HFin = HHf
            } else {
                if (HHi < HInicio) {
                    HInicio = HHi
                }

                if (HHf > HFin) {
                    HFin = HHf
                }
            }
        }


        for (horaEnGrilla in HInicio..HFin) {
            var enHoraActual = false
            val horaInicio = String.format("%02d:00", horaEnGrilla)
            val horaFin = String.format("%02d:50", horaEnGrilla)

            if (horaEnGrilla == horaHoraActual) {
                if (semanaActual == 1) {
                    enHoraActual = true
                    listaGrillaTwo.add(HorarioGrilla(horaInicio + "\n" + horaFin, "", 0, true, minutosHoraActual))
                } else {
                    listaGrillaTwo.add(HorarioGrilla(horaInicio + "\n" + horaFin, "", 0))
                }
            } else {
                listaGrillaTwo.add(HorarioGrilla(horaInicio + "\n" + horaFin, "", 0))
            }

            for (indexParaDiaDeSemana in 0..6) {
                var valorInferior = ""
                var valorSuperior = ""
                var tipo = 3

                for (horario in listaHorario) {
                    if (clearDayText(horario.dia) == diasSemanaKey[indexParaDiaDeSemana].toUpperCase()) {
                        val inicio = horario.horaInicio.split(":")
                        val fin = horario.horaFin.split(":")

                        val hHoraInicio = inicio[0].toInt()
                        val mHoraInicio = inicio[1].toInt()
                        val hHoraFin = fin[0].toInt()
                        val mHoraFin = fin[1].toInt()

                        if (horaEnGrilla == hHoraInicio) {
                            if (mHoraInicio < 30) {
                                if (horaEnGrilla == hHoraFin) {
                                    if (mHoraFin <= 30) {
                                        valorSuperior = horario.curso + "\n" + horario.aula
                                        tipo = 2
                                    } else {
                                        valorInferior = horario.curso + "\n" + horario.aula
                                        tipo = 1
                                    }
                                } else {
                                    valorInferior = horario.curso + "\n" + horario.aula
                                    tipo = 1
                                }
                            } else {
                                valorInferior = horario.curso + "\n" + horario.aula
                                tipo = 2
                            }

                        } else {
                            if (horaEnGrilla == hHoraFin && mHoraFin != 0) {
                                if (mHoraFin <= 30) {
                                    valorSuperior = horario.curso + "\n" + horario.aula
                                    tipo = 2
                                } else {
                                    valorInferior = horario.curso + "\n" + horario.aula
                                    tipo = 1
                                }
                            }
                        }
                    }
                }


                if (enHoraActual) {
                    listaGrillaTwo.add(HorarioGrilla(valorInferior, valorSuperior, tipo, true, minutosHoraActual))
                } else {
                    listaGrillaTwo.add(HorarioGrilla(valorInferior, valorSuperior, tipo))
                }
            }

        }


        return true
    }


    private fun showHorarioWithoutAsyncTask(listaHorario: List<Horario>, mantener: Boolean) {

        listaHorarioDiasTwo.clear()

        listaGrillaTwo.clear()

        prbCargando_fhorario.visibility = View.VISIBLE

        val result = defineTipoHorario(listaHorario, mantener)

        prbCargando_fhorario.visibility = View.GONE
        if (result) {
            if (mantener) {
                if (horarioCambio) {
                    val hoy = Calendar.getInstance()
                    hoy.time = fechaActual ?: Date()

                    val diaSemana = hoy[Calendar.DAY_OF_WEEK]
                    val restarDiaSemana = ((if (diaSemana == 1) 6 else diaSemana - 2) * -1) - 1
                    hoy.add(Calendar.DATE, restarDiaSemana)
                    val fechas = ArrayList<String>()
                    for (i in 0..6) {
                        hoy.add(Calendar.DATE, 1)
                        fechas.add(Utilitarios.getStringToDatedd(hoy.time))
                    }

                    val nombreDias = resources.getStringArray(R.array.dias_semana_min)
                    lblLunes_fhorario.text = "${nombreDias[0]} \n ${fechas[0]}"
                    lblMartes_fhorario.text = "${nombreDias[1]} \n ${fechas[1]}"
                    lblMiercoles_fhorario.text = "${nombreDias[2]} \n ${fechas[2]}"
                    lblJueves_fhorario.text = "${nombreDias[3]} \n ${fechas[3]}"
                    lblViernes_fhorario.text = "${nombreDias[4]} \n ${fechas[4]}"
                    lblSabado_fhorario.text = "${nombreDias[5]} \n ${fechas[5]}"
                    lblDomingo_fhorario.text = "${nombreDias[6]} \n ${fechas[6]}"

                    viewDiasSemana_fhorario.visibility = View.VISIBLE

                    rvHorario_fhorario.layoutManager =
                        androidx.recyclerview.widget.GridLayoutManager(
                            context!!,
                            8
                        )
                    val adapter = HorarioGrillaAdapter(listaGrillaTwo)
                    rvHorario_fhorario.adapter = adapter
                    horarioCambio = true
                } else {
                    rvHorario_fhorario.layoutManager =
                        androidx.recyclerview.widget.LinearLayoutManager(context!!)

                    val adapter = HorarioAdapter(listaHorarioDiasTwo, diaSemana, context!!) { position: Int ->

                        val diaClick = listaHorarioDiasTwo[position][0]
                        val horarioDia = getHorarioDiaEspecifico(diaClick, ControlUsuario.instance.currentListHorario)

                        if (horarioDia.size > 0) {
                            val hoy = diaSemana == position
                            ControlUsuario.instance.currentListHorarioSelect = horarioDia

                            val intent = Intent(activity!!, HorarioDetalleActivity::class.java)
                            intent.putExtra("KEY_DIA", diaClick)
                            intent.putExtra("KEY_HOY", hoy)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        } else {
                            val builder = AlertDialog.Builder(activity!!)
                            builder.setMessage(getString(R.string.error_login_tres))
                                .setTitle(getString(R.string.error))
                            val dialog = builder.create()
                            dialog.show()
                        }

                    }
                    rvHorario_fhorario.adapter = adapter
                    viewDiasSemana_fhorario.visibility = View.GONE
                    horarioCambio = false
                }
            } else {
                if (horarioCambio) {
                    rvHorario_fhorario.layoutManager =
                        androidx.recyclerview.widget.LinearLayoutManager(context!!)

                    val adapter = HorarioAdapter(listaHorarioDiasTwo, diaSemana, context!!) { position: Int ->

                        val diaClick = listaHorarioDiasTwo[position][0]
                        val horarioDia = getHorarioDiaEspecifico(diaClick, ControlUsuario.instance.currentListHorario)

                        if (horarioDia.size > 0) {
                            val hoy = diaSemana == position
                            ControlUsuario.instance.currentListHorarioSelect = horarioDia

                            val intent = Intent(activity!!, HorarioDetalleActivity::class.java)
                            intent.putExtra("KEY_DIA", diaClick)
                            intent.putExtra("KEY_HOY", hoy)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        } else {
                            val builder = AlertDialog.Builder(activity!!)
                            builder.setMessage(getString(R.string.error_login_tres))
                                .setTitle(getString(R.string.error))
                            val dialog = builder.create()
                            dialog.show()
                        }

                    }
                    rvHorario_fhorario.adapter = adapter
                    viewDiasSemana_fhorario.visibility = View.GONE
                    horarioCambio = false
                } else {
                    val hoy = Calendar.getInstance()
                    hoy.time = fechaActual ?: Date()

                    val diaSemana = hoy[Calendar.DAY_OF_WEEK]
                    val restarDiaSemana = ((if (diaSemana == 1) 6 else diaSemana - 2) * -1) - 1
                    hoy.add(Calendar.DATE, restarDiaSemana)
                    val fechas = ArrayList<String>()
                    for (i in 0..6) {
                        hoy.add(Calendar.DATE, 1)
                        fechas.add(Utilitarios.getStringToDatedd(hoy.time))
                    }

                    val nombreDias = resources.getStringArray(R.array.dias_semana_min)
                    lblLunes_fhorario.text = "${nombreDias[0]} \n ${fechas[0]}"
                    lblMartes_fhorario.text = "${nombreDias[1]} \n ${fechas[1]}"
                    lblMiercoles_fhorario.text = "${nombreDias[2]} \n ${fechas[2]}"
                    lblJueves_fhorario.text = "${nombreDias[3]} \n ${fechas[3]}"
                    lblViernes_fhorario.text = "${nombreDias[4]} \n ${fechas[4]}"
                    lblSabado_fhorario.text = "${nombreDias[5]} \n ${fechas[5]}"
                    lblDomingo_fhorario.text = "${nombreDias[6]} \n ${fechas[6]}"

                    viewDiasSemana_fhorario.visibility = View.VISIBLE

                    rvHorario_fhorario.layoutManager =
                        androidx.recyclerview.widget.GridLayoutManager(
                            context!!,
                            8
                        )
                    val adapter = HorarioGrillaAdapter(listaGrillaTwo)
                    rvHorario_fhorario.adapter = adapter
                    horarioCambio = true
                }
            }
        }

    }

    //TODO: QR INTERNATIONAL WEEK
    /*fun enableQR(){
        //controlViewModel.setQRCondition(true)
        val misPreferencias = activity!!.getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
        val editor = misPreferencias.edit()

        editor.putBoolean("qr_code_iw", true)

        editor.apply()

    }

    //TODO: QR INTERNATIONAL WEEK
    fun disableQR(){
        //controlViewModel.setQRCondition(false)
        val misPreferencias = activity!!.getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
        val editor = misPreferencias.edit()

        editor.putBoolean("qr_code_iw", false)

        editor.apply()
    }*/
}
