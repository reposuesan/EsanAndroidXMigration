package pe.edu.esan.appostgrado.view.academico.docente


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_cursos.view.*
import kotlinx.android.synthetic.main.fragment_secciones.*
import kotlinx.android.synthetic.main.fragment_secciones.view.*
import org.json.JSONException
import org.json.JSONObject

import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.SeccionAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Seccion
import pe.edu.esan.appostgrado.entidades.UserEsan
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken


/**
 * A simple [Fragment] subclass.
 */
class SeccionesFragment : androidx.fragment.app.Fragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    private val TAG = "SeccionesFragment"

    private var requestQueue : RequestQueue? = null

    private val LOG = SeccionesFragment::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_secciones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        view.swCurso_fseccion.setOnRefreshListener(this)
        view.swCurso_fseccion.setColorSchemeResources(
            R.color.s1,
            R.color.s2,
            R.color.s3,
            R.color.s4
        )

        view.rvCurso_fseccion.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireActivity())
        view.rvCurso_fseccion.adapter = null
        view.lblMensaje_fseccion.typeface = Utilitarios.getFontRoboto(requireActivity(), Utilitarios.TypeFont.THIN)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showSecciones()
    }

    private fun showSecciones() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForFragmentPublic.observe(viewLifecycleOwner,
                Observer<Boolean> { value ->
                    if(value){
                        sendRequest()
                    }
                }
            )
            controlViewModel.refreshDataForFragment(true)

        }
    }

    private fun sendRequest(){
        val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

        val request = JSONObject()
        request.put("CodigoProfesor", usuario.codigo)

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, requireActivity())

        if (requestEncriptado != null) {
            onSecciones(Utilitarios.getUrl(Utilitarios.URL.SECCIONES), requestEncriptado)
        } else {
            lblMensaje_fseccion.visibility = View.VISIBLE
            lblMensaje_fseccion.text = requireActivity().resources.getString(R.string.error_encriptar)
        }
    }

    private fun onSecciones(url: String, request: JSONObject) {

        if(view != null) {
            prbCargando_fseccion.visibility = View.VISIBLE
        }
        requestQueue = Volley.newRequestQueue(requireActivity())
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
            { response ->
                try {
                    val seccionJArray = Utilitarios.jsArrayDesencriptar(response["ListarSeccionesActualesPorProfesorResult"] as String, requireActivity())
                    if (seccionJArray != null) {

                        if (seccionJArray.length() > 0) {

                            val listSecciones = ArrayList<Seccion>()

                            for (s in 0 until seccionJArray.length()) {
                                val seccionJson = seccionJArray[s] as JSONObject
                                val idSeccion = seccionJson["IdSeccion"] as Int
                                val promocion = seccionJson["PromocionNombre"] as String
                                val seccionCodigo = seccionJson["SeccionCodigo"] as String
                                val nombreCurso = seccionJson["CursoNombre"] as String
                                val codigoCurso = seccionJson["CursoCodigo"] as String

                                listSecciones.add(Seccion(idSeccion, promocion, nombreCurso, seccionCodigo, codigoCurso))
                            }

                            val adapter = SeccionAdapter(listSecciones) { seccion ->

                                ControlUsuario.instance.currentSeccion = seccion
                                val intentOpciones = Intent(requireActivity(), SeccionOpcionesActivity::class.java)
                                intentOpciones.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intentOpciones)
                            }

                            rvCurso_fseccion.adapter = adapter
                            lblMensaje_fseccion.visibility = View.GONE
                        } else {
                            lblMensaje_fseccion.visibility = View.VISIBLE
                            lblMensaje_fseccion.text = context!!.resources.getText(R.string.error_seccion_no)
                        }
                    } else {
                        lblMensaje_fseccion.visibility = View.VISIBLE
                        lblMensaje_fseccion.text = context!!.resources.getText(R.string.error_desencriptar)
                    }
                } catch (jex: JSONException) {
                    lblMensaje_fseccion.visibility = View.VISIBLE
                    lblMensaje_fseccion.text = context!!.resources.getText(R.string.error_respuesta_server)
                } catch (ccex: ClassCastException) {
                    lblMensaje_fseccion.visibility = View.VISIBLE
                    lblMensaje_fseccion.text = context!!.resources.getText(R.string.error_respuesta_server)
                }

                swCurso_fseccion.isRefreshing = false
                prbCargando_fseccion.visibility = View.GONE
            },
            { error ->
                if(error is TimeoutError){
                    if(view != null) {
                        swCurso_fseccion.isRefreshing = false
                        prbCargando_fseccion.visibility = View.GONE
                        lblMensaje_fseccion.visibility = View.VISIBLE
                        lblMensaje_fseccion.text = context!!.resources.getText(R.string.error_no_conexion)
                    }
                } else if(error.networkResponse.statusCode == 401) {

                    requireActivity().renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onSecciones(url, request)
                        } else {
                            if(view != null) {
                                swCurso_fseccion.isRefreshing = false
                                prbCargando_fseccion.visibility = View.GONE
                                lblMensaje_fseccion.visibility = View.VISIBLE
                                lblMensaje_fseccion.text = context!!.resources.getText(R.string.error_no_conexion)
                            }
                        }
                    }
                } else {
                    if(view != null) {
                        swCurso_fseccion.isRefreshing = false
                        prbCargando_fseccion.visibility = View.GONE
                        lblMensaje_fseccion.visibility = View.VISIBLE
                        lblMensaje_fseccion.text = context!!.resources.getText(R.string.error_no_conexion)
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

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
    }

    override fun onRefresh() {
        swCurso_fseccion.isRefreshing = true
        showSecciones()
    }

}// Required empty public constructor
