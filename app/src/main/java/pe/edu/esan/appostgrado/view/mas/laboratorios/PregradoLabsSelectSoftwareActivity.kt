package pe.edu.esan.appostgrado.view.mas.laboratorios

import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_pregrado_labs_select_software.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PregradoPrereservaSeleccionLabsAdapter
import pe.edu.esan.appostgrado.adapter.PregradoPrereservaTagsAdapter
import pe.edu.esan.appostgrado.entidades.ProgramaDescripcionItem
import pe.edu.esan.appostgrado.util.Utilitarios
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PregradoLabsSelectSoftwareActivity : AppCompatActivity(), PregradoPrereservaSeleccionLabsAdapter.LabsListener {

    private var programsIdsList = ArrayList<String>()

    private var URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_LAB_REGISTRAR_PRERESERVA_LABORATORIO)

    private val LOG = PregradoLabsSelectSoftwareActivity::class.simpleName

    private val TAG = "PregradoLabsSelectSoftwareActivity"

    private var mRequestQueue : RequestQueue? = null
    private var configuracionID: String = ""
    private var codigoAlumno: String = ""
    private var horaInicioPrereserva: String = ""
    private var horaFinPrereserva: String = ""
    private var fechaPrereserva: String = ""

    private var rawList = ArrayList<ProgramaDescripcionItem>()
    private var helperList: List<ProgramaDescripcionItem> = ArrayList<ProgramaDescripcionItem>()

    private var mAdapter: PregradoPrereservaSeleccionLabsAdapter? = null
    private var mAdapterTags: PregradoPrereservaTagsAdapter? = null

    private var countForAdapter = 0
    private var cantHorasSeleccionadas = 0.0
    private var dispositivo: String? = null
    private var maxCantidadProgPermitidos = 0
    private var prereservaRealizada = false
    private var dialogoMostradoEnPantalla = false
    private var sizeSavedOfList = 0
    private var lastFirstVisiblePosition = 0
    private var offsetTop = 0
    private var tagsList = ArrayList<String>()
    private var internetChecked = false
    private var listItemsUpdated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_labs_select_software)

        setSupportActionBar(my_toolbar_select_software_lab)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.labs_disponibles_title)

        my_toolbar_select_software_lab.navigationIcon?.setColorFilter(ContextCompat.getColor(this, R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP)
        my_toolbar_select_software_lab.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        prereservaRealizada = false
        dialogoMostradoEnPantalla = false

        progress_bar_select_software_lab.visibility = View.GONE

        if(intent.hasExtra("empty_response")) {
            val emptyResponseFromHorario = intent.getBooleanExtra("empty_response", false)
            if(emptyResponseFromHorario){
                tv_empty_select_software_lab.text = getString(R.string.no_hay_labs_disponibles)
                tv_empty_select_software_lab.visibility = View.VISIBLE
                tv_empty_select_software_lab.setTextColor(ContextCompat.getColor(applicationContext, R.color.esan_red))
                select_software_lab_container.visibility = View.GONE
            }
        } else {

            if (intent.hasExtra("codigo_alumno")) {
                codigoAlumno = intent.getStringExtra("codigo_alumno")
            }

            if (intent.hasExtra("hora_inicio") && intent.hasExtra("hora_fin") && intent.hasExtra("fecha_prereserva") && intent.hasExtra("programas_list")) {
                horaInicioPrereserva = intent.getStringExtra("hora_inicio")
                horaFinPrereserva = intent.getStringExtra("hora_fin")
                fechaPrereserva = intent.getStringExtra("fecha_prereserva")
                cantHorasSeleccionadas = intent.getDoubleExtra("cantidad_horas_seleccionadas", 0.0)
                configuracionID = intent.getStringExtra("id_configuracion")
                rawList = intent.getParcelableArrayListExtra("programas_list")
                dispositivo = intent.getStringExtra("dispositivo_data")
                maxCantidadProgPermitidos = intent.getIntExtra("max_cantidad_prog_permitidos", 0)
                textview_horario_select_software.text = applicationContext.getString(R.string.hours_select_software_text, horaInicioPrereserva, horaFinPrereserva)
            }


            prereservar_laboratorio_button.setOnClickListener {
                if(programsIdsList.isEmpty() && !internetChecked){
                    val snack = Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.debe_seleccionarse_un_lab_mensaje),
                        Snackbar.LENGTH_SHORT
                    )
                    snack.view.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.warning))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(applicationContext, R.color.warning_text))
                    snack.show()
                } else {

                    var labsIdsChain = ""

                    if(internetChecked){
                        labsIdsChain = "-1"
                    } else {
                        for (i in 0..programsIdsList.size - 1) {
                            labsIdsChain += programsIdsList[i] + ","
                        }
                    }

                    val request = JSONObject()

                    request.put("HoraInicio", horaInicioPrereserva)
                    request.put("HoraFin", horaFinPrereserva)
                    request.put("CantHoras", cantHorasSeleccionadas)

                    request.put("CodAlumno", codigoAlumno)
                    request.put("IdConfiguracion", configuracionID.toInt())
                    request.put("Dispositivo", dispositivo)
                    request.put("IdsProgramas", labsIdsChain)

                    prereservar_laboratorio_button.isEnabled = false

                    progress_bar_select_software_lab.visibility = View.VISIBLE
                    tv_empty_select_software_lab.visibility = View.GONE
                    select_software_lab_container.visibility = View.GONE

                    registrarPrereservaLabServicio(URL_TEST, request)
                }
            }

            fab_select_software.setOnClickListener {
                showDialogFab()
            }

            tv_empty_select_software_lab.visibility = View.GONE
            select_software_lab_container.visibility = View.VISIBLE

            recycler_view_select_software_lab.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this)
            recycler_view_select_software_lab.setHasFixedSize(true)

            val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                this,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )

            recycler_view_select_software_lab_tags.layoutManager = layoutManager

            recycler_view_select_software_lab_tags.setHasFixedSize(true)

            helperList = efectuarFiltroItemsRepetidos(rawList, true)
            sizeSavedOfList = helperList.size

            mAdapter = PregradoPrereservaSeleccionLabsAdapter(helperList, this, countForAdapter, maxCantidadProgPermitidos)

            recycler_view_select_software_lab.adapter = mAdapter

            prereservar_laboratorio_button.visibility = View.GONE

            et_search_field_labs.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val dataEntered = et_search_field_labs.text.toString().trim()
                    if(!dataEntered.isNullOrEmpty()) {
                        efectuarFiltroBusquedaPrograma(dataEntered, helperList)
                    } else {
                        if(!listItemsUpdated){
                            refreshList()
                        }
                    }
                    listItemsUpdated = false
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
            })
        }

    }

    fun refreshList() {
        mAdapter = PregradoPrereservaSeleccionLabsAdapter(helperList, this, countForAdapter, maxCantidadProgPermitidos)
        recycler_view_select_software_lab.adapter = mAdapter
    }

    fun showDialogFab(){

        val builder = AlertDialog.Builder(this@PregradoLabsSelectSoftwareActivity, R.style.EsanAlertDialogInformation)

        builder.setTitle(getString(R.string.indicaciones_de_uso))
            .setMessage(getString(R.string.recordatorio_select_software))
            .setPositiveButton(getString(R.string.positive_dialog)) { dialog, which ->
            }
            .setOnCancelListener {
            }
            .show()
    }

    fun onCheckboxClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            when (view.id) {
                R.id.checkbox_solo_internet -> {
                    if (checked) {
                        recycler_view_select_software_lab.visibility = View.INVISIBLE
                        recycler_view_select_software_lab_tags.visibility = View.INVISIBLE
                        et_search_field_labs.visibility = View.INVISIBLE
                        prereservar_laboratorio_button.visibility = View.VISIBLE
                        internetChecked = true
                    } else {
                        recycler_view_select_software_lab.visibility = View.VISIBLE
                        recycler_view_select_software_lab_tags.visibility = View.VISIBLE
                        et_search_field_labs.visibility = View.VISIBLE
                        prereservar_laboratorio_button.visibility = View.INVISIBLE
                        mostrarButtonRealizarPrereserva()
                        internetChecked = false
                    }
                }
            }
        }
    }


    fun efectuarFiltroBusquedaPrograma(data: String, list: List<ProgramaDescripcionItem>){
        val labsHelperListForSearch = list.filter { item -> item.programaNombre.contains(data, true) }
        mAdapter = PregradoPrereservaSeleccionLabsAdapter(labsHelperListForSearch, this, countForAdapter, maxCantidadProgPermitidos)
        recycler_view_select_software_lab.adapter = mAdapter
    }


    fun efectuarFiltroItemsRepetidos(list: List<ProgramaDescripcionItem>, firstTime: Boolean): List<ProgramaDescripcionItem>{
        val distinctList =  list.distinctBy { it.programaNombre }

        if(!firstTime) {
            if (distinctList.isNotEmpty()) {
                if (programsIdsList.isNotEmpty()) {
                    for (j in 0..programsIdsList.size - 1) {
                        for (i in 0..distinctList.size - 1) {
                            //Comparamos para ver que items deben ser seleccionados
                            if (distinctList[i].programaId == programsIdsList[j]) {
                                distinctList[i].itemSeleccionado = true
                            }
                        }
                    }
                }
            }
        }

        return generateNewDeepCopyList(distinctList)
    }


    fun efectuarFiltroDeRelacion(programaIDSelected: String? , list: List<ProgramaDescripcionItem>){

        //Lista temporal de objetos ProgramaDescripcionItem
        val tempList = ArrayList<ProgramaDescripcionItem>()

        //Identificar objetos ProgramaDescriptionItem cuyo id de programa está en la lista programsIdsList
        if(programsIdsList.size > 0) {
            for (i in 0..programsIdsList.size - 1) {
                for (j in 0..list.size - 1) {
                    if (programsIdsList[i].equals(list[j].programaId)) {
                        tempList.add(list[j])
                    }
                }
            }

            //Lista de ids de laboratorios
            val listIdLabsWithProgramsSelected = ArrayList<Int>()

            //Llenamos la lista con los ids de los laboratorios que tienen los programas elegidos
            //Por ejemplo: Python
            if(tempList.isNotEmpty()) {
                for (i in 0..tempList.size - 1) {
                    listIdLabsWithProgramsSelected.add(tempList[i].laboratorioId)
                }
            }

            //Usamos un map para encontrar el laboratorio que cumple con los programas solicitados
            val map = encontrarItemConMayorRepeticiones(listIdLabsWithProgramsSelected)

            var max = 0

            for((key,value) in map){
                if(value > max){
                    max = value
                }
            }

            //Genera una nueva secondTempList usando el map
            //Lista temporal de objetos ProgramaDescripcionItem
            val secondTempList = ArrayList<ProgramaDescripcionItem>()

            for((key,value) in map){
                if(value == max){
                    for(i in 0..rawList.size - 1){
                        if(rawList[i].laboratorioId == key.toInt()){
                            secondTempList.add(rawList[i])
                        }
                    }
                }
            }

            //Generamos una deep copy de la lista secondTempList, acá los items están repetidos. Luego de ello
            //procedemos a realizar el filtro respectivo
            helperList = efectuarFiltroItemsRepetidos(generateNewDeepCopyList(secondTempList), false)
            helperList = helperList.sortedWith(compareBy { it.programaId.toInt() })

        } else {
            helperList = efectuarFiltroItemsRepetidos(rawList, true)
        }

        if(programsIdsList.isNotEmpty()) {
            for (i in 0..programsIdsList.size - 1) {
                for (j in 0..helperList.size - 1) {
                    if (helperList[j].programaId == programsIdsList[i]) {
                        tagsList.add(helperList[j].programaNombre)
                    }
                }
            }
        } else {
            tagsList.clear()
        }

        mAdapterTags = PregradoPrereservaTagsAdapter(tagsList)

        recycler_view_select_software_lab_tags.adapter = mAdapterTags

        if(tagsList.isNotEmpty()) {
            (recycler_view_select_software_lab_tags.layoutManager as androidx.recyclerview.widget.LinearLayoutManager).scrollToPosition(tagsList.size - 1)
        }

        if(helperList.size == sizeSavedOfList){
            lastFirstVisiblePosition = (recycler_view_select_software_lab.layoutManager as androidx.recyclerview.widget.LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            val v = (recycler_view_select_software_lab.layoutManager as androidx.recyclerview.widget.LinearLayoutManager).getChildAt(0)

            if(lastFirstVisiblePosition > 0 && v != null){
                offsetTop = v.top
            }

        }

        mAdapter = PregradoPrereservaSeleccionLabsAdapter(helperList, this, countForAdapter, maxCantidadProgPermitidos)
        recycler_view_select_software_lab.adapter = mAdapter

        if(helperList.size == sizeSavedOfList){
            if(lastFirstVisiblePosition - 1 >= 0 && mAdapter!!.itemCount > 0){
                (recycler_view_select_software_lab.layoutManager as androidx.recyclerview.widget.LinearLayoutManager).scrollToPositionWithOffset(lastFirstVisiblePosition - 1, offsetTop)
            }
        }

        sizeSavedOfList = helperList.size

    }



    fun generateNewDeepCopyList(oldList: List<ProgramaDescripcionItem>): List<ProgramaDescripcionItem>{

        val newDeepCopyList = ArrayList<ProgramaDescripcionItem>()

        if(oldList.isNotEmpty()){
            for (program: ProgramaDescripcionItem in oldList) {
                newDeepCopyList.add(ProgramaDescripcionItem("", "","",false,-2, program))

            }
        }

        return newDeepCopyList
    }



    fun registrarPrereservaLabServicio(url: String, request: JSONObject){

        val fRequest = Utilitarios.jsObjectEncrypted(request, this@PregradoLabsSelectSoftwareActivity)

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                fRequest,
                { response ->
                    if (!response.isNull("RegistrarPreReservaLaboratorioResult")) {
                        val jsResponse = Utilitarios.jsObjectDesencriptar(response.getString("RegistrarPreReservaLaboratorioResult"), this@PregradoLabsSelectSoftwareActivity)

                        progress_bar_select_software_lab.visibility = View.GONE
                        tv_empty_select_software_lab.visibility = View.GONE
                        select_software_lab_container.visibility = View.VISIBLE

                        try {
                            val maquinaId = jsResponse!!.optString("IdMaquina")
                            var mensajeRespuesta = jsResponse.optString("Mensaje")

                            val language = Locale.getDefault().displayLanguage
                            var successMessage = false

                            if(maquinaId.toInt() > 0){
                                prereservaRealizada = true
                                if(language.equals("English")){
                                    mensajeRespuesta = "The pre-reservation was made successfully. Please go to the record section in the laboratories main screen to review the details of the pre-reservation."
                                }
                                successMessage = true
                            } else {
                                prereservaRealizada = false
                                successMessage = false
                                if(mensajeRespuesta.contains("Lo sentimos, no existe disponibilidad de laboratorios")) {
                                    if (language.equals("English")) {
                                        //English
                                        mensajeRespuesta =
                                            "We are sorry to inform you that there are not laboratories available at this moment."
                                    }
                                } else if (mensajeRespuesta.contains("El horario seleccionado se cruza con otra pre-reserva")) {
                                    if (language.equals("English")) {
                                        //English
                                        mensajeRespuesta =
                                            "The hours selected are already in use in another pre-reservation. Please check the record section in the laboratories main screen for details."
                                    }
                                } else if (mensajeRespuesta.contains("Solo tiene disponibles")){
                                    if (language.equals("English")) {
                                        //English
                                        mensajeRespuesta =
                                            "You do not have a valid amount of available hours."
                                    }
                                } else {
                                    if (language.equals("English")) {
                                        //English
                                        mensajeRespuesta = "An error occurred processing your request."
                                    }
                                }


                            }
                            showDialogWithOption(mensajeRespuesta, successMessage)

                        } catch (e: Exception) {
                            tv_empty_select_software_lab.text = getString(R.string.error_recuperacion_datos)
                            tv_empty_select_software_lab.visibility = View.VISIBLE
                            select_software_lab_container.visibility = View.GONE
                            progress_bar_select_software_lab.visibility = View.GONE
                        }

                    }
                },
                { error ->
                    tv_empty_select_software_lab.text = getString(R.string.no_respuesta_desde_servidor)
                    tv_empty_select_software_lab.visibility = View.VISIBLE
                    select_software_lab_container.visibility = View.GONE
                    progress_bar_select_software_lab.visibility = View.GONE
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


    fun showDialogWithOption(mensaje: String, successMessage: Boolean) {

        dialogoMostradoEnPantalla = true

        val builder = if(successMessage) {
            AlertDialog.Builder(this@PregradoLabsSelectSoftwareActivity, R.style.EsanAlertDialogSuccess)
        } else{
            AlertDialog.Builder(this@PregradoLabsSelectSoftwareActivity, R.style.EsanAlertDialog)
        }

        builder.setTitle(getString(R.string.registro_de_prereserva_title))
            .setMessage(mensaje)
            .setPositiveButton(getString(R.string.positive_dialog)) { dialog, which ->
                if(successMessage){
                    val intent = Intent(this@PregradoLabsSelectSoftwareActivity, PregradoLabsPrincipalActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                } else{
                    onBackPressed()
                }
            }
            .setOnCancelListener {
                if(prereservaRealizada){
                    val intent = Intent(this@PregradoLabsSelectSoftwareActivity, PregradoLabsPrincipalActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                } else{
                    if(dialogoMostradoEnPantalla){
                        onBackPressed()
                    }
                }
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()

    }


    override fun itemClick(position: Int, itemSelected: Boolean, programaId: String, laboratorioId: Int) {

        tagsList.clear()

        if(itemSelected){
            //Agrega programa a la lista de items seleccionados
            programsIdsList.add(programaId)
            countForAdapter++
        } else {
            //Remueve programa de la lista de items seleccionados
            if(programsIdsList.size != 0) {
                programsIdsList.remove(programaId)
                countForAdapter--
            }
        }

        mostrarButtonRealizarPrereserva()
        efectuarFiltroDeRelacion("", rawList)
        listItemsUpdated = true
        et_search_field_labs.setText("")

    }

    fun mostrarButtonRealizarPrereserva(){
        //Muestra botón en caso de que la lista tenga por lo menos 1 item
        if(programsIdsList.size != 0){
            prereservar_laboratorio_button.visibility = View.VISIBLE
        } else {
            prereservar_laboratorio_button.visibility = View.GONE
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


    fun encontrarItemConMayorRepeticiones(listToUse: ArrayList<Int>): HashMap<String, Int>{
        val map = HashMap<String, Int>()

        for(i in 0..listToUse.size - 1){
            var counter = 0
            for(j in 0..listToUse.size -1){
                if(listToUse[i] == listToUse[j]){
                    counter++
                }
            }

            if(!map.containsKey(listToUse[i].toString())){
                map.put(listToUse[i].toString(), counter)
            }
        }

        return map
    }

    override fun onStop() {
        super.onStop()
        mRequestQueue?.cancelAll(TAG)
    }
}
