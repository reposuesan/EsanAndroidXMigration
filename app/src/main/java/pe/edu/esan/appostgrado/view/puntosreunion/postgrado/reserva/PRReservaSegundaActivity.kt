package pe.edu.esan.appostgrado.view.puntosreunion.postgrado.reserva

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_prreserva_segunda.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PROtrosHorariosAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.PRHorarioOtro
import pe.edu.esan.appostgrado.entidades.PRHorarioReserva
import pe.edu.esan.appostgrado.entidades.PRReserva
import pe.edu.esan.appostgrado.util.Utilitarios

class PRReservaSegundaActivity : AppCompatActivity() {
    private val TAG = "PRReservaSegundaActivity"

    private val LOG = PRReservaSegundaActivity::class.simpleName

    private var otrosHorariosAdapter : PROtrosHorariosAdapter? = null
    private var contador = 0
    private var otroshorariosmostrados = false

    private var requestQueue: RequestQueue? = null
    private var requestQueueConfirmar: RequestQueue? = null
    var stateReser = 0

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prreserva_segunda)

        val bundle=intent.extras
        if(bundle!=null) {
            stateReser = bundle.getInt("TipoCubiculo")
        }
        //println("TipoCubiculo")
        //println(stateReser)
        Log.i(LOG,"TipoCubiculo: ${stateReser}")

        val toolbar = main_prreservasegunda as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.reservar)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        val reserva = ControlUsuario.instance.prreserva
        if (reserva != null) {
            lblSDetalle_itemprreservasegunda.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.MEDIUM)
            lblSFecha_itemprreservasegunda.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            lblSHora_itemprreservasegunda.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            lblSReservaPor_itemprreservasegunda.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)

            lblFecha_itemprreservasegunda.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            lblHora_itemprreservasegunda.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            lblReservaPor_itemprreservasegunda.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            lblEstado_itemprreservasegunda.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)

            lblReservaPor_itemprreservasegunda.text = reserva.nombre
            lblFecha_itemprreservasegunda.text = reserva.fecha
            var hinicio = ""
            var hfin = ""
            var shora = ""

            for (h in reserva.listaHorario) {
                if (contador == 0) {
                    shora = "${h.horainicio} - ${h.horafin}"
                    hinicio = String.format("%02d", h.inicio)
                    if (contador == reserva.listaHorario.size - 1) {
                        hfin = String.format("%02d", h.fin)
                    }
                } else {
                    shora += "\n${h.horainicio} - ${h.horafin}"
                    hfin = String.format("%02d", h.fin)
                }
                contador++
            }
            lblHora_itemprreservasegunda.text = shora
            if (reserva.disponible) {
                lblEstado_itemprreservasegunda.setTextColor(ContextCompat.getColor(this, R.color.green))
                lblEstado_itemprreservasegunda.text = resources.getString(R.string.disponible)
            } else {
                lblEstado_itemprreservasegunda.setTextColor(ContextCompat.getColor(this, R.color.rojo_oscuro))
                lblEstado_itemprreservasegunda.text = resources.getString(R.string.no_disponible)
            }

            btnConfirmar_itemprreservasegunda.setOnClickListener { setConfirmarReserva(reserva.fecha, hinicio, hfin, reserva.listaHorario.size, 0) }
            showOtrosHorarios(reserva, contador)

        } else {
            finish()
        }
    }

    private fun setConfirmarReserva(fecha: String, hinicio: String, hfin: String, cantHoras: Int, emergencia: Int) {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
           sendRequest(fecha, hinicio, hfin, cantHoras, emergencia)
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        Log.w(LOG, "operationFinishedActivityPublic.observe() was called")
                        Log.w(LOG, "sendRequest() was called")
                        sendRequest(fecha, hinicio, hfin, cantHoras, emergencia)
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

    private fun sendRequest(fecha: String, hinicio: String, hfin: String, cantHoras: Int, emergencia: Int){
        val user = ControlUsuario.instance.currentUsuario[0]
        val ApiAndroid : String?  = android.os.Build.VERSION.SDK_INT.toString()
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val version = pInfo.versionName
        when (user) {
            is Alumno -> {
                val request = JSONObject()
                request.put("Fecha", fecha)
                request.put("HoraIni", hinicio)
                request.put("HoraFin", hfin)
                request.put("CantHoras", cantHoras)
                request.put("EstEmergencia", emergencia)
                request.put("CodAlumno", user.codigo)
                request.put("TipoCubiculo", stateReser)
                request.put("Dispositivo", "Android-" + version+ "-"+ApiAndroid)
                request.put("IdConfiguracion", ControlUsuario.instance.prconfiguracion?.idConfiguracion)
                //println(request.toString())
                Log.i(LOG, request.toString())
                val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                if (requestEncriptado != null)
                    onConfirmarReserva(Utilitarios.getUrl(Utilitarios.URL.PR_REGISTRAR_RESERVA), requestEncriptado)
                else {
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_encriptar), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
            }
        }
    }

    private fun onConfirmarReserva(url: String, request: JSONObject) {
        //println(url)
        Log.i(LOG, url)
        //println(request)
        Log.i(LOG, request.toString())
        //prbCargando_itemprreservasegunda.visibility = View.VISIBLE
        CustomDialog.instance.showDialogLoad(this)
        btnConfirmar_itemprreservasegunda.isEnabled = false
        requestQueueConfirmar = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    //prbCargando_itemprreservasegunda.visibility = View.GONE
                    CustomDialog.instance.dialogoCargando?.dismiss()
                    btnConfirmar_itemprreservasegunda.isEnabled = true
                    try {
                        //println(response)
                        Log.i(LOG, response.toString())
                        if (!response.isNull("RegistraReservaAmbienteResult")) {
                            val jsRespuesta = Utilitarios.stringDesencriptar(response["RegistraReservaAmbienteResult"] as String, this)
                            if (jsRespuesta != null) {
                                val arrayRespuesta = jsRespuesta.split("|")
                                //println(arrayRespuesta.toString())
                                Log.i(LOG, arrayRespuesta.toString())
                                val alertaMensaje = AlertDialog.Builder(this)
                                        .setTitle(resources.getString(R.string.mensaje))
                                        .setMessage(arrayRespuesta[1])
                                        .setPositiveButton(resources.getString(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                                            ControlUsuario.instance.esReservaExitosa = (arrayRespuesta[0] != "00" || arrayRespuesta[0] != "0")
                                            finish()
                                        })
                                        .create()
                                alertaMensaje.show()
                            } else {
                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_desencriptar), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                                snack.show()
                            }
                        } else {
                            val jsRespuesta = Utilitarios.stringDesencriptar(response["RegistraReservaAmbienteResult"] as String, this)
                            //println(jsRespuesta)
                            Log.i(LOG, jsRespuesta)
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                            snack.show()
                        }
                    } catch (jex: JSONException) {
                        val jsRespuesta = Utilitarios.stringDesencriptar(response["RegistraReservaAmbienteResult"] as String, this)
                        //println(jsRespuesta)
                        Log.e(LOG, jsRespuesta)
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                },
                Response.ErrorListener { error ->
                    Log.e("message", error.message.toString())
                    //prbCargando_itemprreservasegunda.visibility = View.GONE
                    CustomDialog.instance.dialogoCargando?.dismiss()
                    btnConfirmar_itemprreservasegunda.isEnabled = true
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
        )
        jsObjectRequest.tag = TAG
        jsObjectRequest.setRetryPolicy( DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            );
        requestQueueConfirmar?.add(jsObjectRequest)

    }

    private fun showOtrosHorarios(reserva: PRReserva, cantHoras: Int) {
        rvOtroHorarios_itemprreservasegunda.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        otrosHorariosAdapter = PROtrosHorariosAdapter(this, ArrayList()) { tipo, horario ->
            if (tipo == 0) {
                if (otroshorariosmostrados == false) {
                    val nuevaLista = ArrayList<PRHorarioReserva>()
                    var construir = true
                    var contador = 0
                    do {
                        if (reserva.listaNoSelect != null) {
                            val hr = reserva.listaNoSelect.get(contador)
                            if (hr.posicion == reserva.listaHorario[reserva.listaHorario.size - 1].posicion) {
                                construir = false
                            } else {
                                nuevaLista.add(hr)
                            }
                            contador++
                            if (reserva.listaNoSelect.size - 1 == contador) {
                                construir = false
                            }
                        } else {
                            construir = false
                        }
                    } while (construir)

                    if (reserva.listaEmergencia.isNotEmpty()) {
                        nuevaLista.addAll(0, reserva.listaEmergencia)
                    }

                    val numeroHorarios = nuevaLista.size - cantHoras + 1
                    val jsArray = JSONArray()
                    for (z in 0 until numeroHorarios) {
                        val jsHoras = JSONObject()
                        for (x in 0..cantHoras) {
                            if (x == 0) {
                                jsHoras.put("Fecha", Utilitarios.getLongToStringddMMyyyy(reserva.time, Utilitarios.TipoSeparacion.GUION))
                                jsHoras.put("HoraIni", String.format("%02d", nuevaLista[x].inicio))
                                jsHoras.put("HInicio", nuevaLista[x].horainicio)
                                jsHoras.put("Emergencia", if (nuevaLista[x].emergencia) 1 else 0)
                                jsHoras.put("IdConfiguracion", ControlUsuario.instance.prconfiguracion?.idConfiguracion)
                            }

                            if (x == cantHoras - 1) {
                                jsHoras.put("HoraFin", String.format("%02d", nuevaLista[x].fin))
                                jsHoras.put("HFin", nuevaLista[x].horafin)
                            }
                        }
                        jsArray.put(jsHoras)
                        nuevaLista.removeAt(0)
                    }

                    val request = Utilitarios.jsArrayEncrypted(jsArray, this)
                    if (request != null)
                        onOtrosHorarios(Utilitarios.getUrl(Utilitarios.URL.PR_OTROSHORARIOS), request, cantHoras)
                    else {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_encriptar), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                        snack.show()
                    }
                }
            } else {
                if (horario != null) {
                    //println(horario.horaInicio)
                    Log.i(LOG, horario.horaInicio)
                    val confirmarCambioDialog = AlertDialog.Builder(this)
                        .setTitle(resources.getString(R.string.confirmar_reserva))
                        .setMessage(String.format(resources.getString(R.string.mensajeconfirmacion_cambiohora), horario.horaInicio, horario.horaFin))
                        .setPositiveButton(resources.getString(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                            val fecha = Utilitarios.getLongToStringddMMyyyy(horario.time, Utilitarios.TipoSeparacion.DIAGONAL)

                            setConfirmarReserva(fecha, horario.inicio, horario.fin, horario.cantHoras, horario.emergencia)
                        })
                        .setNegativeButton(resources.getString(R.string.cancelar), null)
                        .create()

                    confirmarCambioDialog.show()
                }
            }
        }
        rvOtroHorarios_itemprreservasegunda.adapter = otrosHorariosAdapter
    }

    private fun onOtrosHorarios(url: String, request: JSONObject, cantHoras: Int) {
        requestQueue = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    try {
                        if (!response.isNull("ListarDisponibilidadOtrasSalaResult")) {
                            val jsarrayHorarios = Utilitarios.jsArrayDesencriptar(response["ListarDisponibilidadOtrasSalaResult"] as String, this)
                            //println(jsarrayHorarios)
                            Log.i(LOG, jsarrayHorarios.toString())
                            if (jsarrayHorarios != null) {
                                val listaOtroHorarios = ArrayList<PRHorarioOtro>()
                                for (i in 0 until jsarrayHorarios.length()) {
                                    val jsobjHorario = jsarrayHorarios[i] as JSONObject
                                    if (jsobjHorario["Estado"] as Boolean) {
                                        val horaIni = jsobjHorario["HoraIni"] as String
                                        val horaFin = jsobjHorario["HoraFin"] as String
                                        val fecha = jsobjHorario["Fecha"] as String
                                        val hInicio = jsobjHorario["HInicio"] as String
                                        val hFin = jsobjHorario["HFin"] as String
                                        val emergencia = jsobjHorario["Emergencia"] as Int

                                        listaOtroHorarios.add(PRHorarioOtro(horaIni, horaFin, fecha, ControlUsuario.instance.prreserva!!.time, hInicio, hFin, cantHoras, emergencia))
                                    }
                                }
                                if (listaOtroHorarios.isNotEmpty()) {
                                    otroshorariosmostrados = true
                                    otrosHorariosAdapter?.addHorarios(listaOtroHorarios)
                                } else {
                                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_respuesta_server), Snackbar.LENGTH_LONG)
                                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.info))
                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.info_text))
                                    snack.show()
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
                    }
                },
                Response.ErrorListener { error ->
                    //println(error.message)
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

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        requestQueueConfirmar?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
    }

    override fun onDestroy() {
        if(CustomDialog.instance.dialogoCargando != null){
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
        super.onDestroy()
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
}
