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
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_cursos.view.*
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
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken


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
        view.swPrograma_fprograma.setOnRefreshListener(this)
        view.swPrograma_fprograma.setColorSchemeResources(
            R.color.s1,
            R.color.s2,
            R.color.s3,
            R.color.s4
        )

        view.rvPrograma_fprograma.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireActivity())
        view.rvPrograma_fprograma.adapter = null
        view.lblMensaje_fprograma.typeface = Utilitarios.getFontRoboto(requireActivity(), Utilitarios.TypeFont.THIN)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        showProgramas()
    }

    private fun showProgramas() {
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
        request.put("CodAlumno", usuario.codigo)
        request.put("FiltroProg", "TP")

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, requireActivity())

        if (requestEncriptado != null)
            onProgramas(Utilitarios.getUrl(Utilitarios.URL.PROGRAMAS), requestEncriptado)
        else {
            lblMensaje_fprograma.visibility = View.VISIBLE
            lblMensaje_fprograma.text = requireActivity().resources.getString(R.string.error_encriptar)
        }
    }

    private fun onProgramas(url: String, request: JSONObject) {
        if(view != null){
            prbCargando_fprograma.visibility = View.VISIBLE
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
                    val programaJArray = Utilitarios.jsArrayDesencriptar(response["ListarProgramasPostResult"] as String, requireActivity())
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

                                //getEncuestaPorPrograma (programa)
                                val intent = Intent(requireActivity(), CursosPostActivity::class.java)
                                intent.putExtra("KEY_CODIGO_PROGRAMA", programa.codigo)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            }
                            rvPrograma_fprograma.visibility = View.VISIBLE
                            rvPrograma_fprograma.adapter = adapter
                            lblMensaje_fprograma.visibility = View.GONE
                        } else {
                            rvPrograma_fprograma.visibility = View.GONE
                            lblMensaje_fprograma.visibility = View.VISIBLE
                            lblMensaje_fprograma.text = context!!.resources.getText(R.string.error_programa_no)
                        }
                    } else {
                        rvPrograma_fprograma.visibility = View.GONE
                        lblMensaje_fprograma.visibility = View.VISIBLE
                        lblMensaje_fprograma.text = context!!.resources.getText(R.string.error_desencriptar)
                    }
                } catch (jex: JSONException) {
                    rvPrograma_fprograma.visibility = View.GONE
                    lblMensaje_fprograma.visibility = View.VISIBLE
                    lblMensaje_fprograma.text = context!!.resources.getText(R.string.error_respuesta_server)
                }
                if(view != null){
                    swPrograma_fprograma.isRefreshing = false
                    prbCargando_fprograma.visibility = View.GONE
                }
            },
            { error ->
                if(error is TimeoutError || error.networkResponse == null) {
                    if(view != null) {
                        swPrograma_fprograma.isRefreshing = false
                        prbCargando_fprograma.visibility = View.GONE
                        lblMensaje_fprograma.visibility = View.VISIBLE
                        lblMensaje_fprograma.text = context!!.resources.getText(R.string.error_no_conexion)
                    }
                } else if(error.networkResponse.statusCode == 401) {

                    requireActivity().renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onProgramas(url, request)
                        } else {
                            if(view != null) {
                                swPrograma_fprograma.isRefreshing = false
                                prbCargando_fprograma.visibility = View.GONE
                                lblMensaje_fprograma.visibility = View.VISIBLE
                                lblMensaje_fprograma.text = context!!.resources.getText(R.string.error_no_conexion)
                            }
                        }
                    }
                } else {
                    if(view != null) {
                        swPrograma_fprograma.isRefreshing = false
                        prbCargando_fprograma.visibility = View.GONE
                        lblMensaje_fprograma.visibility = View.VISIBLE
                        lblMensaje_fprograma.text = context!!.resources.getText(R.string.error_no_conexion)
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

    override fun onRefresh() {
        swPrograma_fprograma.isRefreshing = true
        showProgramas()
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
    }

}
