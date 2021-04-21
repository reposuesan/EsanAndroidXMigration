package pe.edu.esan.appostgrado.view.puntosreunion.postgrado.crear

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.activity_prcrear_grupo.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.CreaGrupoMiGrupoAdapter
import pe.edu.esan.appostgrado.adapter.CreaGrupoSinGrupoAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class PRCrearGrupoActivity : AppCompatActivity() {

    private val TAG = "PRCrearGrupoActivity"
    private var requestQueue : RequestQueue? = null
    private var requestQueueEliminar: RequestQueue? = null
    private var requestQueueAgregar: RequestQueue? = null

    private var listaMiGrupo = ArrayList<Alumno>()
    private var listaAlumnos = ArrayList<Alumno>()

    private var adapterAlumnos : CreaGrupoSinGrupoAdapter? = null
    private var adapterMiGrupo : CreaGrupoMiGrupoAdapter? = null

    private var creagruponuevo = false

    private val LOG = PRCrearGrupoActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prcrear_grupo)

        val toolbar = main_prcreargrupo as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.alumnos)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        rvAlumno_prcreargrupo.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvGrupo_prcreargrupo.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(
                this,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )

        rvAlumno_prcreargrupo.adapter = null
        rvGrupo_prcreargrupo.adapter = null

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        if (ControlUsuario.instance.currentUsuario.size == 1) {
            getCargarEstadoActual()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        getCargarEstadoActual()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
        }

    }

    private fun getCargarEstadoActual(){
        this.listaMiGrupo = ControlUsuario.instance.currentMiGrupo
        creagruponuevo = this.listaMiGrupo.isEmpty()

        adapterMiGrupo = CreaGrupoMiGrupoAdapter(listaMiGrupo)
        rvGrupo_prcreargrupo.adapter = adapterMiGrupo

        getAlumnosSinGrupo()
    }

    private fun getAlumnosSinGrupo() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val user = ControlUsuario.instance.currentUsuario[0]
            when (user) {
                is Alumno -> {
                    if (ControlUsuario.instance.prPromocionConfig != null) {
                        val request = JSONObject()
                        request.put("IdConfiguracion", ControlUsuario.instance.prPromocionConfig!!.idConfiguracion)
                        request.put("IdPromocion", ControlUsuario.instance.prPromocionConfig!!.idPomocion)
                        request.put("IdGrupoPromocion", ControlUsuario.instance.prPromocionConfig!!.idGrupo)

                        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                        if (requestEncriptado != null) {
                            onAlumnosSinGrupo(Utilitarios.getUrl(Utilitarios.URL.PR_ALUMNOS_SINGRUPO), requestEncriptado, user.codigo)
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
        } else {
            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_ingreso), Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
            snack.show()
        }
    }

    private fun onAlumnosSinGrupo(url: String, request: JSONObject, alumnoActual: String) {

        prbCargando_prcreargrupo.visibility = View.VISIBLE

        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
            { response ->
                prbCargando_prcreargrupo.visibility = View.GONE
                try {
                    if (!response.isNull("ListarAlumnosSinGrupoResult")) {
                        val alumnosJArray = Utilitarios.jsArrayDesencriptar(response["ListarAlumnosSinGrupoResult"] as String, this)
                        if (alumnosJArray != null) {
                            if (alumnosJArray.length() > 0) {
                                this.listaAlumnos = ArrayList()
                                for (x in 0 until alumnosJArray.length()) {
                                    val alumnoJson = alumnosJArray[x] as JSONObject
                                    val codigo = alumnoJson["Codigo"] as String
                                    val nombre = alumnoJson["NombreCompleto"] as String
                                    val idActor = alumnoJson["IdAlumno"] as Int

                                    val alumno = Alumno(codigo, idActor, nombre, "")
                                    this.listaAlumnos.add(alumno)
                                }

                                for (alumno in listaAlumnos) {
                                    if (alumno.codigo == alumnoActual) {
                                        listaAlumnos.remove(alumno)
                                        break
                                    }
                                }

                                adapterAlumnos = CreaGrupoSinGrupoAdapter(listaAlumnos) { alumno, position ->
                                    if (alumno.seleccionado) {
                                        /*ELIMINAR */
                                        setEliminarAlumnoGrupo(alumno, position)
                                    } else {
                                        /*AGREGAR */
                                        if ((ControlUsuario.instance.prPromocionConfig?.cantMaxGrupo ?: 0) - (listaMiGrupo.size  + (if (creagruponuevo) 1 else 0)) > 0) {
                                            setAgregaAlumnoGrupo(alumno, position)
                                        } else {
                                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.advertencia_maximo_alumnos_grupo), Snackbar.LENGTH_LONG)
                                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                                            snack.show()
                                        }
                                    }

                                }

                                rvAlumno_prcreargrupo.adapter = adapterAlumnos
                            } else {
                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.info_no_alumnos_sin_grupo), Snackbar.LENGTH_LONG)
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
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_respuesta_server), Snackbar.LENGTH_LONG)
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
            { error ->
                when {
                    error is TimeoutError -> {
                        prbCargando_prcreargrupo.visibility = View.GONE
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onAlumnosSinGrupo(url, request, alumnoActual)
                            } else {
                                prbCargando_prcreargrupo.visibility = View.GONE
                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        }
                    }
                    else -> {
                        prbCargando_prcreargrupo.visibility = View.GONE
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                }

            }
        )
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        {
            override fun getHeaders(): MutableMap<String, String> {
                return getHeaderForJWT()
            }
        }
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }

    private fun setEliminarAlumnoGrupo(alumnoSelect: Alumno, position: Int) {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val user = ControlUsuario.instance.currentUsuario[0]
            when (user) {
                is Alumno -> {
                    if (ControlUsuario.instance.prPromocionConfig != null) {
                        val request = JSONObject()
                        request.put("CodAlumnoGrupo", alumnoSelect.codigo)
                        request.put("CodAlumnoCrea", user.codigo)
                        request.put("IdGrupoReserva", ControlUsuario.instance.prPromocionConfig!!.idGrupoEstudio)

                        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                        if (requestEncriptado != null) {
                            onEliminarAlumnoGrupo(Utilitarios.getUrl(Utilitarios.URL.PR_ELIMINAR_ALUMNOGRUPO), requestEncriptado, alumnoSelect, position)
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
        } else {
            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_ingreso), Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
            snack.show()
        }
    }

    private fun onEliminarAlumnoGrupo(url: String, request: JSONObject, alumnoSelect: Alumno, position: Int) {
        CustomDialog.instance.showDialogLoad(this)
        requestQueueEliminar = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
            { response ->
                try {
                    if (!response.isNull("EliminarAlumnoGrupoResult")) {
                        val respuesta = Utilitarios.stringDesencriptar(response["EliminarAlumnoGrupoResult"] as String, this)

                        if ((respuesta ?: "0").toInt() == 1) {
                            adapterAlumnos?.actualizarEstado(position, false)
                            adapterMiGrupo?.removerAlumno(alumnoSelect)
                        } else if ((respuesta ?: "0").toInt() == 2) {
                            adapterAlumnos?.actualizarEstado(position, false)
                            adapterMiGrupo?.removerAlumno(alumnoSelect)
                            ControlUsuario.instance.creoMomificoGrupo = true
                            finish()
                        } else {
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.advertencia_no_eliminar_alumnos_grupo), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()
                        }
                    } else {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_respuesta_server), Snackbar.LENGTH_LONG)
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
                CustomDialog.instance.dialogoCargando?.dismiss()
            },
            { error ->
                when {
                    error is TimeoutError -> {
                        CustomDialog.instance.dialogoCargando?.dismiss()
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onEliminarAlumnoGrupo(url, request, alumnoSelect, position)
                            } else {
                                CustomDialog.instance.dialogoCargando?.dismiss()
                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        }
                    }
                    else -> {
                        CustomDialog.instance.dialogoCargando?.dismiss()
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                }

            }
        )
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        {
            override fun getHeaders(): MutableMap<String, String> {
                return getHeaderForJWT()
            }
        }
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueueEliminar?.add(jsObjectRequest)
    }

    private fun setAgregaAlumnoGrupo(alumnoSelect: Alumno, position: Int) {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val user = ControlUsuario.instance.currentUsuario[0]
            when (user) {
                is Alumno -> {
                    if (ControlUsuario.instance.prPromocionConfig != null) {
                        val request = JSONObject()
                        request.put("IdConfiguracion", ControlUsuario.instance.prPromocionConfig!!.idConfiguracion)
                        request.put("CodAlumno", alumnoSelect.codigo)
                        request.put("CodAlumnoCrea", user.codigo)
                        request.put("IdGrupoReserva", ControlUsuario.instance.prPromocionConfig?.idGrupoEstudio ?: 0)

                        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                        if (requestEncriptado != null) {
                            onAgregarAlumnoGrupo(Utilitarios.getUrl(Utilitarios.URL.PR_AGREGAR_ALUMNOGRUPO), requestEncriptado, alumnoSelect, position)
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
        } else {
            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_ingreso), Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
            snack.show()
        }
    }

    private fun onAgregarAlumnoGrupo(url: String, request: JSONObject, alumnoSelect: Alumno, position: Int) {
        CustomDialog.instance.showDialogLoad(this)

        requestQueueAgregar = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
            { response ->
                try {
                    if (!response.isNull("RegistraAlumnoGrupoResult")) {
                        val respuestaAgregarJs = Utilitarios.jsObjectDesencriptar(response["RegistraAlumnoGrupoResult"] as String, this)

                        if (respuestaAgregarJs != null) {
                            val idGrupoReserva = respuestaAgregarJs["IdGrupoReserva"] as? String ?: "0"
                            val mensajeval = respuestaAgregarJs["MensajeVal"] as String

                            if (idGrupoReserva.toInt() > 0) {
                                ControlUsuario.instance.prPromocionConfig?.idGrupoEstudio = idGrupoReserva.toInt()
                                adapterAlumnos?.actualizarEstado(position, true)
                                adapterMiGrupo?.agregarAlumno(alumnoSelect)
                            } else {
                                val snack = Snackbar.make(findViewById(android.R.id.content), mensajeval, Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                                snack.show()
                            }
                        } else {
                            val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.advertencia_no_agregar_alumnos_grupo), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()
                        }
                    } else {
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_respuesta_server), Snackbar.LENGTH_LONG)
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
                CustomDialog.instance.dialogoCargando?.dismiss()
            },
            { error ->
                when {
                    error is TimeoutError -> {
                        CustomDialog.instance.dialogoCargando?.dismiss()
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                    error.networkResponse.statusCode == 401 -> {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onAgregarAlumnoGrupo(url, request, alumnoSelect, position)
                            } else {
                                CustomDialog.instance.dialogoCargando?.dismiss()
                                val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                                snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                                snack.show()
                            }
                        }
                    }
                    else -> {
                        CustomDialog.instance.dialogoCargando?.dismiss()
                        val snack = Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.error_no_conexion), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.danger_text))
                        snack.show()
                    }
                }

            }
        )
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        {
            override fun getHeaders(): MutableMap<String, String> {
                return getHeaderForJWT()
            }
        }
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueueAgregar?.add(jsObjectRequest)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                ControlUsuario.instance.creoMomificoGrupo = true
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ControlUsuario.instance.creoMomificoGrupo = true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        requestQueueEliminar?.cancelAll(TAG)
        requestQueueAgregar?.cancelAll(TAG)
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
