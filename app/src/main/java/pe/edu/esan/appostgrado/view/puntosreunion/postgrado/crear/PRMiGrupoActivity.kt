package pe.edu.esan.appostgrado.view.puntosreunion.postgrado.crear

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_mi_grupo.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.MiGrupoAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios

class PRMiGrupoActivity : AppCompatActivity() {

    private val TAG = "PRMiGrupoActivity"
    private var requestQueue : RequestQueue? = null
    private var requestQueueGrupo : RequestQueue? = null
    private var requestQueueEliminar: RequestQueue? = null
    private var menu : Menu? = null

    private var adapter : MiGrupoAdapter? = null

    private var puedeEliminarAgregar = false
    private var creaGrupo = false
    private var cambio = false

    private val LOG = PRMiGrupoActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_grupo)

        val toolbar = main_prmigrupo as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.migrupo)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        ControlUsuario.instance.currentMiGrupo = ArrayList()

        if (ControlUsuario.instance.prPromocionConfig == null){
            finish()
        } else {
            lblMensaje_prmigrupo.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
            rvGrupo_migrupo.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this)
            rvGrupo_migrupo.adapter = null

            getVerificarGrupo()
        }
    }

    private fun getVerificarGrupo() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
          sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        Log.w(LOG, "operationFinishedActivityPublic.observe() was called")
                        Log.w(LOG, "sendRequest() was called")
                        sendRequest()
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

    private fun sendRequest(){
        val user = ControlUsuario.instance.currentUsuario[0]
        when (user) {
            is Alumno -> {
                val request = JSONObject()
                request.put("CodAlumno", user.codigo)
                request.put("IdConfiguracion", ControlUsuario.instance.prPromocionConfig?.idConfiguracion)

                val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                if (requestEncriptado != null) {
                    onVerificarGrupo(Utilitarios.getUrl(Utilitarios.URL.PR_VERIFICAR_GRUPO), requestEncriptado)
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

    private fun onVerificarGrupo(url: String, request: JSONObject) {
        prbCargando_prmigrupo.visibility = View.VISIBLE

        requestQueue = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    prbCargando_prmigrupo.visibility = View.GONE
                    try {
                        if (!response.isNull("VerificarGrupoResult")) {
                            val datosGrupoJson = Utilitarios.jsObjectDesencriptar(response["VerificarGrupoResult"] as String, this)
                            if (datosGrupoJson != null) {
                                val idGrupo = datosGrupoJson["IdGrupo"] as Int
                                val esAdmin = datosGrupoJson["EsAdmin"] as Int
                                if (idGrupo == 0) {
                                    rvGrupo_migrupo.adapter = MiGrupoAdapter(ArrayList(), puedeEliminarAgregar, ControlUsuario.instance.prPromocionConfig?.cantMaxGrupo ?: 0, {codigo, position ->  })
                                    this.menu?.findItem(R.id.action_eliminar_crear)?.setTitle(resources.getString(R.string.crear))
                                    this.creaGrupo = true
                                    ControlUsuario.instance.prPromocionConfig?.idGrupoEstudio = 0
                                } else {
                                    ControlUsuario.instance.prPromocionConfig?.idGrupoEstudio = idGrupo
                                    println("LISTAR GRUPO")
                                    getMiGrupo(idGrupo)
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
                    } catch (caax: ClassCastException) {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }

                },
                Response.ErrorListener { error ->
                    prbCargando_prmigrupo.visibility = View.GONE
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

    private fun getMiGrupo(idGrupo: Int){

        val request = JSONObject()
        request.put("IdGrupoReserva", idGrupo)

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
        if (requestEncriptado != null) {
            onMiGrupo(Utilitarios.getUrl(Utilitarios.URL.PR_MI_GRUPO), requestEncriptado)
        } else {
            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_encriptar), Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
            snack.show()
        }

    }

    private fun onMiGrupo(url: String, request: JSONObject) {
        prbCargando_prmigrupo.visibility = View.VISIBLE

        requestQueueGrupo = Volley.newRequestQueue(this)
        val jsonObjectResquest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    prbCargando_prmigrupo.visibility = View.GONE
                    try {
                        if (!response.isNull("ListarAlumnoDeGrupoResult")) {
                            val datosMiGrupoJArray = Utilitarios.jsArrayDesencriptar(response["ListarAlumnoDeGrupoResult"] as String, this)

                            if (datosMiGrupoJArray != null) {
                                var codigoActual = ""
                                val user = ControlUsuario.instance.currentUsuario[0]
                                when (user) {
                                    is Alumno -> {
                                        codigoActual = user.codigo
                                    }
                                }

                                val listaMiGrupo = ArrayList<Alumno>()
                                for (z in 0 until datosMiGrupoJArray.length()) {
                                    val miGrupoJson = datosMiGrupoJArray[z] as JSONObject
                                    val idActor = miGrupoJson["IdActor"] as Int
                                    val codigo = miGrupoJson["Codigo"] as String
                                    val nombre = miGrupoJson["NombreCompleto"] as String
                                    val esCreador = miGrupoJson["Creador"] as Int
                                    println(miGrupoJson)
                                    val alumno = Alumno(codigo, idActor, nombre, esCreador==1)
                                    if (alumno.esCreador){
                                        if (codigoActual == codigo) {
                                            this.menu?.findItem(R.id.action_eliminar_crear)?.setTitle(resources.getString(R.string.eliminar))
                                            this.puedeEliminarAgregar = true
                                        }

                                    }
                                    listaMiGrupo.add(alumno)
                                }
                                println("CANTIDAD MAXIMA")
                                println(ControlUsuario.instance.prPromocionConfig?.cantMaxGrupo)
                                adapter = MiGrupoAdapter(listaMiGrupo, puedeEliminarAgregar, ControlUsuario.instance.prPromocionConfig?.cantMaxGrupo ?: 0) { codigo, position ->
                                    println("Eliminar $codigo - $position")
                                    setEliminarAlumnoGrupo(ControlUsuario.instance.prPromocionConfig?.idGrupoEstudio, codigo, position)
                                }
                                rvGrupo_migrupo.adapter = adapter
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
                    } catch (caax: ClassCastException) {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                },
                Response.ErrorListener { error ->
                    prbCargando_prmigrupo.visibility = View.GONE
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
        )
        jsonObjectResquest.tag = TAG
        requestQueueGrupo?.add(jsonObjectResquest)
    }

    fun setEliminarAlumnoGrupo(idGrupo: Int?, codigoAlumnoElimina: String, position: Int) {
        if (idGrupo != null) {
            if (ControlUsuario.instance.currentUsuario.size == 1) {
                val user = ControlUsuario.instance.currentUsuario[0]
                when (user) {
                    is Alumno -> {
                        val request = JSONObject()
                        request.put("IdGrupoReserva", idGrupo)
                        request.put("CodAlumnoGrupo", codigoAlumnoElimina)
                        request.put("CodAlumnoCrea", user.codigo)
                        println(request)
                        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                        if (requestEncriptado != null) {
                            onEliminarAlumnoGrupo(Utilitarios.getUrl(Utilitarios.URL.PR_ELIMINAR_ALUMNOGRUPO), requestEncriptado, position)
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
        } else {
            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_valores), Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
            snack.show()
        }
    }

    fun onEliminarAlumnoGrupo(url: String, request: JSONObject, position: Int) {
        println(url)
        println(request)
        CustomDialog.instance.showDialogLoad(this)

        requestQueueEliminar = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    CustomDialog.instance.dialogoCargando?.dismiss()
                    try {
                        if (!response.isNull("EliminarAlumnoGrupoResult")) {
                            val respuesta = Utilitarios.stringDesencriptar(response["EliminarAlumnoGrupoResult"] as String, this)
                            if (respuesta != null) {

                                if (respuesta.toInt() == 1) {
                                    eliminarAlumnoLista(position)

                                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.exito_alumno_eliminado), Snackbar.LENGTH_LONG)
                                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.success))
                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.success_text))
                                    snack.show()
                                } else if (respuesta.toInt() == 2) {
                                    val dialog = AlertDialog.Builder(this)
                                            .setTitle(resources.getString(R.string.mensaje))
                                            .setMessage(resources.getString(R.string.exito_elimino_grupo))
                                            .setCancelable(false)
                                            .setPositiveButton(resources.getText(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                                                ControlUsuario.instance.currentMiGrupo = ArrayList()
                                                ControlUsuario.instance.prPromocionConfig?.idGrupoEstudio = 0
                                                finish()
                                            })
                                            .create()
                                    dialog.show()
                                    dialog.getButton(android.app.Dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.esan_rojo))
                                    dialog.findViewById<TextView>(android.R.id.message)?.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
                                } else {
                                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.advertencia_eliminar_alumno_grupo), Snackbar.LENGTH_LONG)
                                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
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
                    } catch (caax: ClassCastException) {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                },
                Response.ErrorListener { error ->
                    CustomDialog.instance.dialogoCargando?.dismiss()
                    val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                    snack.show()
                }
        )
        jsObjectRequest.tag = TAG
        requestQueueEliminar?.add(jsObjectRequest)
    }

    fun cambiarEliminarHecho(estado: Boolean) {
        adapter?.agregarQuitarEliminar(estado)
    }

    fun eliminarAlumnoLista(position: Int) {
        adapter?.eliminarAlumnoLista(position)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_migrupo, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_eliminar_crear -> {
                //setAsistencia()
                if (creaGrupo) {
                    ///Ir a crear
                    val intentCrear = Intent(this, PRCrearGrupoActivity::class.java)
                    startActivity(intentCrear)
                }else {
                    if (puedeEliminarAgregar) {
                        //Cambia vista Eliminar
                        cambio = !cambio
                        if (cambio)
                            this.menu?.findItem(R.id.action_eliminar_crear)?.setTitle(resources.getString(R.string.hecho))
                        else
                            this.menu?.findItem(R.id.action_eliminar_crear)?.setTitle(resources.getString(R.string.eliminar))

                        cambiarEliminarHecho(cambio)
                    }
                }

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        if (ControlUsuario.instance.creoMomificoGrupo) {
            ControlUsuario.instance.creoMomificoGrupo = false
            finish()
        } else {
            getVerificarGrupo()
        }
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        requestQueueGrupo?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
    }

    override fun onDestroy() {
        if(CustomDialog.instance.dialogoCargando != null){
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
        super.onDestroy()
    }
}
