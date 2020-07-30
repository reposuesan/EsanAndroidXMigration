package pe.edu.esan.appostgrado.view.puntosreunion.postgrado.reserva

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.timessquare.CalendarPickerView
import kotlinx.android.synthetic.main.activity_prreserva_primera.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PRHorarioAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.PRConfiguracion
import pe.edu.esan.appostgrado.entidades.PRHorarioReserva
import pe.edu.esan.appostgrado.entidades.PRReserva
import pe.edu.esan.appostgrado.util.Utilitarios
import java.util.*
import kotlin.collections.ArrayList

class PRReservaPrimeraActivity : AppCompatActivity(){

    private val TAG = "PRReservaPrimeraActivity"

    private val LOG = PRReservaPrimeraActivity::class.simpleName

    private var horarioAdapter : PRHorarioAdapter? = null
    private var requestQueue: RequestQueue? = null
    private var requestQueueDispo: RequestQueue? = null

    private var fechaReserva = Date()
    private var puedeContinuar = false
    var horasNoVisibles = ArrayList<PRHorarioReserva>()
    var listaHorarioSelect = ArrayList<PRHorarioReserva>()

    var stateReser = 0

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prreserva_primera)

        val toolbar = main_prreservaprimera as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.reservar)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        if (ControlUsuario.instance.prconfiguracion == null){
            finish()
        } else {
            lblSGrupo_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            lblSDiasAnticipacion_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            lblSHorasDisponibles_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.MEDIUM_ITALIC)
            lblSHorasReserva_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)

            lblGrupo_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            lblDiasAnticipacion_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            lblHorasDisponibles_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.MEDIUM_ITALIC)
            lblHorasReserva_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)

            lblFecha_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            lblHorario_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)

            btnConsultar_prreservaprimera.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)

            lblGrupo_prreservaprimera.text = ControlUsuario.instance.prconfiguracion?.grupo
            lblDiasAnticipacion_prreservaprimera.text = String.format(resources.getString(R.string._dias), ControlUsuario.instance.prconfiguracion?.diasAnticipa)
            lblHorasDisponibles_prreservaprimera.text = String.format(resources.getString(R.string._horas), ControlUsuario.instance.prconfiguracion?.horasUtil)
            lblHorasReserva_prreservaprimera.text = String.format(resources.getString(R.string._horas), ControlUsuario.instance.prconfiguracion?.horasReserva)

            val gridLayout =
                androidx.recyclerview.widget.GridLayoutManager(this, 2)
            rvHorario_prreservaprimera.layoutManager = gridLayout

            txtFecha_prreservaprimera.setText(Utilitarios.getLongToStringddMMyyyy(Date().time, Utilitarios.TipoSeparacion.DIAGONAL))

            txtFecha_prreservaprimera.setOnClickListener {
                openCalendar()
            }

            btnFecha_prreservaprimera.setOnClickListener {
                openCalendar()
            }

            txtFecha_prreservaprimera.addTextChangedListener(object:TextWatcher{
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    getDetalleDisponibilidad()
                    showHorario()
                }
            })

            btnConsultar_prreservaprimera.setOnClickListener {
                if (puedeContinuar)
                    getConsultarDisponibilidad()
                else {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.info_seleccionahoras), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.info))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.info_text))
                    snack.show()
                }
            }

            switchPRPrereservaPrimera!!.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    switchPRPrereservaPrimera!!.text = "Abierto"
                    stateReser = 1
                } else {
                    switchPRPrereservaPrimera!!.text = "Cerrado"
                    stateReser = 0
                }
            }

            showHorario()
        }
    }


    override fun onRestart() {
        super.onRestart()
        if (ControlUsuario.instance.esReservaExitosa) {
            //ControlUsuario.instance.esReservaExitosa = false
            finish()
        }
    }

    private fun getDetalleDisponibilidad() {

        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequestDetalleDisponibilidad()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        Log.w(LOG, "operationFinishedActivityPublic.observe() was called")
                        Log.w(LOG, "sendRequestDetalleDisponibilidad() was called")
                        sendRequestDetalleDisponibilidad()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
            Log.w(LOG, "controlViewModel.getDataFromRoom() was called")

            /*val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_ingreso), Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
            snack.show()*/
        }
    }

    private fun sendRequestDetalleDisponibilidad(){
        val usuario = ControlUsuario.instance.currentUsuario[0] as Alumno

        val request = JSONObject()
        request.put("CodAlumno", usuario.codigo)
        request.put("IdConfiguracion", ControlUsuario.instance.prconfiguracion?.idConfiguracion)
        request.put("Fecha", Utilitarios.getLongToStringddMMyyyy(fechaReserva.time, Utilitarios.TipoSeparacion.DIAGONAL))
        request.put("TipoCubiculo", stateReser)

        val requestEncriptar = Utilitarios.jsObjectEncrypted(request, this)
        if (requestEncriptar != null)
            onDetalleDisponibilidad(Utilitarios.getUrl(Utilitarios.URL.PR_DETALLEGRUPO), ControlUsuario.instance.prconfiguracion!!.idConfiguracion, requestEncriptar)
        else {
            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_encriptar), Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
            snack.show()
        }
    }

    private fun onDetalleDisponibilidad(url: String, idConfiguracion: Int, request: JSONObject) {
        //println(url)
        Log.i(LOG, url)
        //println(request)
        Log.i(LOG, request.toString())
        requestQueue = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    try {
                        if (!response.isNull("ListarHorasxAlumnoResult")) {
                            val detalleJArray = Utilitarios.jsArrayDesencriptar(response["ListarHorasxAlumnoResult"] as String, this)
                            if (detalleJArray != null) {
                                if (detalleJArray.length() == 1) {
                                    val detalleJObject = detalleJArray[0] as JSONObject
                                    val grupo = detalleJObject["NomGrupo"] as String
                                    val cantHorasAntici = detalleJObject["CantHorasAnticipa"] as Int
                                    val cantHorasReserv = detalleJObject["CantHorasReserva"] as Int
                                    val cantHorasRestan = cantHorasReserv - detalleJObject["CantHorasUtil"] as Int

                                    ControlUsuario.instance.prconfiguracion = PRConfiguracion(idConfiguracion, grupo, cantHorasAntici, cantHorasReserv, cantHorasRestan)

                                    lblHorasDisponibles_prreservaprimera.text = String.format(resources.getString(R.string._horas), cantHorasRestan)

                                } else {
                                    lblHorasDisponibles_prreservaprimera.text = String.format(resources.getString(R.string._horas), 0)
                                    ControlUsuario.instance.prconfiguracion?.horasUtil = 0

                                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_permiso_opcion), Snackbar.LENGTH_LONG)
                                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                    snack.show()
                                }
                            } else {
                                lblHorasDisponibles_prreservaprimera.text = String.format(resources.getString(R.string._horas), 0)
                                ControlUsuario.instance.prconfiguracion?.horasUtil = 0

                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_desencriptar), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        } else {
                            lblHorasDisponibles_prreservaprimera.text = String.format(resources.getString(R.string._horas), 0)
                            ControlUsuario.instance.prconfiguracion?.horasUtil = 0

                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_permiso_opcion), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                            snack.show()
                        }
                    } catch (jex: JSONException) {
                        lblHorasDisponibles_prreservaprimera.text = String.format(resources.getString(R.string._horas), 0)
                        ControlUsuario.instance.prconfiguracion?.horasUtil = 0

                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                    prbCargando_prreservaprimera.visibility = View.GONE
                    rvHorario_prreservaprimera.visibility = View.VISIBLE
                },
                Response.ErrorListener { error ->
                    //println(error.message)
                    Log.e(LOG, error.message.toString())
                    prbCargando_prreservaprimera.visibility = View.GONE
                    rvHorario_prreservaprimera.visibility = View.VISIBLE

                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
        )
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }





    private fun showHorario(){
        puedeContinuar = false
        btnConsultar_prreservaprimera.background = ContextCompat.getDrawable(this, R.drawable.btn_desactivo)
        val fecha = Date()
        var inicio = 0
        var emergencia = false
        if (Utilitarios.getLongToStringddMMyyyy(fechaReserva.time, Utilitarios.TipoSeparacion.DIAGONAL) == Utilitarios.getLongToStringddMMyyyy(fecha.time, Utilitarios.TipoSeparacion.DIAGONAL)) {
            val hora = Utilitarios.getLongToStringHHmm(fecha.time)
            val hh = hora.split(":")[0]
            inicio = hh.toInt()
            emergencia = true
        }

        horarioAdapter = PRHorarioAdapter(this, getHorario(inicio, emergencia)) { prHorarioReserva, view, position, lista ->
            if (prHorarioReserva.seleccionado) {
                if (listaHorarioSelect.size >= 3) {// ControlUsuario.instance.prconfiguracion!!.horasUtil) {
                    var mayor = 0
                    var menor = 0
                    //var mayorpos = 0
                    //var menorpos = 0
                    var contador = 0
                    for (h in listaHorarioSelect) {
                        val idActual = h.id
                        if (contador == 0) {
                            mayor = idActual
                            menor = idActual
                            //mayorpos = contador
                            //menorpos = contador
                        } else {
                            if (mayor > idActual) {
                                if (menor > idActual) {
                                    menor = idActual
                                    //menorpos = contador
                                }
                            } else {
                                mayor = idActual
                                //mayorpos = contador
                            }
                        }
                        contador++
                    }

                    if (prHorarioReserva.id == menor || prHorarioReserva.id == mayor) {
                        prHorarioReserva.seleccionado = false
                        updateAdapter(position)
                        for (h in listaHorarioSelect) {
                            if (prHorarioReserva.id == h.id) {
                                listaHorarioSelect.remove(h)
                                break
                            }
                        }
                    } else {
                        val snack = Snackbar.make(view, resources.getString(R.string.error_prhoracentar), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                        snack.show()
                    }

                } else {
                    prHorarioReserva.seleccionado = false
                    updateAdapter(position)
                    for (h in listaHorarioSelect) {
                        if (prHorarioReserva.id == h.id) {
                            listaHorarioSelect.remove(h)
                            break
                        }
                    }
                    if (listaHorarioSelect.size == 0) {
                        btnConsultar_prreservaprimera.background = ContextCompat.getDrawable(this, R.drawable.btn_desactivo)
                        puedeContinuar = false
                    }
                }
            } else {
                if (listaHorarioSelect.size < ControlUsuario.instance.prconfiguracion!!.horasUtil) {

                    var valido = false

                    for (i in listaHorarioSelect) {
                        if (prHorarioReserva.id == i.id + 1 || prHorarioReserva.id == i.id - 1)
                            valido = true
                    }

                    if (valido || listaHorarioSelect.size == 0) {
                        prHorarioReserva.seleccionado = true
                        listaHorarioSelect.add(prHorarioReserva)
                        updateAdapter(position)
                        btnConsultar_prreservaprimera.background = ContextCompat.getDrawable(this, R.drawable.btn_activo)
                        puedeContinuar = true
                    } else {
                        val snack = Snackbar.make(view, resources.getString(R.string.error_prhoraconsecutiva), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                        snack.show()
                    }
                } else if (listaHorarioSelect.size == ControlUsuario.instance.prconfiguracion!!.horasUtil) {
                    var mayor = 0
                    var menor = 0
                    var mayorpos = 0
                    var menorpos = 0
                    var contador = 0

                    for (h in listaHorarioSelect) {
                        val idActual = h.id
                        if (contador == 0) {
                            mayor = idActual
                            menor = idActual
                            mayorpos = contador
                            menorpos = contador
                        } else {
                            if (mayor > idActual) {
                                if (menor > idActual) {
                                    menor = idActual
                                    menorpos = contador
                                }
                            } else {
                                mayor = idActual
                                mayorpos = contador
                            }
                        }
                        contador++
                    }

                    if (prHorarioReserva.id == menor-1) {
                        prHorarioReserva.seleccionado = true
                        updateAdapter(position)
                        lista[listaHorarioSelect[mayorpos].posicion].seleccionado = false
                        updateAdapter(listaHorarioSelect[mayorpos].posicion)
                        listaHorarioSelect.add(prHorarioReserva)
                        listaHorarioSelect.removeAt(mayorpos)
                    } else if (prHorarioReserva.id == mayor+1) {
                        prHorarioReserva.seleccionado = true
                        updateAdapter(position)
                        lista[listaHorarioSelect[menorpos].posicion].seleccionado = false
                        updateAdapter(listaHorarioSelect[menorpos].posicion)
                        listaHorarioSelect.add(prHorarioReserva)
                        listaHorarioSelect.removeAt(menorpos)
                    } else {
                        val snack = Snackbar.make(view, resources.getString(R.string.error_prsuperohoras), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                        snack.show()
                    }
                }
            }
        }

        rvHorario_prreservaprimera.adapter = horarioAdapter
        listaHorarioSelect = ArrayList()
    }


    private fun getHorario(inicio: Int, emergencia: Boolean): List<PRHorarioReserva> {
        val listaHorario = ArrayList<PRHorarioReserva>()
        var pos = 0
        var contEmer = 0
        horasNoVisibles = ArrayList()
        for (i in inicio..23) {
            /*var hIinico : String
              var hFin : String
                if (i >= 12) {
                if (i+1 == 24) {
                    hIinico = String.format(resources.getString(R.string.horapm), i-12)
                    hFin = String.format(resources.getString(R.string.horaam), i-12+1)
                } else {
                    hIinico = String.format(resources.getString(R.string.horapm), if (i-12 == 0) 12 else i-12)
                    hFin = String.format(resources.getString(R.string.horapm), i-12+1)
                }
            } else {
                if (i+1 == 12) {
                    hIinico = String.format(resources.getString(R.string.horaam), i)
                    hFin = String.format(resources.getString(R.string.horapm), i+1)
                } else {
                    hIinico = String.format(resources.getString(R.string.horaam), i)
                    hFin = String.format(resources.getString(R.string.horaam), i+1)
                }
            }*/

            val hInicio = determinarHora(i)
            val hFin = determinarHora(i+1)

            val horario = PRHorarioReserva(i, hInicio, hFin, i, (i+1))

            if (emergencia) {
                if (contEmer < 2) {
                    if (contEmer == 0)
                        horario.emergencia = true
                    horasNoVisibles.add(horario)
                } else {
                    horario.posicion = pos++
                    listaHorario.add(horario)
                }
                contEmer += 1
            } else {
                horasNoVisibles.clear()
                horario.posicion = pos++
                listaHorario.add(horario)
            }
        }

        return listaHorario
    }


    private fun getConsultarDisponibilidad() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
           sendRequestConsultarDisponibilidad()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        Log.w(LOG, "operationFinishedActivityPublic.observe() was called")
                        Log.w(LOG, "sendRequestConsultarDisponibilidad() was called")
                        sendRequestConsultarDisponibilidad()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
            Log.w(LOG, "controlViewModel.getDataFromRoom() was called")

            /*val snack= Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_ingreso), Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
            snack.show()*/
        }
    }

    private fun sendRequestConsultarDisponibilidad(){
        val user = ControlUsuario.instance.currentUsuario[0]
        when (user) {
            is Alumno -> {
                val listaOrdenada = listaHorarioSelect.sortedWith(compareBy { it.id })

                val horaInicio = String.format("%02d", listaOrdenada[0].inicio)
                val horaFin = String.format("%02d", listaOrdenada[listaOrdenada.size-1].fin)
                val fechaReserva = Utilitarios.getLongToStringddMMyyyy(fechaReserva.time, Utilitarios.TipoSeparacion.DIAGONAL)

                val request = JSONObject()
                request.put("CodAlumno", user.codigo)
                request.put("Fecha", fechaReserva)
                request.put("HoraIni", horaInicio)
                request.put("HoraFin", horaFin)
                request.put("TipoCubiculo", stateReser)
                request.put("IdConfiguracion", ControlUsuario.instance.prconfiguracion?.idConfiguracion)

                val requestEncritado = Utilitarios.jsObjectEncrypted(request, this)
                if (requestEncritado != null) {
                    onConsultarDisponibilidad(Utilitarios.getUrl(Utilitarios.URL.PR_DISPONIBILIDAD), requestEncritado, user.codigo, user.nombreCompleto)
                } else {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_encriptar), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
            }
        }
    }

    private fun onConsultarDisponibilidad(url: String, request: JSONObject, codigo: String, nombre: String) {
        //println(url)
        Log.i(LOG, url)
        //println(request)
        Log.i(LOG, request.toString())
        requestQueue = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    try {
                        if (!response.isNull("DisponibilidadSalaResult")) {
                            val respuesta = Utilitarios.stringDesencriptar(response["DisponibilidadSalaResult"] as String, this)
                            if (respuesta != null) {
                                val reserva = PRReserva(
                                        codigo,
                                        nombre,
                                        Utilitarios.getLongToStringddMMyyyy(fechaReserva.time, Utilitarios.TipoSeparacion.DIAGONAL),
                                        fechaReserva.time,
                                        respuesta == "True",
                                        listaHorarioSelect,
                                        horasNoVisibles,
                                        horarioAdapter?.listaHorario)

                                ControlUsuario.instance.prreserva = reserva

                                val intentSegunda = Intent(this, PRReservaSegundaActivity::class.java).putExtra("TipoCubiculo", stateReser)

                                startActivity(intentSegunda)
                            } else {
                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_respuesta_server), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        } else {
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                            snack.show()
                        }
                    }catch (jex: JSONException) {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                },
                Response.ErrorListener { error ->
                    //print(error.message)
                    Log.e(LOG, error.message.toString())
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
        )
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }

    private fun openCalendar() {
        val dayafter = Calendar.getInstance()
        dayafter.add(Calendar.DAY_OF_MONTH, ControlUsuario.instance.prconfiguracion!!.diasAnticipa)

        val datePicker = layoutInflater.inflate(R.layout.item_calentar, null, false) as CalendarPickerView
        val dialog = AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.fecha_reserva))
                .setView(datePicker)
                .setPositiveButton(resources.getString(R.string.ok)) { dialogInterface, i ->
                    val fechaActual = Utilitarios.getLongToStringddMMyyyy(fechaReserva.time, Utilitarios.TipoSeparacion.DIAGONAL)
                    val fechaNueva = Utilitarios.getLongToStringddMMyyyy(datePicker.selectedDate.time, Utilitarios.TipoSeparacion.DIAGONAL)

                    if (fechaActual != fechaNueva) {
                        fechaReserva = datePicker.selectedDate
                        txtFecha_prreservaprimera.setText(Utilitarios.getLongToStringddMMyyyy(fechaReserva.time, Utilitarios.TipoSeparacion.DIAGONAL))
                    }
                }
            .setNegativeButton(resources.getString(R.string.cancelar)) { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create()
        dialog.setOnShowListener {
            datePicker.fixDialogDimens()
        }
        dialog.show()

        datePicker.init(Date(), dayafter.time, Locale(resources.getString(R.string.lenguaje), resources.getString(R.string.pais)))
                .inMode(CalendarPickerView.SelectionMode.SINGLE)
                .withSelectedDate(fechaReserva)
    }

    private fun updateAdapter (position: Int) {
        horarioAdapter?.notifyItemChanged(position)
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        requestQueueDispo?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun determinarHora(horaRecibida: Int): String {

        return when (horaRecibida) {
            0 -> "00:00"
            1 -> "01:00"
            2 -> "02:00"
            3 -> "03:00"
            4 -> "04:00"
            5 -> "05:00"
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
}
