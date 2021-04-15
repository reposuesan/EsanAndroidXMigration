package pe.edu.esan.appostgrado.view.login

import android.Manifest
import android.annotation.TargetApi


import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.crashlytics.FirebaseCrashlytics
/*import com.crashlytics.android.Crashlytics*/
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.*
import pe.edu.esan.appostgrado.helpers.ShowAlertHelper
//UNCOMMENT THIS FOR THE INTERNATIONAL WEEK
/*import com.example.vuforiamod.international.activities.InternationalMainActivity*/
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.isOnlineUtils
import pe.edu.esan.appostgrado.view.MenuPrincipalActivity
import pe.edu.esan.appostgrado.view.login.dialog.FacultadUsuarioDialog
import pe.edu.esan.appostgrado.view.login.dialog.PerfilUsuarioDialog
import pe.edu.esan.appostgrado.view.login.fingerprint.FingerprintAuthenticationLoginDialogFragment
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import kotlin.collections.ArrayList

class LoginActivity : AppCompatActivity(),
    FingerprintAuthenticationLoginDialogFragment.FingerprintResultsLogin {

    private val TAG = "LoginActivity"
    private val DIALOG_FRAGMENT_TAG = "myFragment"

    private var fingerprintManager: FingerprintManager? = null
    private var keyguardManager: KeyguardManager? = null
    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private val KEY_NAME = "FINGER_KEY"
    private var cipher: Cipher? = null
    private var cryptoObject: FingerprintManager.CryptoObject? = null

    private val PERMISO_CAM_GPS = 0
    private val PERMISO_CAMARA = 1
    private val PERMISO_GPS = 2

    private var isAlumnoPre = false
    private var isAlumnoPost = false
    private var esAlumnoProfesor = false

    private var esPreyPost = false

    private var mSharedPreferences: SharedPreferences? = null

    private var requestQueue: RequestQueue? = null

    private var cerroSesion: Boolean = true

    private val LOG = LoginActivity::class.simpleName

    private var fingerFragmentDialog: FingerprintAuthenticationLoginDialogFragment? = null

    //UNCOMMENT THIS FOR CMUDE
    /*private val URL_CMUDE = "https://cmudeperu.esan.edu.pe/"*/
    private var iniciandoSesion = false
    private var dialogoPerfil: PerfilUsuarioDialog? = null
    private var dialogoFacultad: FacultadUsuarioDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        img_cmude.requestLayout()
        img_cmude.layoutParams.width = (width * 4) / 10

        imgInternationalWeek.requestLayout()
        imgInternationalWeek.layoutParams.width = (width * 4) / 10

        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName

            lblVersion_login.text = getString(R.string.version_app, version)
        } catch (e: PackageManager.NameNotFoundException) {
            lblVersion_login.text = ""
        }

        //CHANGE THIS TO VISIBLE FOR THE INTERNATIONAL WEEK
        imgInternationalWeek.visibility = View.GONE
        //CHANGE THIS TO VISIBLE FOR CMUDE
        img_cmude.visibility = View.GONE

        //Cmude Imagen
        //UNCOMMENT THIS FOR CMUDE
        /*img_cmude.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(URL_CMUDE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            //executeSubscription()
        }*/

        //Semana Internacional
        //UNCOMMENT THIS FOR THE INTERNATIONAL WEEK
        /*imgInternationalWeek.setOnClickListener {
            val intent = Intent(this@LoginActivity,  InternationalMainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            //executeUnsubscription()
        }*/


        //Login Button
        btnIngresar_login.setOnClickListener {
            if (!iniciandoSesion) {
                onIniciar()
            } else {
                Log.w(LOG, "Usuario ya presionó botón Login, no repetir el request")
            }
        }

        //Iniciar como Invitado
        btnSincuenta_login.setOnClickListener { onInvitado() }

        if (cerroSesion) {
            //Validate Permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                onValidaPermisosNecesarios()
            } else {
                ControlUsuario.instance.accesoCamara = true
                ControlUsuario.instance.accesoGPS = true
            }
        }


        //Set listener so it can work with enter button from the phone keyboard
        txtClave_login.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEND || i == EditorInfo.IME_ACTION_GO) {
                onIniciar()
            }
            false
        }

    }


    private fun onInvitado() {
        val invitado = UserEsan(0, "Invitado")
        ControlUsuario.instance.currentUsuario.add(invitado)
        ControlUsuario.instance.statusLogout = 0

        //INTENT MENU PRINCIPAL PARA INVITADO
        val intentMenuPrincipal =
            Intent().setClass(this@LoginActivity, MenuPrincipalActivity::class.java)
        intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intentMenuPrincipal.putExtra("login_invitado", "es_invitado")
        startActivity(intentMenuPrincipal)
    }


    override fun onStart() {
        super.onStart()
    }


    private fun callLogin() {
        if (isOnlineUtils(this)) {

            val misPreferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
            val usuario = misPreferencias.getString("code", "")
            val clave = misPreferencias.getString("password", "")
            val token = misPreferencias.getString("tokenID", "")

            if (!usuario.isNullOrEmpty() && !clave.isNullOrEmpty()) {
                val request = JSONObject()
                request.put("Usuario", usuario)
                request.put("Password", clave)
                request.put("Token", token)

                iniciandoSesion = true
                onLogin(Utilitarios.getUrl(Utilitarios.URL.LOGIN), request, usuario, clave)
            } else {
                Log.w(LOG, "Usuario y clave son null o están vacíos")
            }
        } else {

            val showAlertHelper = ShowAlertHelper(this)
            showAlertHelper.showAlertError(null, getString(R.string.error_no_internet), null)

        }
    }


    //Grab user and password and verify data input
    private fun onIniciar() {

        //ONLY FOR DEBUGGING (DEBUGGING PARA ALUMNO PREGRADO)
        /*val usuario = "19100263"*/
        //ONLY FOR DEBUGGING (DEBUGGING PARA ALUMNO POSGRADO)
        /*val usuario = "11100951"*/

        //USUARIO Y CLAVE
        val usuario = txtUsuario_login.text.toString()
        val clave = txtClave_login.text.toString()

        txtUsuario_login.error = null
        txtClave_login.error = null
        var focus: View? = null
        var isError = false

        if (TextUtils.isEmpty(clave)) {
            txtClave_login.error = resources.getString(R.string.error_clave_vacio)
            focus = txtClave_login
            isError = true
        }

        if (TextUtils.isEmpty(usuario)) {
            txtUsuario_login.error = resources.getString(R.string.error_usuario_vacio)
            focus = txtUsuario_login
            isError = true
        }

        //USUARIO EXPULSADO DE LA UNIVERSIDAD
        if (usuario == "17200563") {
            val snack = Snackbar.make(
                findViewById(android.R.id.content),
                "Usuario 17200563 no tiene permiso para realizar esta operación",
                Snackbar.LENGTH_SHORT
            )
            snack.view.setBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.warning
                )
            )
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                .setTextColor(ContextCompat.getColor(applicationContext, R.color.warning_text))
            snack.show()
            focus = txtUsuario_login
            isError = true
        }

        if (isError) {
            focus?.requestFocus()
        } else {
            if (isOnlineUtils(this)) {
                val misPreferencias =
                    getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
                val token = misPreferencias.getString("tokenID", "")

                val request = JSONObject()
                request.put("Usuario", usuario)
                request.put("Password", clave)
                request.put("Token", token)

                iniciandoSesion = true
                onLogin(Utilitarios.getUrl(Utilitarios.URL.LOGIN), request, usuario, clave)
            } else {
                val showAlertHelper = ShowAlertHelper(this)
                showAlertHelper.showAlertError(null, getString(R.string.error_no_internet), null)
            }
        }
    }


    private fun onLogin(url: String, request: JSONObject, usuario: String, clave: String) {

        CustomDialog.instance.showDialogLoad(this)

        val fRequest = Utilitarios.jsObjectEncrypted(request, this)

        if (fRequest != null) {
            requestQueue = Volley.newRequestQueue(this)
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            /*val jsObjectRequest = object: JsonObjectRequest(*/
            val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                fRequest,
                { response ->
                    try {
                        ControlUsuario.instance.currentUsuario = ArrayList()

                        if (!response.isNull("AutenticarUsuarioResult")) {
                            val jsResponse = Utilitarios.jsObjectDesencriptar(
                                response.getString("AutenticarUsuarioResult"),
                                this@LoginActivity
                            )

                            if (jsResponse != null) {

                                val jwtToken = jsResponse["TokenJWT"] as? String?

                                val jsObjDatosPersonales =
                                    jsResponse["ObjDatosPersonal"] as? JSONObject

                                val tipoDocumento =
                                    jsObjDatosPersonales?.getString("TipoDocumento") ?: ""
                                val documento = jsObjDatosPersonales?.getString("Documento") ?: ""
                                val nombre = jsObjDatosPersonales?.getString("Nombre") ?: ""
                                val apellido = jsObjDatosPersonales?.getString("Apellido") ?: ""
                                val nombreCompleto =
                                    jsObjDatosPersonales?.getString("NombreCompleto") ?: ""
                                val correo = jsObjDatosPersonales?.getString("Correo") ?: ""

                                val crashlytics = FirebaseCrashlytics.getInstance()

                                val jsObjDatosPostgrado =
                                    jsResponse["ObjDatosPostgrado"] as? JSONObject

                                //ALUMNO POSTGRADO--------------------------------------------------------------
                                val esAlumnoPost =
                                    jsObjDatosPostgrado?.getBoolean("EsAlumno") ?: false
                                //CÓDIGO ALUMNO POSTGRADO
                                val codAlumnoPost = jsObjDatosPostgrado?.getString("CodAlumno") ?: ""
                                //ONLY FOR DEBUGGING POSTGRADO
                                /*val codAlumnoPost = txtUsuario_login.text.toString()*/
                                /*val codAlumnoPost = "1302177"*/

                                crashlytics.setUserId(codAlumnoPost)

                                //DOCENTE POSTGRADO--------------------------------------------------------------
                                val esDocentePost =
                                    when (jsObjDatosPostgrado?.getInt("EsDocente") ?: 0) {
                                        0 -> false
                                        1 -> true
                                        2 -> false
                                        9 -> true
                                        else -> false
                                    }
                                //CÓDIGO DOCENTE POSTGRADO
                                val codDocentePost =
                                    jsObjDatosPostgrado?.getString("CodDocente") ?: ""

                                crashlytics.setUserId(codDocentePost)

                                val jsObjDatosPregrado =
                                    jsResponse["ObjDatosPregrado"] as? JSONObject

                                //ALUMNO PREGRADO--------------------------------------------------------------
                                val esAlumnoPre =
                                    jsObjDatosPregrado?.getBoolean("EsAlumno") ?: false
                                //CÓDIGO ALUMNO PREGRADO
                                val codAlumnoPre = jsObjDatosPregrado?.getString("CodAlumno") ?: ""
                                //ONLY FOR DEBUGGING PREGRADO
                                /*val codAlumnoPre = txtUsuario_login.text.toString()*/

                                crashlytics.setUserId(codAlumnoPre)

                                val objCarrerasPre =
                                    jsObjDatosPregrado?.getJSONArray("LstCarreraPregrado")

                                val programasPregrado = ArrayList<ProgramasPregrado>()

                                if (objCarrerasPre?.length() ?: 0 > 0) {
                                    for (i in 0 until (objCarrerasPre?.length() ?: 0)) {
                                        val objCarrera = objCarrerasPre?.getJSONObject(i)
                                        val codPrograma = objCarrera?.getString("CodPrograma") ?: ""
                                        val esCarreraActual =
                                            objCarrera?.getBoolean("EsCarreraActual") ?: false
                                        val nombrePrograma =
                                            objCarrera?.getString("NomPrograma") ?: ""

                                        val programaPre = ProgramasPregrado(
                                            codPrograma,
                                            nombrePrograma,
                                            "",
                                            esCarreraActual
                                        )
                                        programasPregrado.add(programaPre)
                                    }
                                }
                                //DOCENTE PREGRADO--------------------------------------------------------------
                                val esDocentePre =
                                    when (jsObjDatosPregrado?.getInt("EsDocente") ?: 0) {
                                        0 -> false
                                        1 -> false
                                        2 -> true
                                        9 -> true
                                        else -> false
                                    }

                                //CÓDIGO DOCENTE PREGRADO
                                val codDocentePre = jsObjDatosPregrado?.getString("CodDocente") ?: ""
                                /*val codDocentePre = "agazzolo"*/

                                crashlytics.setUserId(codDocentePre)

                                //USUARIO GENERAL OBJECT
                                val usuarioGeneral = UsuarioGeneral(
                                    tipoDocumento,
                                    documento,
                                    nombre,
                                    apellido,
                                    nombreCompleto,
                                    correo,
                                    usuario,
                                    clave,
                                    esAlumnoPre,
                                    codAlumnoPre,
                                    esAlumnoPost,
                                    codAlumnoPost,
                                    esDocentePre,
                                    codDocentePre,
                                    esDocentePost,
                                    codDocentePost,
                                    programasPregrado
                                )


                                //Usuario General guardado en Singleton
                                ControlUsuario.instance.currentUsuarioGeneral = usuarioGeneral

                                val misPreferencias = getSharedPreferences(
                                    "PreferenciasUsuario",
                                    Context.MODE_PRIVATE
                                )

                                val agreetouchid = misPreferencias.getBoolean("touchid", false)
                                val editor = misPreferencias.edit()

                                //JSON WEB TOKEN
                                if(!jwtToken.isNullOrEmpty()){
                                    editor.putString("jwt", jwtToken)
                                } else {
                                    editor.putString("jwt", "")
                                }

                                if (!agreetouchid) {
                                    //Si la opción de huella digital está apagada
                                    editor.putString("code", usuario)
                                    editor.putString("password", clave)
                                } else {
                                    //Si la opción de huella digital está encendida
                                    val codeSaved =
                                        misPreferencias.getString("userWithFingerprint", "")
                                    if (codeSaved == usuario) {
                                        editor.putString("code", usuario)
                                        editor.putString("password", clave)

                                    } else {
                                        editor.putString("code", usuario)
                                        editor.putString("password", clave)

                                    }
                                }

                                editor.apply()

                                val tipoPerfil = misPreferencias.getString("tipoperfil", "")

                                var seleccionPerfilAutomatico = false

                                if (!tipoPerfil.isNullOrEmpty()) {
                                    seleccionPerfilAutomatico = true
                                }

                                var esAlumno = false
                                var esDocente = false


                                if (esAlumnoPre || esAlumnoPost)
                                    esAlumno = true

                                if (esDocentePre || esDocentePost)
                                    esDocente = true

                                this.isAlumnoPre = esAlumnoPre
                                this.isAlumnoPost = esAlumnoPost

                                //ALUMNO Y DOCENTE, AMBOS
                                if (esAlumno && esDocente) {

                                    ControlUsuario.instance.statusLogout = 0
                                    this.esAlumnoProfesor = true
                                    //TODO: CONSULTAR SI UN ALUMNO PUEDE SER DE PREGRADO Y POSTGRADO Y ADEMÁS DOCENTE
                                    this.esPreyPost = false
                                    if (esAlumnoPre && esAlumnoPost) {
                                        this.esPreyPost = true
                                    }

                                    ControlUsuario.instance.currentUsuarioGeneral!!.validaPerfil =
                                        true
                                    ControlUsuario.instance.currentUsuarioGeneral!!.validarFacultad =
                                        this.esPreyPost
                                    ControlUsuario.instance.currentUsuarioGeneral!!.cambioPerfil =
                                        true

                                    //MUESTRA OPCIONES AL USUARIO

                                    if (!seleccionPerfilAutomatico) {
                                        //La opción de selección de perfil automático NO está habilitada
                                        val fragmentManager = supportFragmentManager

                                        if (dialogoPerfil == null) {

                                            dialogoPerfil = PerfilUsuarioDialog()

                                            dialogoPerfil?.show(fragmentManager, "perfil")
                                            dialogoPerfil?.isCancelable = false
                                            dialogoPerfil?.onClickOptionCustomListener { opcion ->
                                                if (dialogoPerfil != null) {
                                                    dialogoPerfil?.dismiss()
                                                }
                                                dialogoPerfil = null

                                                when (opcion) {
                                                    //ALUMNO
                                                    Utilitarios.ALU -> {
                                                        //ALUMNO PREGRADO Y POSTGRADO, AMBOS
                                                        if (esPreyPost) {

                                                            val fragmentManagerF =
                                                                supportFragmentManager

                                                            if (dialogoFacultad == null) {
                                                                dialogoFacultad =
                                                                    FacultadUsuarioDialog()
                                                                dialogoFacultad?.show(
                                                                    fragmentManagerF,
                                                                    "facultad"
                                                                )

                                                                dialogoFacultad?.isCancelable =
                                                                    false
                                                                dialogoFacultad?.onClickOptionCustomListener { op ->
                                                                    if (dialogoFacultad != null) {
                                                                        dialogoFacultad?.dismiss()
                                                                    }
                                                                    dialogoFacultad = null
                                                                    val student = Alumno(
                                                                        if (op == Utilitarios.PRE) {
                                                                            ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPre
                                                                        } else {
                                                                            ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPost
                                                                        },
                                                                        ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                                                        ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                                                        ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                                                        ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                                                        ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                                                        op,
                                                                        0,
                                                                        usuario,
                                                                        clave
                                                                    )

                                                                    val preferencias =
                                                                        getSharedPreferences(
                                                                            "PreferenciasUsuario",
                                                                            Context.MODE_PRIVATE
                                                                        )
                                                                    val edit = preferencias.edit()
                                                                    edit.putString("tipoperfil", op)
                                                                    edit.putBoolean(
                                                                        "cerrosesion",
                                                                        false
                                                                    )
                                                                    edit.apply()

                                                                    ControlUsuario.instance.currentUsuario.add(
                                                                        student
                                                                    )
                                                                    ControlUsuario.instance.statusLogout =
                                                                        0

                                                                    //INTENT MENU PRINCIPAL
                                                                    val intentMenuPrincipal =
                                                                        Intent().setClass(
                                                                            this@LoginActivity,
                                                                            MenuPrincipalActivity::class.java
                                                                        )
                                                                    intentMenuPrincipal.addFlags(
                                                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                                    )
                                                                    startActivity(
                                                                        intentMenuPrincipal
                                                                    )

                                                                }
                                                            } else {
                                                                Log.w(
                                                                    LOG,
                                                                    "dialogoFacultad ${dialogoFacultad.toString()} is not null, we are using a previous created dialogoFacultad"
                                                                )
                                                            }

                                                        } else {
                                                            //ALUMNO PREGRADO O ALUMNO POSTGRADO, PERO NO AMBOS
                                                            val student = Alumno(
                                                                if (esAlumnoPre) {
                                                                    ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPre
                                                                } else {
                                                                    ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPost
                                                                },
                                                                ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                                                ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                                                ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                                                ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                                                ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                                                if (esAlumnoPre) Utilitarios.PRE else Utilitarios.POS,
                                                                0,
                                                                usuario,
                                                                clave
                                                            )

                                                            val preferencias = getSharedPreferences(
                                                                "PreferenciasUsuario",
                                                                Context.MODE_PRIVATE
                                                            )
                                                            val edit = preferencias.edit()
                                                            edit.putString(
                                                                "tipoperfil",
                                                                if (esAlumnoPre) Utilitarios.PRE else Utilitarios.POS
                                                            )
                                                            edit.putBoolean("cerrosesion", false)
                                                            edit.apply()

                                                            ControlUsuario.instance.currentUsuario.add(
                                                                student
                                                            )
                                                            ControlUsuario.instance.statusLogout = 0

                                                            //INTENT MENU PRINCIPAL
                                                            val intentMenuPrincipal =
                                                                Intent().setClass(
                                                                    this@LoginActivity,
                                                                    MenuPrincipalActivity::class.java
                                                                )
                                                            intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                            startActivity(intentMenuPrincipal)

                                                        }
                                                    }
                                                    //DOCENTE
                                                    Utilitarios.DOC -> {
                                                        val codigoDocente =
                                                            if (ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePostgrado) ControlUsuario.instance.currentUsuarioGeneral!!.codigoDocentePost else ControlUsuario.instance.currentUsuarioGeneral!!.codigoDocentePre

                                                        val profesor = Profesor(
                                                            0, codigoDocente,
                                                            ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                                            ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                                            ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                                            ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                                            ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                                            ControlUsuario.instance.currentUsuarioGeneral!!.correo,
                                                            usuario, clave,
                                                            ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePregrado,
                                                            ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePostgrado
                                                        )

                                                        val preferencias = getSharedPreferences(
                                                            "PreferenciasUsuario",
                                                            Context.MODE_PRIVATE
                                                        )
                                                        val edit = preferencias.edit()
                                                        edit.putString(
                                                            "tipoperfil",
                                                            Utilitarios.DOC
                                                        )
                                                        edit.putBoolean("cerrosesion", false)
                                                        edit.apply()

                                                        ControlUsuario.instance.currentUsuario.add(
                                                            profesor
                                                        )
                                                        ControlUsuario.instance.statusLogout = 0

                                                        //INTENT MENU PRINCIPAL
                                                        val intentMenuPrincipal = Intent().setClass(
                                                            this@LoginActivity,
                                                            MenuPrincipalActivity::class.java
                                                        )
                                                        intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                        startActivity(intentMenuPrincipal)

                                                    }
                                                }
                                            }
                                        } else {
                                            Log.w(
                                                LOG,
                                                "dialogoPerfil ${dialogoPerfil.toString()} is not null, we are using a previous created dialogoPerfil"
                                            )
                                        }
                                    } else {
                                        //La opción de selección de perfil automático SI está habilitada
                                        val preferencias = getSharedPreferences(
                                            "PreferenciasUsuario",
                                            Context.MODE_PRIVATE
                                        )
                                        val edit = preferencias.edit()
                                        edit.putBoolean("cerrosesion", false)
                                        edit.apply()
                                        when (tipoPerfil) {
                                            //ALUMNO PREGRADO
                                            Utilitarios.PRE -> {
                                                val student = Alumno(
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPre,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                                    Utilitarios.PRE,
                                                    0,
                                                    usuario,
                                                    clave
                                                )

                                                ControlUsuario.instance.currentUsuario.add(student)
                                                ControlUsuario.instance.statusLogout = 0

                                                //INTENT MENU PRINCIPAL
                                                val intentMenuPrincipal = Intent().setClass(
                                                    this@LoginActivity,
                                                    MenuPrincipalActivity::class.java
                                                )
                                                intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                startActivity(intentMenuPrincipal)
                                            }
                                            //ALUMNO POSTGRADO
                                            Utilitarios.POS -> {
                                                val student = Alumno(
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPost,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                                    Utilitarios.POS,
                                                    0,
                                                    usuario,
                                                    clave
                                                )

                                                ControlUsuario.instance.currentUsuario.add(student)
                                                ControlUsuario.instance.statusLogout = 0

                                                //INTENT MENU PRINCIPAL
                                                val intentMenuPrincipal = Intent().setClass(
                                                    this@LoginActivity,
                                                    MenuPrincipalActivity::class.java
                                                )
                                                intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                startActivity(intentMenuPrincipal)
                                            }
                                            //DOCENTE
                                            Utilitarios.DOC -> {
                                                val codigoDocente =
                                                    if (ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePostgrado) ControlUsuario.instance.currentUsuarioGeneral!!.codigoDocentePost else ControlUsuario.instance.currentUsuarioGeneral!!.codigoDocentePre

                                                val profesor = Profesor(
                                                    0,
                                                    codigoDocente,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.correo,
                                                    usuario,
                                                    clave,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePregrado,
                                                    ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePostgrado
                                                )

                                                ControlUsuario.instance.currentUsuario.add(profesor)
                                                ControlUsuario.instance.statusLogout = 0

                                                //INTENT MENU PRINCIPAL
                                                val intentMenuPrincipal = Intent().setClass(
                                                    this@LoginActivity,
                                                    MenuPrincipalActivity::class.java
                                                )
                                                intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                startActivity(intentMenuPrincipal)
                                            }
                                        }
                                    }
                                    //SOLO ALUMNO, NO ES DOCENTE
                                } else if (esAlumno) {
                                    //ALUMNO PREGRADO Y POSTGRADO, AMBOS
                                    if (esAlumnoPre && esAlumnoPost) {

                                        ControlUsuario.instance.statusLogout = 0
                                        this.esPreyPost = true
                                        ControlUsuario.instance.currentUsuarioGeneral!!.validarFacultad =
                                            true
                                        ControlUsuario.instance.currentUsuarioGeneral!!.cambioPerfil =
                                            true

                                        //SHOW OPCIONES

                                        if (!seleccionPerfilAutomatico) {
                                            //La opción de selección de perfil automático NO está habilitada
                                            val fragmentManager = supportFragmentManager

                                            if (dialogoFacultad == null) {
                                                dialogoFacultad = FacultadUsuarioDialog()
                                                dialogoFacultad?.show(fragmentManager, "fd")
                                                dialogoFacultad?.isCancelable = false
                                                dialogoFacultad?.onClickOptionCustomListener { opcion ->
                                                    if (dialogoFacultad != null) {
                                                        dialogoFacultad?.dismiss()
                                                    }
                                                    dialogoFacultad = null
                                                    val student = Alumno(
                                                        if (opcion == Utilitarios.PRE) ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPre else ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPost,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                                        opcion, 0,
                                                        usuario, clave
                                                    )

                                                    val preferencias =
                                                        getSharedPreferences(
                                                            "PreferenciasUsuario",
                                                            Context.MODE_PRIVATE
                                                        )
                                                    val edit = preferencias.edit()
                                                    edit.putString("tipoperfil", opcion)
                                                    edit.putBoolean("cerrosesion", false)
                                                    edit.apply()

                                                    ControlUsuario.instance.currentUsuario.add(
                                                        student
                                                    )
                                                    ControlUsuario.instance.statusLogout = 0

                                                    //INTENT MENU PRINCIPAL
                                                    val intentMenuPrincipal = Intent().setClass(
                                                        this@LoginActivity,
                                                        MenuPrincipalActivity::class.java
                                                    )
                                                    intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    startActivity(intentMenuPrincipal)
                                                }

                                            } else {
                                                Log.w(
                                                    LOG,
                                                    "dialogoFacultad ${dialogoFacultad.toString()} is not null, we are using a previous created dialogoFacultad"
                                                )
                                            }
                                        } else {
                                            //La opción de selección de perfil automático SI está habilitada
                                            val preferencias = getSharedPreferences(
                                                "PreferenciasUsuario",
                                                Context.MODE_PRIVATE
                                            )
                                            val edit = preferencias.edit()
                                            edit.putBoolean("cerrosesion", false)
                                            edit.apply()
                                            when (tipoPerfil) {
                                                //ALUMNO PREGRADO
                                                Utilitarios.PRE -> {
                                                    val student = Alumno(
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPre,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                                        Utilitarios.PRE, 0,
                                                        usuario, clave
                                                    )

                                                    ControlUsuario.instance.currentUsuario.add(
                                                        student
                                                    )
                                                    ControlUsuario.instance.statusLogout = 0

                                                    //INTENT MENU PRINCIPAL
                                                    val intentMenuPrincipal = Intent().setClass(
                                                        this@LoginActivity,
                                                        MenuPrincipalActivity::class.java
                                                    )
                                                    intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    startActivity(intentMenuPrincipal)
                                                }
                                                //ALUMNO POSTGRADO
                                                Utilitarios.POS -> {
                                                    val student = Alumno(
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPost,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                                        ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                                        Utilitarios.POS, 0,
                                                        usuario, clave
                                                    )

                                                    ControlUsuario.instance.currentUsuario.add(
                                                        student
                                                    )
                                                    ControlUsuario.instance.statusLogout = 0

                                                    //INTENT MENU PRINCIPAL
                                                    val intentMenuPrincipal = Intent().setClass(
                                                        this@LoginActivity,
                                                        MenuPrincipalActivity::class.java
                                                    )
                                                    intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    startActivity(intentMenuPrincipal)
                                                }
                                            }
                                        }
                                    } else {
                                        //ALUMNO PREGRADO O ALUMNO POSTGRADO, PERO NO AMBOS
                                        val student = Alumno(
                                            if (esAlumnoPre) ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPre else ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPost,
                                            ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                            ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                            ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                            ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                            ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                            if (esAlumnoPre) Utilitarios.PRE else Utilitarios.POS,
                                            0,
                                            usuario,
                                            clave
                                        )

                                        val preferencias = getSharedPreferences(
                                            "PreferenciasUsuario",
                                            Context.MODE_PRIVATE
                                        )
                                        val edit = preferencias.edit()
                                        edit.putString(
                                            "tipoperfil",
                                            if (esAlumnoPre) Utilitarios.PRE else Utilitarios.POS
                                        )
                                        edit.putBoolean("cerrosesion", false)
                                        edit.apply()

                                        ControlUsuario.instance.currentUsuario.add(student)
                                        ControlUsuario.instance.statusLogout = 0

                                        //INTENT MENU PRINCIPAL
                                        val intentMenuPrincipal =
                                            Intent().setClass(
                                                this@LoginActivity,
                                                MenuPrincipalActivity::class.java
                                            )
                                        intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        startActivity(intentMenuPrincipal)

                                    }
                                } else if (esDocente) {
                                    //SOLO DOCENTE, NO ES ALUMNO
                                    val codigoDocente =
                                        if (ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePostgrado) ControlUsuario.instance.currentUsuarioGeneral!!.codigoDocentePost else ControlUsuario.instance.currentUsuarioGeneral!!.codigoDocentePre

                                    val profesor = Profesor(
                                        0,
                                        codigoDocente,
                                        ControlUsuario.instance.currentUsuarioGeneral!!.nombreCompleto,
                                        ControlUsuario.instance.currentUsuarioGeneral!!.nombre,
                                        ControlUsuario.instance.currentUsuarioGeneral!!.apellido,
                                        ControlUsuario.instance.currentUsuarioGeneral!!.tipoDocumento,
                                        ControlUsuario.instance.currentUsuarioGeneral!!.numeroDocumento,
                                        ControlUsuario.instance.currentUsuarioGeneral!!.correo,
                                        usuario,
                                        clave,
                                        ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePregrado,
                                        ControlUsuario.instance.currentUsuarioGeneral!!.esDocentePostgrado
                                    )

                                    val preferencias = getSharedPreferences(
                                        "PreferenciasUsuario",
                                        Context.MODE_PRIVATE
                                    )
                                    val edit = preferencias.edit()
                                    edit.putString("tipoperfil", Utilitarios.DOC)
                                    edit.putBoolean("cerrosesion", false)
                                    edit.apply()

                                    ControlUsuario.instance.currentUsuario.add(profesor)
                                    ControlUsuario.instance.statusLogout = 0

                                    //INTENT MENU PRINCIPAL
                                    val intentMenuPrincipal =
                                        Intent().setClass(
                                            this@LoginActivity,
                                            MenuPrincipalActivity::class.java
                                        )
                                    intentMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intentMenuPrincipal)

                                }
                            } else {
                                val showAlertHelper = ShowAlertHelper(this)
                                showAlertHelper.showAlertError(
                                    getString(R.string.error),
                                    getString(R.string.error_encriptar),
                                    null
                                )
                                iniciandoSesion = false
                            }

                        } else {
                            val showAlertHelper = ShowAlertHelper(this)
                            showAlertHelper.showAlertError(
                                getString(R.string.error),
                                getString(R.string.error_login_uno),
                                null
                            )
                            iniciandoSesion = false
                        }
                    } catch (jex: JSONException) {
                        val showAlertHelper = ShowAlertHelper(this)
                        showAlertHelper.showAlertError(
                            getString(R.string.error),
                            getString(R.string.error_login_tres),
                            null
                        )
                        iniciandoSesion = false
                    }
                    dismissDialog()
                },
                { error ->
                    if(error.networkResponse.statusCode == 500){
                        val showAlertHelper = ShowAlertHelper(this)
                        showAlertHelper.showAlertError(
                            getString(R.string.error),
                            getString(R.string.error_login_uno),
                            null
                        )
                    } else {
                        val showAlertHelper = ShowAlertHelper(this)
                        showAlertHelper.showAlertError(getString(R.string.error),
                            getString(R.string.error_no_conexion),
                            null
                        )
                    }
                    dismissDialog()
                    iniciandoSesion = false
                }
            )
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            /*{
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer <<Token ID>>"
                    return headers
                }
            }*/

            jsObjectRequest.retryPolicy = DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            jsObjectRequest.tag = TAG

            requestQueue?.add(jsObjectRequest)
        }
    }

    //VALIDACIÓN DE PERMISOS
    private fun onValidaPermisosNecesarios() {
        val iCamara = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val iGPS = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if (iCamara != PackageManager.PERMISSION_GRANTED || iGPS != PackageManager.PERMISSION_GRANTED) {
            val pCamara =
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this@LoginActivity,
                    Manifest.permission.CAMERA
                )
            val pGps = ActivityCompat.shouldShowRequestPermissionRationale(
                this@LoginActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (pCamara || pGps) {
                if (pCamara && pGps) {
                    ActivityCompat.requestPermissions(
                        this@LoginActivity,
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        PERMISO_CAM_GPS
                    )
                } else {
                    if (pCamara) {
                        ActivityCompat.requestPermissions(
                            this@LoginActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            PERMISO_CAMARA
                        )
                    }
                    if (pGps) {
                        ActivityCompat.requestPermissions(
                            this@LoginActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISO_GPS
                        )
                    }
                }
            } else {
                ActivityCompat.requestPermissions(
                    this@LoginActivity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISO_CAM_GPS
                )
            }
        } else {
            ControlUsuario.instance.accesoCamara = true
            ControlUsuario.instance.accesoGPS = true
        }
    }

    //RESULTADO DE VALIDACIÓN DE PERMISOS
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISO_CAM_GPS -> {
                if (grantResults.isNotEmpty()) {
                    ControlUsuario.instance.accesoCamara =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                    ControlUsuario.instance.accesoGPS =
                        grantResults[1] == PackageManager.PERMISSION_GRANTED
                } else {
                    ControlUsuario.instance.accesoCamara = false
                    ControlUsuario.instance.accesoGPS = false
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.no_camera_and_no_gps_usage_message),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            PERMISO_CAMARA -> {
                if (grantResults.isNotEmpty()) {
                    ControlUsuario.instance.accesoCamara =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                }
            }
            PERMISO_GPS -> {
                if (grantResults.isNotEmpty()) {
                    ControlUsuario.instance.accesoGPS =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExito() {
        if (fingerFragmentDialog != null) {
            fingerFragmentDialog?.dismiss()
            fingerFragmentDialog = null

            val misPreferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)

            val editor = misPreferencias.edit()
            editor.putString("code", misPreferencias.getString("userWithFingerprint", ""))
            editor.putString("password", misPreferencias.getString("passwordWithFingerprint", ""))

            editor.apply()

            callLogin()
        }
    }

    override fun onCancel() {
        if (fingerFragmentDialog != null) {
            fingerFragmentDialog?.dismiss()
            fingerFragmentDialog = null
        }
    }

    override fun onErrorFingerprint(errorId: Int) {

        if (fingerFragmentDialog != null) {
            if (errorId != FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                dialog.setCancelable(false)
                dialog.setTitle(resources.getString(R.string.error))
                    .setMessage(resources.getString(R.string.error_login_huella))
                dialog.setPositiveButton(resources.getString(R.string.aceptar), null)
                dialog.create().show()
            }
        }
    }


    /**LECTOR DE HUELLA*/
    @TargetApi(Build.VERSION_CODES.M)
    private fun getManagers(): Boolean {
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager

        if (fingerprintManager == null) {
            return false
        }

        if (fingerprintManager?.isHardwareDetected == false) {
            return false
        }

        if (keyguardManager?.isKeyguardSecure == false) {
            return false
        }

        if (fingerprintManager?.hasEnrolledFingerprints() == false) {
            return false
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.USE_FINGERPRINT
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            return false
        }
        return true
    }


    @TargetApi(Build.VERSION_CODES.M)
    private fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(
                "Failed to get KeyGenerator instance", e
            )
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        }

        try {
            keyStore?.load(null)
            keyGenerator?.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )
            keyGenerator?.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private fun cipherInit(): Boolean {
        try {
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore?.load(null)
            val key = keyStore?.getKey(KEY_NAME, null) as SecretKey
            cipher?.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }


    override fun onStop() {
        requestQueue?.cancelAll(TAG)
        super.onStop()
    }

    override fun onPause() {
        if (fingerFragmentDialog != null) {
            fingerFragmentDialog?.dismiss()
            fingerFragmentDialog = null
        }

        if (dialogoPerfil != null) {
            dialogoPerfil?.dismiss()
            dialogoPerfil = null
        }

        if (dialogoFacultad != null) {
            dialogoFacultad?.dismiss()
            dialogoFacultad = null
        }

        super.onPause()
    }

    override fun onDestroy() {
        dismissDialog()
        super.onDestroy()
    }


    //VERIFICAR LA EXISTENCIA DE PREFERENCIAS PARA LA HUELLA DIGITAL
    private fun checkRemovedPreferences(): Boolean {

        val misPreferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)

        val code = misPreferencias.getString("userWithFingerprint", "")
        val password = misPreferencias.getString("passwordWithFingerprint", "")

        return (code.isNullOrEmpty() && password.isNullOrEmpty())
    }


    override fun onResume() {
        super.onResume()
        iniciandoSesion = false
        val misPreferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)

        if (checkRemovedPreferences()) {
            val edit = misPreferencias.edit()
            edit.putBoolean("touchid", false)
            edit.apply()
        }

        val agreetouchid = misPreferencias.getBoolean("touchid", false)

        cerroSesion = misPreferencias.getBoolean("cerrosesion", true)

        if (cerroSesion) {
            enableLoginOption(true)
            enableGuestOption(true)
            if (agreetouchid) {
                if (fingerFragmentDialog == null) {
                    if (getManagers()) {
                        generateKey()
                        if (cipherInit()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                cipher?.let {
                                    cryptoObject = FingerprintManager.CryptoObject(it)
                                }

                                mSharedPreferences =
                                    PreferenceManager.getDefaultSharedPreferences(this)

                                fingerFragmentDialog =
                                    FingerprintAuthenticationLoginDialogFragment()


                                fingerFragmentDialog?.let { it.setCryptoObject(cryptoObject) }

                                fingerFragmentDialog?.let {
                                    it.setFingerprintResultsLoginInterface(
                                        this
                                    )
                                }

                                val useFingerprintPreference =
                                    mSharedPreferences?.getBoolean(
                                        "use_fingerprint_to_authenticate_key",
                                        true
                                    )

                                if (useFingerprintPreference!!) {

                                    fingerFragmentDialog?.let {
                                        it.setStage(
                                            FingerprintAuthenticationLoginDialogFragment.Stage.FINGERPRINT
                                        )
                                    }
                                } else {

                                    fingerFragmentDialog?.let {
                                        it.setStage(
                                            FingerprintAuthenticationLoginDialogFragment.Stage.PASSWORD
                                        )
                                    }
                                }

                                fingerFragmentDialog?.let {
                                    it.show(
                                        fragmentManager,
                                        DIALOG_FRAGMENT_TAG
                                    )
                                }

                            }
                        }
                    }
                }

            }
        } else {
            enableGuestOption(false)
            enableLoginOption(false)
            if (!iniciandoSesion) {
                callLogin()
            } else {
                Log.w(LOG, "Usuario ya presionó botón Login, no repetir el request")
            }
        }

    }

    private fun dismissDialog(){
        if (CustomDialog.instance.dialogoCargando != null) {
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
    }

    private fun enableGuestOption(enable: Boolean) {
        btnSincuenta_login.isEnabled = enable
    }

    private fun enableLoginOption(enable: Boolean){
        btnIngresar_login.isEnabled = enable
    }


}
