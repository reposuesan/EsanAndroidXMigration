package pe.edu.esan.appostgrado.view.academico.postgrado


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
import kotlinx.android.synthetic.main.fragment_programas.*
import kotlinx.android.synthetic.main.fragment_programas.view.*
import org.json.JSONException
import org.json.JSONObject

import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.ProgramaAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Programa
import pe.edu.esan.appostgrado.entidades.UserEsan
import pe.edu.esan.appostgrado.util.Utilitarios


/**
 * A simple [Fragment] subclass.
 */
class ProgramasFragment : androidx.fragment.app.Fragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    private val TAG = "ProgramasFragment"

    private var requestQueue : RequestQueue? = null

    private val LOG = ProgramasFragment::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_programas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        Log.w(LOG, "ViewModel is: $controlViewModel")
        view.swPrograma_fprograma.setOnRefreshListener(this)
        view.swPrograma_fprograma.setColorSchemeResources(
            R.color.s1,
            R.color.s2,
            R.color.s3,
            R.color.s4
        )

        view.rvPrograma_fprograma.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(activity)
        view.rvPrograma_fprograma.adapter = null
        view.lblMensaje_fprograma.typeface = Utilitarios.getFontRoboto(activity!!, Utilitarios.TypeFont.THIN)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        showProgramas()
    }

    private fun showProgramas() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForFragmentPublic.observe(this,
                Observer<Boolean> { value ->
                    if(value){
                        Log.w(LOG, "operationFinishedProgramasPublic.observe() was called")
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
        request.put("CodAlumno", usuario.codigo)
        request.put("FiltroProg", "TP")

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, activity!!)
        println(requestEncriptado)
        if (requestEncriptado != null)
            onProgramas(Utilitarios.getUrl(Utilitarios.URL.PROGRAMAS), requestEncriptado)
        else {
            lblMensaje_fprograma.visibility = View.VISIBLE
            lblMensaje_fprograma.text = activity!!.resources.getString(R.string.error_encriptar)
        }
    }

    private fun onProgramas(url: String, request: JSONObject) {
        println(url)
        println(request.toString())
        prbCargando_fprograma.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(activity)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    try {
                        val programaJArray = Utilitarios.jsArrayDesencriptar(response["ListarProgramasPostResult"] as String, activity!!)
                        //val programaJArray = response["ListarProgramasPostResult"] as JSONArray
                        if (programaJArray != null) {
                            if (programaJArray.length() > 0) {
                                val listProgramas = ArrayList<Programa>()
                                for (p in 0 until programaJArray.length()) {
                                    val programaJson = programaJArray[p] as JSONObject
                                    val codigo = programaJson["PromocionCodigo"] as String
                                    val nombre = programaJson["PromocionNombre"] as String
                                    listProgramas.add(Programa(codigo, nombre))
                                }

                                val adapter = ProgramaAdapter(listProgramas) { programa ->
                                    println(programa.codigo)

                                    //getEncuestaPorPrograma (programa)
                                    val intent = Intent(activity, CursosPostActivity::class.java)
                                    intent.putExtra("KEY_CODIGO_PROGRAMA", programa.codigo)
                                    startActivity(intent)
                                }

                                rvPrograma_fprograma.adapter = adapter
                                lblMensaje_fprograma.visibility = View.GONE
                            } else {
                                lblMensaje_fprograma.visibility = View.VISIBLE
                                lblMensaje_fprograma.text = context!!.resources.getText(R.string.error_programa_no)
                            }
                        } else {
                            lblMensaje_fprograma.visibility = View.VISIBLE
                            lblMensaje_fprograma.text = context!!.resources.getText(R.string.error_desencriptar)
                        }
                    } catch (jex: JSONException) {
                        lblMensaje_fprograma.visibility = View.VISIBLE
                        lblMensaje_fprograma.text = context!!.resources.getText(R.string.error_respuesta_server)
                    }
                    swPrograma_fprograma.isRefreshing = false
                    prbCargando_fprograma.visibility = View.GONE
                },
                Response.ErrorListener { error ->
                    println(error.message)
                    swPrograma_fprograma.isRefreshing = false
                    prbCargando_fprograma.visibility = View.GONE
                    lblMensaje_fprograma.visibility = View.VISIBLE
                    lblMensaje_fprograma.text = context!!.resources.getText(R.string.error_no_conexion)
                }
        )
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(1500, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }

    /*
    override fun onStart() {
        super.onStart()
        Toast.makeText(activity, "START: " + activity.supportFragmentManager.backStackEntryCount, Toast.LENGTH_SHORT).show()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        //(activity.main_toolbar as Toolbar).toolbar_title.text = "Programas"
    }
    */

    override fun onRefresh() {
        println("REFRESH")
        swPrograma_fprograma.isRefreshing = true
        showProgramas()
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
    }

}// Required empty public constructor
