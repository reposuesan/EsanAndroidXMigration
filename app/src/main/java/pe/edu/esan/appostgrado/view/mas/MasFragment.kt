package pe.edu.esan.appostgrado.view.mas


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
/*import com.crashlytics.android.Crashlytics*/
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.fragment_mas.*
import kotlinx.android.synthetic.main.fragment_mas.view.*
import org.json.JSONException
import org.json.JSONObject

import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.MasOpcionAdapter
import pe.edu.esan.appostgrado.adapter.UsuarioAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.*
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken
import pe.edu.esan.appostgrado.view.mas.cargacademica.CargaAcademicaActivity
import pe.edu.esan.appostgrado.view.mas.carne.CarneActivity
import pe.edu.esan.appostgrado.view.mas.comedor.ComedorActivity
import pe.edu.esan.appostgrado.view.mas.configuracion.ConfiguracionActivity
import pe.edu.esan.appostgrado.view.mas.curricula.MallaCurricularActivity
import pe.edu.esan.appostgrado.view.mas.laboratorios.PregradoLabsPrincipalActivity
import pe.edu.esan.appostgrado.view.mas.linkinteres.LinkInteresActivity
import pe.edu.esan.appostgrado.view.mas.mensaje.MensajeActivity
import pe.edu.esan.appostgrado.view.mas.ra.PrincipalRAActivity
import java.text.ParseException
import kotlin.Exception
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class MasFragment : androidx.fragment.app.Fragment() {

    private var listaUsuarios = ArrayList<UserEsan>()
    private var invitado = false
    private var requestQueue: RequestQueue? = null
    private var requestQueueCantMensaje: RequestQueue? = null
    private val TAG = "MasFragment"

    private var opcionesAdapter: MasOpcionAdapter? = null

    private val LOG = MasFragment::class.simpleName

    private var cerrandoSesion = false

    private lateinit var controlViewModel: ControlViewModel
    //TODO: QR INTERNATIONAL WEEK
    /*private var generateQRForIW: Boolean? = false
    private var readQRForIW: Boolean? = false*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        if(ControlUsuario.instance.currentUsuarioGeneral != null && ControlUsuario.instance.currentUsuario.size != 0){
            mainSetupMasFragment(view)
            setupAdapters()
            //TODO: QR INTERNATIONAL WEEK
            /*checkQRFeatureForIW(view)*/
        } else if(ControlUsuario.instance.currentUsuarioGeneral == null){
            if(ControlUsuario.instance.currentUsuario.size != 0){
                //Este caso es si el usuario es invitado
                if(ControlUsuario.instance.currentUsuario[0].nombreCompleto == "Invitado"){
                    mainSetupMasFragment(view)
                    setupAdapters()
                    //TODO: QR INTERNATIONAL WEEK
                    /*checkQRFeatureForIW(view)*/
                } else {
                    waitForViewModel(view)
                }
            } else {
                waitForViewModel(view)
            }
        } else if(ControlUsuario.instance.currentUsuario.size == 0){
            waitForViewModel(view)
        }

    }

    fun waitForViewModel(view: View){
        controlViewModel.dataWasRetrievedForFragmentPublic.observe(viewLifecycleOwner,
            Observer<Boolean> { value ->
                if(value){
                    mainSetupMasFragment(view)
                    setupAdapters()
                    //TODO: QR INTERNATIONAL WEEK
                    /*checkQRFeatureForIW(view)*/
                }
            })
        controlViewModel.refreshDataForFragment(true)
    }

    //TODO: QR INTERNATIONAL WEEK
    /*fun checkQRFeatureForIW(view: View){
        /*controlViewModel.showQRFeaturePublic.observe(this,
            Observer<Boolean> { value ->
                showQRForIW = value
                mainSetupMasFragment(view)
                setupAdapters()
        })*/
        val misPreferencias = requireActivity().getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
        generateQRForIW = misPreferencias?.getBoolean("qr_code_iw",false)

        mainSetupMasFragment(view)
        setupAdapters()
    }*/


    fun mainSetupMasFragment(view: View?){

        var usuarioGeneral: UsuarioGeneral? = null
        var currentUsuario = UserEsan(0, "Invitado")

        val misPreferencias = requireActivity().getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)

        try {
            usuarioGeneral = ControlUsuario.instance.currentUsuarioGeneral
        } catch (e: Exception) {
            val usuario = misPreferencias?.getString("code", "")
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log("E/MasFragment: currentUsuarioGeneral is null, the user is $usuario.")
        }

        try {
            currentUsuario = ControlUsuario.instance.currentUsuario[0]
        } catch (e: Exception) {
            val usuario = misPreferencias?.getString("code", "")
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log("E/MasFragment: currentUsuario is null, the user is $usuario.")
        }

        when (usuarioGeneral) {
            is UsuarioGeneral -> {

                if (usuarioGeneral.esAlumnoPostgrado) {
                    val student = Alumno(
                        usuarioGeneral.codigoAlumnoPost,
                        usuarioGeneral.nombreCompleto,
                        usuarioGeneral.nombre,
                        usuarioGeneral.apellido,
                        usuarioGeneral.tipoDocumento,
                        usuarioGeneral.numeroDocumento,
                        Utilitarios.POS, 0,
                        usuarioGeneral.usuario,
                        usuarioGeneral.clave, "Sin Carrera"
                    )
                    if (currentUsuario is Alumno) {
                        if (currentUsuario.equals(student)) {
                            student.seleccionado = true
                        }
                    }
                    listaUsuarios.add(student)
                }

                if (usuarioGeneral.esAlumnoPregrado) {
                    val student = Alumno(
                        usuarioGeneral.codigoAlumnoPre,
                        usuarioGeneral.nombreCompleto,
                        usuarioGeneral.nombre,
                        usuarioGeneral.apellido,
                        usuarioGeneral.tipoDocumento,
                        usuarioGeneral.numeroDocumento,
                        Utilitarios.PRE, 0,
                        usuarioGeneral.usuario,
                        usuarioGeneral.clave,
                        usuarioGeneral.programasPregrado[0].nombrePrograma
                    )

                    if (currentUsuario is Alumno) {
                        if (currentUsuario.equals(student)) {
                            student.seleccionado = true
                        }
                    }
                    listaUsuarios.add(student)
                }

                if (usuarioGeneral.esDocentePostgrado || usuarioGeneral.esDocentePregrado) {
                    val profesor = Profesor(
                        0,
                        if (ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePostgrado) {
                            ControlUsuario.instance.currentUsuarioGeneral!!.codigoDocentePost
                        } else {
                            ControlUsuario.instance.currentUsuarioGeneral!!.codigoDocentePre
                        },
                        ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                        ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                        ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                        ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                        ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                        ControlUsuario.instance.currentUsuarioGeneral!!.correo,
                        usuarioGeneral.usuario,
                        usuarioGeneral.clave,
                        ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePregrado,
                        ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePostgrado
                    )

                    if (currentUsuario is Profesor) {
                        if (currentUsuario.equals(profesor)) {
                            profesor.seleccionado = true
                        }
                    }
                    listaUsuarios.add(profesor)
                }
            }
        }

        if(view != null){
            if (listaUsuarios.size == 1) {
                view.rvUsuario_fmas.layoutManager =
                    androidx.recyclerview.widget.LinearLayoutManager(requireActivity())
            } else if (listaUsuarios.size > 1) {
                view.rvUsuario_fmas.layoutManager =
                    androidx.recyclerview.widget.LinearLayoutManager(
                        requireActivity(),
                        androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                        false
                    )
            } else {
                view.rvUsuario_fmas.visibility = View.GONE
            }

            view.rvOpciones_fmas.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(requireActivity())

            view.rvUsuario_fmas.adapter = null
            view.rvOpciones_fmas.adapter = null
        }
    }


    private fun setupAdapters(){

        rvUsuario_fmas.adapter = UsuarioAdapter(listaUsuarios) { userSelected, cantidadPerfiles, _ , tipo ->
            if (cantidadPerfiles == 1) {
                /**Un solo usuario*/
            } else if (cantidadPerfiles == 2) {

                val preferencias = requireActivity().getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
                val tipoAnterior = preferencias.getString("tipoperfil", "")

                if(tipoAnterior == tipo){
                    Log.w(LOG, "El usuario no ha cambiado de perfil")
                } else {
                    //Este valor es guardado para ser usado en LoginActivity en caso el usuario no haya cerrado sesión y tenga dos perfiles (Ejemplo: Alumno Pregrado y Alumno Posgrado)
                    val edit = preferencias.edit()
                    edit.putString("tipoperfil", tipo)
                    edit.apply()

                    ControlUsuario.instance.currentUsuario[0] = userSelected

                    val intent = requireActivity().intent
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    requireActivity().finish()
                    startActivity(intent)
                }
            }
        }

        opcionesAdapter =
            MasOpcionAdapter(getOpcionesUsuario(ControlUsuario.instance.currentUsuario[0]), invitado) { masOpcion ->
                if (masOpcion != null) {
                    if (masOpcion.intent != null) {
                        masOpcion.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        requireActivity().startActivity(masOpcion.intent)
                    }
                } else {
                    rvOpciones_fmas.visibility = View.INVISIBLE
                    rvOpciones_fmas.isEnabled = false
                    rvOpciones_fmas.isClickable = false
                    CustomDialog.instance.showDialogLoad(requireActivity())
                    if (invitado) {
                        ControlUsuario.instance.currentUsuario.clear()
                        ControlUsuario.instance.statusLogout = 1
                        dismissDialog()
                        requireActivity().finish()
                    } else {
                        if(!cerrandoSesion) {
                            setCerrarSesion()
                        } else {
                            Log.w(LOG, "Usuario ya presionó Salir o Cerrar Sesión, no repetir el request")
                        }
                    }
                }
            }


        rvOpciones_fmas.adapter = opcionesAdapter
    }

    private fun dismissDialog() {
        if (CustomDialog.instance.dialogoCargando != null) {
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private fun setCerrarSesion() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val usuario = ControlUsuario.instance.currentUsuario[0]
            when (usuario) {
                is UserEsan -> {
                    val request = JSONObject()
                    request.put("CodAlumno", usuario.codigo)

                    val requestEncriptado = Utilitarios.jsObjectEncrypted(request, requireActivity())
                    if (requestEncriptado != null) {
                        cerrandoSesion = true
                        onCerrarSesion(Utilitarios.getUrl(Utilitarios.URL.CERRAR_SESION), requestEncriptado)
                    } else {
                        Log.e(LOG, "Error en encriptado")
                    }
                }
            }
        } else {
            Log.e(LOG, "Error en cierre de sesión")
            rvOpciones_fmas.visibility = View.VISIBLE
            rvOpciones_fmas.isEnabled = true
            rvOpciones_fmas.isClickable = true
        }
    }

    private fun onCerrarSesion(url: String, request: JSONObject) {

        requestQueue = Volley.newRequestQueue(requireActivity())
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
            Request.Method.POST,
            url,
            request,
            { response ->

                val respuesta: String? = try {
                    Utilitarios.stringDesencriptar(response["ActualizarTokenResult"] as String, requireActivity())
                } catch (e: Exception) {
                    ""
                }

                controlViewModel.proceedLogoutPublic.observe(viewLifecycleOwner, Observer<Boolean> { value ->
                    if(value){
                        ControlUsuario.instance.statusLogout = 1

                        if (respuesta == "true") {
                            borrarPreferencias()
                            borrarDatos()
                            //UNSUBSCRIBE FROM TOPICS
                            /*unsubscribeUserFromTopics()*/
                            requireActivity().finish()
                        } else {
                            cerrandoSesion = false
                            Toast.makeText(requireActivity(), getString(R.string.error_en_cierre_sesion), Toast.LENGTH_LONG).show()
                            controlViewModel.proceedLogoutPublic.removeObservers(viewLifecycleOwner)
                        }
                    }
                })

                if (respuesta == "true") {
                    controlViewModel.deleteDataFromRoom()
                } else {
                    cerrandoSesion = false
                    Toast.makeText(requireActivity(), getString(R.string.error_en_cierre_sesion), Toast.LENGTH_LONG).show()
                    controlViewModel.proceedLogoutPublic.removeObservers(viewLifecycleOwner)
                }
            },
            { error ->
                when {
                    error is TimeoutError || error.networkResponse == null -> {
                        rvOpciones_fmas.visibility = View.VISIBLE
                        rvOpciones_fmas.isEnabled = true
                        rvOpciones_fmas.isClickable = true
                        dismissDialog()
                        cerrandoSesion = false
                        Toast.makeText(requireActivity(), getString(R.string.error_respuesta_server), Toast.LENGTH_LONG).show()
                    }
                    error.networkResponse.statusCode == 401 -> {
                        requireActivity().renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onCerrarSesion(url, request)
                            } else {
                                rvOpciones_fmas.visibility = View.VISIBLE
                                rvOpciones_fmas.isEnabled = true
                                rvOpciones_fmas.isClickable = true
                                dismissDialog()
                                cerrandoSesion = false
                                Toast.makeText(requireActivity(), getString(R.string.error_en_cierre_sesion), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    else -> {
                        rvOpciones_fmas.visibility = View.VISIBLE
                        rvOpciones_fmas.isEnabled = true
                        rvOpciones_fmas.isClickable = true
                        dismissDialog()
                        cerrandoSesion = false
                        Toast.makeText(requireActivity(), getString(R.string.error_en_cierre_sesion), Toast.LENGTH_LONG).show()
                    }
                }

            }
        )
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        {
            override fun getHeaders(): MutableMap<String, String> {
                return requireActivity().getHeaderForJWT()
            }
        }
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }

    private fun borrarDatos() {
        ControlUsuario.instance.currentUsuarioGeneral = null
        ControlUsuario.instance.currentUsuario.clear()
        ControlUsuario.instance.entroEncuesta = false

        ControlUsuario.instance.currentListHorarioSelect.clear()

        ControlUsuario.instance.currentListHorario.clear()

        ControlUsuario.instance.copiarListHorario.clear()
        ControlUsuario.instance.currentHorario = null
        ControlUsuario.instance.accesoCamara = false
        ControlUsuario.instance.accesoGPS = false
        ControlUsuario.instance.prconfiguracion = null
        ControlUsuario.instance.prPromocionConfig = null
        ControlUsuario.instance.prreserva = null
        ControlUsuario.instance.esReservaExitosa = false

        ControlUsuario.instance.creoMomificoGrupo = false
        ControlUsuario.instance.currentMiGrupo.clear()
        ControlUsuario.instance.cambioPantalla = false
        ControlUsuario.instance.pantallaSuspendida = false
        ControlUsuario.instance.recargaHorarioProfesor = false
        ControlUsuario.instance.tomoAsistenciaMasica = false
        ControlUsuario.instance.indexActualiza = -1

        ControlUsuario.instance.currentCursoPost = null
        ControlUsuario.instance.currentCursoPre = null
        ControlUsuario.instance.currentSeccion = null
    }

    private fun borrarPreferencias() {
        val misPresferencias = requireActivity().getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
        val agreetouchid = misPresferencias.getBoolean("touchid", false)
        val editor = misPresferencias.edit()
        if (agreetouchid) {
            editor.putString("code", "")
            editor.putString("password", "")
            editor.putString("tipoperfil", "")
            editor.putBoolean("cerrosesion", true)
        } else {
            editor.putString("code", "")
            editor.putString("password", "")
            editor.putString("tipoperfil", "")
            editor.putBoolean("cerrosesion", true)
        }
        //TODO: QR INTERNATIONAL WEEK
        /*editor.putBoolean("qr_code_iw", false)*/
        editor.apply()
    }

    private fun getOpcionesUsuario(usuario: UserEsan): List<MasOpcion> {

        val listaOpciones = ArrayList<MasOpcion>()
        when (usuario) {
            is Alumno -> {
                //TODO: QR INTERNATIONAL WEEK
                /*readQRForIW = false*/
                listaOpciones.add(
                    MasOpcion(
                        1,
                        Intent(requireActivity(), MensajeActivity::class.java),
                        requireContext().resources.getString(R.string.mensajes),
                        requireContext().resources.getString(R.string.mensajes_detalle),
                        requireContext().resources.getDrawable(R.drawable.icon_mail),
                        false,
                        0
                    )
                )
                if (usuario.tipoAlumno == Utilitarios.PRE) {
                    listaOpciones.add(
                        MasOpcion(
                            5,
                            Intent(requireActivity(), MallaCurricularActivity::class.java),
                            requireContext().resources.getString(R.string.malla_curricular),
                            requireContext().resources.getString(R.string.malla_curricular_detalle),
                            requireContext().resources.getDrawable(R.drawable.icon_malla),
                            false,
                            0
                        )
                    )
                    listaOpciones.add(
                        MasOpcion(
                            8,
                            Intent(requireActivity(), PregradoLabsPrincipalActivity::class.java),
                            requireContext().resources.getString(R.string.laboratorios_title),
                            requireContext().resources.getString(R.string.laboratorios_detalle),
                            ContextCompat.getDrawable(requireContext(), R.drawable.tab_laboratorio),
                            false,
                            0
                        )
                    )

                    if (listaUsuarios.size == 1) {
                        val alumnoPre = listaUsuarios[0] as UserEsan

                        val carneIntent = Intent(requireActivity(), CarneActivity::class.java)
                        carneIntent.putExtra("KEY_NOMBRE", alumnoPre.nombres)
                        carneIntent.putExtra("KEY_APELLIDO", alumnoPre.apellidos)
                        carneIntent.putExtra("KEY_CODIGO", alumnoPre.codigo)
                        carneIntent.putExtra("KEY_CARRERA", alumnoPre.carrera)
                        listaOpciones.add(
                            MasOpcion(
                                9,
                                carneIntent,
                                requireContext().getString(R.string.carne_virtual_title),
                                getString(R.string.generar_carne_virtual_subtitle),
                                requireContext().resources.getDrawable(R.drawable.tab_carnet),
                                false,
                                0
                            )
                        )
                    } else if (listaUsuarios.size > 1 && (listaUsuarios[0] as Alumno).tipoAlumno == Utilitarios.POS) {
                        val alumnoPre = listaUsuarios[1] as UserEsan

                        val carneIntent = Intent(requireActivity(), CarneActivity::class.java)
                        carneIntent.putExtra("KEY_NOMBRE", alumnoPre.nombres)
                        carneIntent.putExtra("KEY_APELLIDO", alumnoPre.apellidos)
                        carneIntent.putExtra("KEY_CODIGO", alumnoPre.codigo)
                        carneIntent.putExtra("KEY_CARRERA", alumnoPre.carrera)
                        listaOpciones.add(
                            MasOpcion(
                                9,
                                carneIntent,
                                requireContext().getString(R.string.carne_virtual_title),
                                getString(R.string.generar_carne_virtual_subtitle),
                                requireContext().resources.getDrawable(R.drawable.tab_carnet),
                                false,
                                0
                            )
                        )
                    } else {
                        val alumnoPre = listaUsuarios[0] as UserEsan

                        val carneIntent = Intent(requireActivity(), CarneActivity::class.java)
                        carneIntent.putExtra("KEY_NOMBRE", alumnoPre.nombres)
                        carneIntent.putExtra("KEY_APELLIDO", alumnoPre.apellidos)
                        carneIntent.putExtra("KEY_CODIGO", alumnoPre.codigo)
                        carneIntent.putExtra("KEY_CARRERA", alumnoPre.carrera)
                        listaOpciones.add(
                            MasOpcion(
                                9,
                                carneIntent,
                                requireContext().getString(R.string.carne_virtual_title),
                                getString(R.string.generar_carne_virtual_subtitle),
                                requireContext().resources.getDrawable(R.drawable.tab_carnet),
                                false,
                                0
                            )
                        )
                    }

                }

                if (usuario.tipoAlumno == Utilitarios.POS) {
                    val alumnoPost = listaUsuarios[0] as UserEsan

                    val carneIntent = Intent(requireActivity(), CarneActivity::class.java)
                    carneIntent.putExtra("KEY_NOMBRE", alumnoPost.nombres)
                    carneIntent.putExtra("KEY_APELLIDO", alumnoPost.apellidos)
                    carneIntent.putExtra("KEY_CODIGO", alumnoPost.codigo)
                    carneIntent.putExtra("KEY_CARRERA", alumnoPost.carrera)
                    listaOpciones.add(
                        MasOpcion(
                            9,
                            carneIntent,
                            requireContext().getString(R.string.carne_virtual_title),
                            getString(R.string.generar_carne_virtual_subtitle),
                            requireContext().resources.getDrawable(R.drawable.tab_carnet),
                            false,
                            0
                        )
                    )
                }

                listaOpciones.add(
                    MasOpcion(
                        6,
                        Intent(requireActivity(), ConfiguracionActivity::class.java),
                        requireContext().resources.getString(R.string.configuracion),
                        requireContext().resources.getString(R.string.configuracion_detalle),
                        requireContext().resources.getDrawable(R.drawable.ico_config),
                        false,
                        0
                    )
                )

                //TODO: QR INTERNATIONAL WEEK
                /*if(generateQRForIW!!){
                    listaOpciones.add(
                        MasOpcion(
                            15,
                            Intent(requireActivity(), GenerateQRCodeActivity::class.java),
                            getString(R.string.generacion_codigo_qr),
                            getString(R.string.genera_tu_codigo_qr),
                            context!!.resources.getDrawable(R.drawable.ic_code_qr),
                            false,
                            0
                        )
                    )
                }*/
            }
            is Profesor -> {
                //TODO: QR INTERNATIONAL WEEK
                /*readQRForIW = false*/
                listaOpciones.add(
                    MasOpcion(
                        1,
                        Intent(requireActivity(), MensajeActivity::class.java),
                        requireContext().resources.getString(R.string.mensajes),
                        requireContext().resources.getString(R.string.mensajes_detalle),
                        requireContext().resources.getDrawable(R.drawable.icon_mail),
                        false,
                        0
                    )
                )
                if (usuario.esDocentePost) {
                    listaOpciones.add(
                        MasOpcion(
                            4,
                            Intent(requireActivity(), CargaAcademicaActivity::class.java),
                            requireContext().resources.getString(R.string.carga_academica),
                            requireContext().resources.getString(R.string.carga_academica_detalle),
                            requireContext().resources.getDrawable(R.drawable.tab_clock),
                            false,
                            0
                        )
                    )
                }
                listaOpciones.add(
                    MasOpcion(
                        5,
                        Intent(requireActivity(), ConfiguracionActivity::class.java),
                        requireContext().resources.getString(R.string.configuracion),
                        requireContext().resources.getString(R.string.configuracion_detalle),
                        requireContext().resources.getDrawable(R.drawable.ico_config),
                        false,
                        0
                    )
                )
                //TODO: QR INTERNATIONAL WEEK
                /*if(generateQRForIW!!){
                    listaOpciones.add(
                        MasOpcion(
                            15,
                            Intent(requireActivity(), GenerateQRCodeActivity::class.java),
                            getString(R.string.generacion_codigo_qr),
                            getString(R.string.genera_tu_codigo_qr),
                            context!!.resources.getDrawable(R.drawable.ic_code_qr),
                            false,
                            0
                        )
                    )
                }*/
            }
            else -> {
                invitado = true
                //TODO: QR INTERNATIONAL WEEK
                /*readQRForIW = true*/
            }
        }

        //TODO: QR INTERNATIONAL WEEK
        /*if(readQRForIW!!){
            listaOpciones.add(
                MasOpcion(
                    16,
                    Intent(requireActivity(), ScanQRCodeActivity::class.java),
                    context!!.resources.getString(R.string.lectura_codigo_qr),
                    context!!.resources.getString(R.string.leer_codigo_qr),
                    context!!.resources.getDrawable(R.drawable.ic_code_qr),
                    false,
                    0
                )
            )
        }*/

        if (Utilitarios.comprobarSensor(requireActivity()))
            listaOpciones.add(
                MasOpcion(
                    2,
                    Intent(requireActivity(), PrincipalRAActivity::class.java).putExtra("accion", "busqueda"),
                    requireContext().resources.getString(R.string.ra),
                    requireContext().resources.getString(R.string.ra_detalle),
                    requireContext().resources.getDrawable(R.drawable.tab_ra),
                    false,
                    0
                )
            )
        else
            listaOpciones.add(
                MasOpcion(
                    2,
                    null,
                    requireContext().resources.getString(R.string.ra),
                    requireContext().resources.getString(R.string.ra_detalle),
                    requireContext().resources.getDrawable(R.drawable.tab_ra_unselect),
                    false,
                    0
                )
            )


        listaOpciones.add(
            MasOpcion(
                7,
                Intent(requireActivity(), ComedorActivity::class.java),
                requireContext().resources.getString(R.string.comedor),
                requireContext().resources.getString(R.string.comedor_detalle),
                requireContext().resources.getDrawable(R.drawable.tab_restaurant),
                false,
                0
            )
        )

        listaOpciones.add(
            MasOpcion(
                3,
                Intent(requireActivity(), LinkInteresActivity::class.java),
                requireContext().resources.getString(R.string.link_interes),
                requireContext().resources.getString(R.string.link_interes_detalle),
                requireContext().resources.getDrawable(R.drawable.tab_link),
                false,
                0
            )
        )

        return listaOpciones
    }

    override fun onStart() {
        super.onStart()
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val usuario = ControlUsuario.instance.currentUsuario[0]
            when (usuario) {
                is Alumno -> {
                    val request = JSONObject()
                    request.put("Facultad", if (usuario.tipoAlumno == Utilitarios.PRE) "2" else "1")
                    request.put("Usuario", usuario.codigo)

                    val requestEncriptado = Utilitarios.jsObjectEncrypted(request, requireActivity())
                    if (requestEncriptado != null) {

                        onCantidadMensaje(Utilitarios.getUrl(Utilitarios.URL.CANTIDAD_MENSAJES), requestEncriptado)
                    }
                }

                is Profesor -> {
                    val request = JSONObject()
                    request.put("Facultad", "0")
                    request.put("Usuario", usuario.codigo)

                    val requestEncriptado = Utilitarios.jsObjectEncrypted(request, requireActivity())
                    if (requestEncriptado != null) {

                        onCantidadMensaje(Utilitarios.getUrl(Utilitarios.URL.CANTIDAD_MENSAJES), requestEncriptado)
                    }
                }
            }
        }
    }


    private fun onCantidadMensaje(url: String, request: JSONObject) {
        if(isAdded) {
            requestQueueCantMensaje = Volley.newRequestQueue(requireActivity())
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            val jsObjectRequest = object: JsonObjectRequest(
                /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
                { response ->
                    try {
                        val respuesta = Utilitarios.stringDesencriptar(
                            response["ObtenerNotificacionPendientePorUsuarioResult"] as String,
                            requireActivity()
                        )
                        if (respuesta != null) {
                            opcionesAdapter?.actualizarCantidadMensajes(respuesta.toInt())
                        }
                    } catch (jex: JSONException) {

                    } catch (caax: ClassCastException) {

                    } catch (ex: ParseException) {

                    }

                },
                { error ->
                    when {
                        error is TimeoutError || error.networkResponse == null -> {
                            error.printStackTrace()
                        }
                        error.networkResponse.statusCode == 401 -> {
                            requireActivity().renewToken { token ->
                                if(!token.isNullOrEmpty()){
                                    onCantidadMensaje(url, request)
                                } else {
                                    error.printStackTrace()
                                }
                            }
                        }
                        else -> {
                            error.printStackTrace()
                        }
                    }
                }
            )
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            {
                override fun getHeaders(): MutableMap<String, String> {
                    return requireActivity().getHeaderForJWT()
                }
            }
            jsObjectRequest.retryPolicy = DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            jsObjectRequest.tag = TAG
            requestQueueCantMensaje?.add(jsObjectRequest)

        }
    }



    override fun onStop() {
        super.onStop()
        requestQueueCantMensaje?.cancelAll(TAG)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        dismissDialog()
        super.onDestroyView()
    }

    override fun onDestroy() {
        listaUsuarios.clear()
        dismissDialog()
        super.onDestroy()
    }


    //****************************************************************** TOPICS ************************************************************************************
    //UNSUBSCRIBE FROM TOPICS
    /*private fun unsubscribeUserFromTopics(){
        executeUnsubscriptionFromTopic("notific_alumnos_pregrado")
        executeUnsubscriptionFromTopic("notific_alumnos_postgrado")
        executeUnsubscriptionFromTopic("notific_alumnos")
        executeUnsubscriptionFromTopic("notific_docentes")
    }*/



    //UNSUBSCRIBE USER FROM TOPIC
    /*fun executeUnsubscriptionFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Unsubscribed from topic $topic"
                if (!task.isSuccessful) {
                    msg = "Error during unsubscription"
                }
            }
    }*/

}
