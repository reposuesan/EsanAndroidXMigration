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
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
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
        // Inflate the layout for this fragment
        //println(TAG)
        return inflater.inflate(R.layout.fragment_secciones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        Log.w(LOG, "ViewModel is: $controlViewModel")
        view.swCurso_fseccion.setOnRefreshListener(this)
        view.swCurso_fseccion.setColorSchemeResources(
            R.color.s1,
            R.color.s2,
            R.color.s3,
            R.color.s4
        )

        view.rvCurso_fseccion.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(activity)
        view.rvCurso_fseccion.adapter = null
        view.lblMensaje_fseccion.typeface = Utilitarios.getFontRoboto(activity!!, Utilitarios.TypeFont.THIN)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        println(TAG)
        showSecciones()
    }

    private fun showSecciones() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForFragmentPublic.observe(viewLifecycleOwner,
                Observer<Boolean> { value ->
                    if(value){
                        Log.w(LOG, "operationFinishedSeccionesPublic.observe() was called")
                        Log.w(LOG, "sendRequest() was called")
                        sendRequest()
                    }
                }
            )
            Log.w(LOG, "controlViewModel.refreshData() was called")
            controlViewModel.refreshDataForFragment(true)

        }
    }

    private fun sendRequest(){
        val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

        val request = JSONObject()
        request.put("CodigoProfesor", usuario.codigo)

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, activity!!)
        println(request)
        println(requestEncriptado)
        if (requestEncriptado != null) {
            onSecciones(Utilitarios.getUrl(Utilitarios.URL.SECCIONES), requestEncriptado)
        } else {
            lblMensaje_fseccion.visibility = View.VISIBLE
            lblMensaje_fseccion.text = activity!!.resources.getString(R.string.error_encriptar)
        }
    }

    private fun onSecciones(url: String, request: JSONObject) {
        println(url)
        println(request.toString())
        prbCargando_fseccion.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(activity)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    try {
                        val seccionJArray = Utilitarios.jsArrayDesencriptar(response["ListarSeccionesActualesPorProfesorResult"] as String, activity!!)
                        if (seccionJArray != null) {
                            println(seccionJArray)
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
                                    //println(seccion.nombreCurso)
                                    ControlUsuario.instance.currentSeccion = seccion
                                    val intentOpciones = Intent(activity, SeccionOpcionesActivity::class.java)
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
                Response.ErrorListener { error ->
                    println(error.message)
                    swCurso_fseccion.isRefreshing = false
                    prbCargando_fseccion.visibility = View.GONE
                    lblMensaje_fseccion.visibility = View.VISIBLE
                    lblMensaje_fseccion.text = context!!.resources.getText(R.string.error_no_conexion)
                }
        )
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
    }

    override fun onRefresh() {
        println("REFRESH")
    }

}// Required empty public constructor
