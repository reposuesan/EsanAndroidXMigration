package pe.edu.esan.appostgrado.view.puntosreunion.pregrado

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.appcompat.app.AlertDialog
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_cursos.view.*
import kotlinx.android.synthetic.main.fragment_puntos_reunion_pregrado.*
import kotlinx.android.synthetic.main.fragment_puntos_reunion_pregrado.view.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno

import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken
import java.util.*


class PuntosReunionPregradoFragment : androidx.fragment.app.Fragment() {

    private var mRequestQueue: RequestQueue? = null

    private val LOG = PuntosReunionPregradoFragment::class.simpleName

    private val TAG = "PuntosReunionPregradoFragment"

    private val URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_PP_OBT_CONFIGURACION)
    private var URL_TEST_VERIFICAR_TYC_PR = Utilitarios.getUrl(Utilitarios.URL.PREG_PP_VERIFICAR_TIPO_POLITICA)
    private var URL_TEST_ACEPTAR_TYC_PR = Utilitarios.getUrl(Utilitarios.URL.PREG_PP_ACEPTAR_TIPO_POLITICA)

    private var configuracionID: String = ""
    private var promocion: String = ""
    private var direccion: String = ""

    private var codigoAlumno: String = ""

    private var terminosCondicionesChecked = false

    private var idTipoPolitica = ""

    private var urlForPolicy = ""

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_puntos_reunion_pregrado, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        view.linear_layout_container.visibility = View.GONE
        view.empty_text_view.visibility = View.GONE
        view.progress_bar_pp_pregrado.visibility = View.VISIBLE

        view.realizar_prereserva_opcion.setOnClickListener {
            if(terminosCondicionesChecked){
                val intent = Intent(requireActivity(), PregradoCubsRegistroActivity::class.java)
                intent.putExtra("configuracionID", configuracionID)
                intent.putExtra("codigo_alumno", codigoAlumno)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                if(activity != null) {
                    val snack = Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        getString(R.string.debe_aceptar_politicas_uso),
                        Snackbar.LENGTH_SHORT
                    )
                    snack.view.setBackgroundColor(
                        ContextCompat.getColor(
                            view.context,
                            R.color.warning
                        )
                    )
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        .setTextColor(ContextCompat.getColor(view.context, R.color.warning_text))
                    snack.show()
                }
            }
        }

        view.confirmar_prereserva_opcion.setOnClickListener {
            if(terminosCondicionesChecked) {
                val intent = Intent(requireActivity(), PregradoCubsConfirmacionActivity::class.java)
                intent.putExtra("configuracionID", configuracionID)
                intent.putExtra("codigo_alumno", codigoAlumno)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                if(activity != null) {
                    val snack = Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        getString(R.string.debe_aceptar_politicas_uso),
                        Snackbar.LENGTH_SHORT
                    )
                    snack.view.setBackgroundColor(
                        ContextCompat.getColor(
                            view.context,
                            R.color.warning
                        )
                    )
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        .setTextColor(ContextCompat.getColor(view.context, R.color.warning_text))
                    snack.show()
                }
            }
        }

        view.ver_historial_prereservas_opcion.setOnClickListener {
            if(terminosCondicionesChecked) {
                val intent = Intent(requireActivity(), PregradoCubsHistorialActivity::class.java)
                intent.putExtra("configuracionID", configuracionID)
                intent.putExtra("codigo_alumno", codigoAlumno)
                intent.putExtra("promocion", promocion)
                intent.putExtra("direccion", direccion)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                if(activity != null) {
                    val snack = Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        getString(R.string.debe_aceptar_politicas_uso),
                        Snackbar.LENGTH_SHORT
                    )
                    snack.view.setBackgroundColor(
                        ContextCompat.getColor(
                            view.context,
                            R.color.warning
                        )
                    )
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        .setTextColor(ContextCompat.getColor(view.context, R.color.warning_text))
                    snack.show()
                }
            }
        }

        view.checkbox_terminos_condiciones_pr.visibility = View.GONE

        view.checkbox_terminos_condiciones_pr.setOnClickListener {
            val checked: Boolean = checkbox_terminos_condiciones_pr.isChecked
            if(checked && !terminosCondicionesChecked){
                showTYCConfirmationDialog()
            }
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(ControlUsuario.instance.currentUsuarioGeneral != null){
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

        codigoAlumno = if(ControlUsuario.instance.currentUsuarioGeneral != null) {
            ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPre
        } else {
            ""
        }

        val request = JSONObject()
        request.put("CodAlumno", codigoAlumno)

        obtenerConfiguracionIdServicio(URL_TEST, request)
    }


    fun setCheckboxTextClickable(url: String){
        val checkBoxText = getString(R.string.acepto_politicas_uso_checkbox_text_href, url)

        requireView().tv_for_checkbox_tyc_cubs.text = Html.fromHtml(checkBoxText)
        requireView().tv_for_checkbox_tyc_cubs.movementMethod = LinkMovementMethod.getInstance()

    }


    fun showTYCConfirmationDialog(){

        if(activity != null) {
            val alertDialog = AlertDialog.Builder(requireActivity(), R.style.EsanAlertDialog)

            alertDialog.setTitle(getString(R.string.acepta_tyc_dialog_pr_title))
            alertDialog.setMessage(getString(R.string.mensaje_politicas_uso_dialog))

            alertDialog.setNegativeButton(
                getString(R.string.cancelar_dialog_text),
                DialogInterface.OnClickListener { dialog, which ->
                    terminosCondicionesChecked = false
                    checkbox_terminos_condiciones_pr.isChecked = false
                    dialog.dismiss()
                })

            alertDialog.setPositiveButton(
                getString(R.string.aceptar_dialog_text),
                DialogInterface.OnClickListener { dialog, which ->

                    val requestTYC = JSONObject()

                    val tipoAlumno =
                        (ControlUsuario.instance.currentUsuario[0] as Alumno).tipoAlumno

                    if (tipoAlumno == Utilitarios.PRE) {
                        requestTYC.put("CodUsuario", codigoAlumno)
                        requestTYC.put("IdFacultad", 2)
                        requestTYC.put("Rol", "A")
                        requestTYC.put("IdTipoPolitica", idTipoPolitica.toInt())

                        var dispositivo: String? = null

                        try {
                            if (activity != null) {
                                val pInfo = requireActivity().packageManager.getPackageInfo(
                                    requireActivity().packageName,
                                    0
                                )
                                val version = pInfo.versionName

                                dispositivo = "Android-$version"
                            }
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                        }

                        requestTYC.put("Dispositivo", dispositivo)

                        aceptarTipoPolitica(URL_TEST_ACEPTAR_TYC_PR, requestTYC)
                    } else {
                        Log.e(LOG, "Error: Tipo de usuario incorrecto")
                    }
                })

            alertDialog.setOnCancelListener {
                terminosCondicionesChecked = false
                checkbox_terminos_condiciones_pr.isChecked = false
            }

            alertDialog.show()
        }
    }


    fun onCheckboxClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked
            when (view.id) {
                R.id.checkbox_terminos_condiciones_pr -> {
                    if(checked && !terminosCondicionesChecked){
                        showTYCConfirmationDialog()
                    }
                }
            }
        }
    }


    fun obtenerConfiguracionIdServicio(url: String, request: JSONObject) {

        if(activity != null) {
            val fRequest = Utilitarios.jsObjectEncrypted(request, requireActivity().applicationContext)

            if (fRequest != null) {
                mRequestQueue = Volley.newRequestQueue(requireActivity().applicationContext)
                //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
                val jsonObjectRequest = object: JsonObjectRequest(
                /*val jsonObjectRequest = JsonObjectRequest(*/
                    Request.Method.POST,
                    url,
                    fRequest,
                    { response ->
                        if (!response.isNull("ObtenerConfiguracionAlumnoResult")) {
                            var jsResponse: JSONObject? = null
                            if (activity != null) {
                                jsResponse = Utilitarios.jsObjectDesencriptar(
                                    response.getString("ObtenerConfiguracionAlumnoResult"),
                                    requireActivity().applicationContext
                                )
                            }
                            if (jsResponse != null) {

                                try {
                                    configuracionID = jsResponse.optString("IdConfiguracion")
                                    promocion = jsResponse.optString("Promocion")
                                    direccion = jsResponse.optString("TipoMatricula")

                                    idTipoPolitica = jsResponse.optString("IdTipoPolitica")
                                    val indicadorError = jsResponse.optString("IndicadorError")
                                    val mensaje = jsResponse.optString("Mensaje")

                                    if (indicadorError.toInt() == 0) {
                                        if (view != null) {
                                            requireView().linear_layout_container.visibility = View.VISIBLE
                                            requireView().empty_text_view.visibility = View.GONE
                                            requireView().progress_bar_pp_pregrado.visibility = View.GONE
                                        }

                                        val requestTYC = JSONObject()

                                        val tipoAlumno =
                                            (ControlUsuario.instance.currentUsuario[0] as Alumno).tipoAlumno

                                        if (tipoAlumno == Utilitarios.PRE) {
                                            requestTYC.put("CodUsuario", codigoAlumno)
                                            requestTYC.put("IdFacultad", 2)
                                            requestTYC.put("Rol", "A")
                                            requestTYC.put("IdTipoPolitica", idTipoPolitica.toInt())

                                            verificarTipoPoliticaPortal(
                                                URL_TEST_VERIFICAR_TYC_PR,
                                                requestTYC
                                            )
                                        } else {
                                            Log.e(LOG, "Error: Tipo de usuario incorrecto")
                                        }
                                    } else {
                                        if (view != null) {
                                            requireView().linear_layout_container.visibility = View.GONE
                                            requireView().empty_text_view.visibility = View.VISIBLE
                                            requireView().empty_text_view.text = mensaje
                                            requireView().progress_bar_pp_pregrado.visibility = View.GONE

                                            val language = Locale.getDefault().displayLanguage

                                            if (language.equals("English")) {
                                                if (mensaje.contains("no se encuentra matr")) {
                                                    requireView().empty_text_view.text =
                                                        "You are not enrolled in the current semester or these services will be available at the beginning of the semester."
                                                } else {
                                                    requireView().empty_text_view.text =
                                                        "An error occurred, please contact ServiceDesk."
                                                }
                                            }
                                        }
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()

                                    if (view != null) {
                                        requireView().linear_layout_container.visibility = View.GONE
                                        requireView().empty_text_view.text =
                                            getString(R.string.usted_no_tiene_permiso_mensaje)
                                        requireView().empty_text_view.visibility = View.VISIBLE
                                        requireView().progress_bar_pp_pregrado.visibility = View.GONE
                                    }
                                }
                            } else {
                                if (view != null) {
                                    requireView().linear_layout_container.visibility = View.GONE
                                    requireView().empty_text_view.text =
                                        getString(R.string.no_respuesta_desde_servidor)
                                    requireView().empty_text_view.visibility = View.VISIBLE
                                    requireView().progress_bar_pp_pregrado.visibility = View.GONE
                                }
                            }
                        }
                    },
                    { error ->
                        if(error is TimeoutError || error.networkResponse == null) {
                            if(view != null) {
                                requireView().linear_layout_container.visibility = View.GONE
                                requireView().empty_text_view.text = getString(R.string.no_respuesta_desde_servidor)
                                requireView().empty_text_view.visibility = View.VISIBLE
                                requireView().progress_bar_pp_pregrado.visibility = View.GONE
                            }
                        } else if(error.networkResponse.statusCode == 401) {

                            requireActivity().renewToken { token ->
                                if(!token.isNullOrEmpty()){
                                    obtenerConfiguracionIdServicio(url, request)
                                } else {
                                    if(view != null) {
                                        requireView().linear_layout_container.visibility = View.GONE
                                        requireView().empty_text_view.text = getString(R.string.no_respuesta_desde_servidor)
                                        requireView().empty_text_view.visibility = View.VISIBLE
                                        requireView().progress_bar_pp_pregrado.visibility = View.GONE
                                    }
                                }
                            }
                        } else {
                            if(view != null) {
                                requireView().linear_layout_container.visibility = View.GONE
                                requireView().empty_text_view.text = getString(R.string.no_respuesta_desde_servidor)
                                requireView().empty_text_view.visibility = View.VISIBLE
                                requireView().progress_bar_pp_pregrado.visibility = View.GONE
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

                jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
                jsonObjectRequest.tag = TAG

                mRequestQueue?.add(jsonObjectRequest)

            }
        }
    }


    fun verificarTipoPoliticaPortal(url: String, request: JSONObject) {

        if(activity != null) {
            val fRequest = Utilitarios.jsObjectEncrypted(request, requireActivity().applicationContext)
            if (fRequest != null) {
                mRequestQueue = Volley.newRequestQueue(requireActivity().applicationContext)
                //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
                val jsonObjectRequest = object: JsonObjectRequest(
                /*val jsonObjectRequest = JsonObjectRequest(*/
                    Request.Method.POST,
                    url,
                    fRequest,
                    { response ->
                        if (!response.isNull("VerificarTipoPoliticaPortalResult")) {
                            var jsResponse: JSONObject? = null
                            if(activity != null) {
                                jsResponse = Utilitarios.jsObjectDesencriptar(
                                    response.getString("VerificarTipoPoliticaPortalResult"),
                                    requireActivity().applicationContext
                                )
                            }

                            if (jsResponse != null) {

                                try {
                                    val tycAccepted = jsResponse.optString("AceptoPolitica")
                                    val urlPolitica = jsResponse.optString("UrlPolitica")

                                    urlForPolicy = urlPolitica
                                    setCheckboxTextClickable(urlForPolicy)

                                    //Las políticas han sido aceptadas
                                    if (tycAccepted != "0") {
                                        terminosCondicionesChecked = true

                                        if (view != null) {
                                            requireView().linear_layout_container.visibility = View.VISIBLE
                                            requireView().empty_text_view.visibility = View.GONE

                                            requireView().checkbox_terminos_condiciones_pr.visibility =
                                                View.VISIBLE
                                            requireView().checkbox_terminos_condiciones_pr.isChecked = true
                                            requireView().checkbox_terminos_condiciones_pr.isEnabled =
                                                false

                                            CompoundButtonCompat.setButtonTintList(
                                                requireView().checkbox_terminos_condiciones_pr,
                                                ColorStateList.valueOf(
                                                    Color.parseColor("#757575")
                                                )
                                            )

                                        }
                                    } else {
                                        //Las políticas NO han sido aceptadas
                                        terminosCondicionesChecked = false

                                        if (view != null) {
                                            requireView().linear_layout_container.visibility = View.VISIBLE
                                            requireView().empty_text_view.visibility = View.GONE
                                            requireView().checkbox_terminos_condiciones_pr.visibility =
                                                View.VISIBLE
                                            requireView().checkbox_terminos_condiciones_pr.isChecked =
                                                false
                                            requireView().checkbox_terminos_condiciones_pr.isEnabled = true
                                        }
                                    }

                                } catch (e: Exception) {
                                    if (view != null) {
                                        requireView().linear_layout_container.visibility = View.GONE
                                        requireView().empty_text_view.text =
                                            getString(R.string.error_recuperacion_datos)
                                        requireView().empty_text_view.visibility = View.VISIBLE
                                        requireView().checkbox_terminos_condiciones_pr.visibility =
                                            View.GONE
                                    }
                                }
                            } else {
                                if (view != null) {
                                    requireView().linear_layout_container.visibility = View.GONE
                                    requireView().empty_text_view.text =
                                        getString(R.string.no_respuesta_desde_servidor)
                                    requireView().empty_text_view.visibility = View.VISIBLE
                                    requireView().checkbox_terminos_condiciones_pr.visibility = View.GONE
                                }
                            }
                        }
                    },
                    { error ->
                        if(error is TimeoutError || error.networkResponse == null) {
                            if (view != null) {
                                requireView().linear_layout_container.visibility = View.GONE
                                requireView().empty_text_view.text = getString(R.string.no_respuesta_desde_servidor)
                                requireView().empty_text_view.visibility = View.VISIBLE
                                requireView().checkbox_terminos_condiciones_pr.visibility = View.GONE
                            }
                        } else if(error.networkResponse.statusCode == 401) {

                            requireActivity().renewToken { token ->
                                if(!token.isNullOrEmpty()){
                                    verificarTipoPoliticaPortal(url, request)
                                } else {
                                    if (view != null) {
                                        requireView().linear_layout_container.visibility = View.GONE
                                        requireView().empty_text_view.text = getString(R.string.no_respuesta_desde_servidor)
                                        requireView().empty_text_view.visibility = View.VISIBLE
                                        requireView().checkbox_terminos_condiciones_pr.visibility = View.GONE
                                    }
                                }
                            }
                        } else {
                            if (view != null) {
                                requireView().linear_layout_container.visibility = View.GONE
                                requireView().empty_text_view.text = getString(R.string.no_respuesta_desde_servidor)
                                requireView().empty_text_view.visibility = View.VISIBLE
                                requireView().checkbox_terminos_condiciones_pr.visibility = View.GONE
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

                jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
                jsonObjectRequest.tag = TAG

                mRequestQueue?.add(jsonObjectRequest)
            }
        }
    }


    fun aceptarTipoPolitica(url: String, request: JSONObject){

        if(activity != null) {
            val fRequest = Utilitarios.jsObjectEncrypted(request, requireActivity().applicationContext)

            if (fRequest != null) {
                mRequestQueue = Volley.newRequestQueue(requireActivity().applicationContext)
                //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
                val jsonObjectRequest = object: JsonObjectRequest(
                /*val jsonObjectRequest = JsonObjectRequest(*/
                    Request.Method.POST,
                    url,
                    fRequest,
                    { response ->
                        if (!response.isNull("AceptarTipoPoliticaResult")) {
                            var jsResponse: JSONObject? = null
                            if(activity != null) {
                                jsResponse = Utilitarios.jsObjectDesencriptar(
                                    response.getString("AceptarTipoPoliticaResult"),
                                    requireActivity().applicationContext
                                )
                            }

                            if (jsResponse != null) {

                                try {
                                    val mensaje = jsResponse.optString("Mensaje")
                                    val rpta = jsResponse.optString("Rpta")

                                    if (rpta.toInt() > 0) {
                                        terminosCondicionesChecked = true
                                        if (view != null) {
                                            requireView().linear_layout_container.visibility = View.VISIBLE
                                            requireView().empty_text_view.visibility = View.GONE
                                            requireView().checkbox_terminos_condiciones_pr.visibility =
                                                View.VISIBLE
                                            requireView().checkbox_terminos_condiciones_pr.isChecked = true
                                            requireView().checkbox_terminos_condiciones_pr.isEnabled =
                                                false
                                            CompoundButtonCompat.setButtonTintList(
                                                requireView().checkbox_terminos_condiciones_pr,
                                                ColorStateList.valueOf(Color.parseColor("#757575"))
                                            )
                                        }

                                        if (activity != null) {

                                            val snack = Snackbar.make(
                                                requireActivity().findViewById(android.R.id.content),
                                                getString(R.string.politicas_uso_aceptadas_mensaje),
                                                Snackbar.LENGTH_LONG
                                            )
                                            snack.view.setBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireView().context,
                                                    R.color.success
                                                )
                                            )
                                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                                                .setTextColor(
                                                    ContextCompat.getColor(
                                                        requireView().context,
                                                        R.color.success_text
                                                    )
                                                )
                                            snack.show()
                                        }
                                    } else {
                                        terminosCondicionesChecked = false
                                        if (view != null) {
                                            requireView().linear_layout_container.visibility = View.GONE
                                            requireView().empty_text_view.visibility = View.VISIBLE
                                            requireView().empty_text_view.text =
                                                getString(R.string.error_recuperacion_datos)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    if (view != null) {
                                        requireView().linear_layout_container.visibility = View.GONE
                                        requireView().empty_text_view.text =
                                            getString(R.string.error_recuperacion_datos)
                                        requireView().empty_text_view.visibility = View.VISIBLE
                                        requireView().checkbox_terminos_condiciones_pr.visibility =
                                            View.GONE
                                    }
                                }
                            } else {
                                if (view != null) {
                                    requireView().linear_layout_container.visibility = View.GONE
                                    requireView().empty_text_view.text =
                                        getString(R.string.no_respuesta_desde_servidor)
                                    requireView().empty_text_view.visibility = View.VISIBLE
                                    requireView().checkbox_terminos_condiciones_pr.visibility = View.GONE
                                }
                            }
                        }
                    },
                    { error ->
                        if(error is TimeoutError || error.networkResponse == null) {
                            if (view != null) {
                                requireView().linear_layout_container.visibility = View.GONE
                                requireView().empty_text_view.text =
                                    getString(R.string.no_respuesta_desde_servidor)
                                requireView().empty_text_view.visibility = View.VISIBLE
                                requireView().checkbox_terminos_condiciones_pr.visibility = View.GONE
                            }
                        } else if(error.networkResponse.statusCode == 401) {

                            requireActivity().renewToken { token ->
                                if(!token.isNullOrEmpty()){
                                    aceptarTipoPolitica(url, request)
                                } else {
                                    if (view != null) {
                                        requireView().linear_layout_container.visibility = View.GONE
                                        requireView().empty_text_view.text =
                                            getString(R.string.no_respuesta_desde_servidor)
                                        requireView().empty_text_view.visibility = View.VISIBLE
                                        requireView().checkbox_terminos_condiciones_pr.visibility = View.GONE
                                    }
                                }
                            }
                        } else {
                            if (view != null) {
                                requireView().linear_layout_container.visibility = View.GONE
                                requireView().empty_text_view.text =
                                    getString(R.string.no_respuesta_desde_servidor)
                                requireView().empty_text_view.visibility = View.VISIBLE
                                requireView().checkbox_terminos_condiciones_pr.visibility = View.GONE
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
                jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
                jsonObjectRequest.tag = TAG

                mRequestQueue?.add(jsonObjectRequest)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        mRequestQueue?.cancelAll(TAG)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.tyc_menu_action -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(urlForPolicy)
                startActivity(intent)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}