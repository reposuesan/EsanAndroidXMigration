package pe.edu.esan.appostgrado.view.pago


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_pago_pre.*
import kotlinx.android.synthetic.main.fragment_pago_pre.view.*
import org.json.JSONException
import org.json.JSONObject

import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PagoPreAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.PagoPre
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken


class PagoPreFragment : androidx.fragment.app.Fragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    private val TAG = "PagoPreFragment"

    private var requestQueue : RequestQueue? = null

    private val LOG = PagoPreFragment::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view = inflater.inflate(R.layout.fragment_pago_pre, container, false)
        } else{
            view = inflater.inflate(R.layout.fragment_pago_pre_old_version, container, false)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        view.lblMensaje_fpagopre.typeface = Utilitarios.getFontRoboto(context!!, Utilitarios.TypeFont.REGULAR)

        view.swPago_fpagopre.setOnRefreshListener(this)
        view.swPago_fpagopre.setColorSchemeResources(
            R.color.s1,
            R.color.s2,
            R.color.s3,
            R.color.s4
        )
        view.lblMensaje_fpagopre.typeface = Utilitarios.getFontRoboto(requireActivity(), Utilitarios.TypeFont.THIN)
        view.rvPago_fpagopre.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireActivity())
        view.rvPago_fpagopre.adapter = null

        view.lblMensaje_fpagopre.setOnClickListener {
            showPagoPre()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showPagoPre()
    }

    private fun showPagoPre () {
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
        val usuario = ControlUsuario.instance.currentUsuario[0] as Alumno

        val request = JSONObject()
        request.put("codigo", usuario.codigo)
        request.put("idprocesomatricula", -1)
        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, requireActivity())
        if (requestEncriptado != null) {
            onPagoPre(Utilitarios.getUrl(Utilitarios.URL.PAGO_PRE), requestEncriptado)
        } else {
            lblMensaje_fpagopre.visibility = View.VISIBLE
            lblMensaje_fpagopre.text = requireActivity().resources.getString(R.string.error_encriptar)
        }
    }

    private fun onPagoPre(url: String, request: JSONObject) {

        if(view != null) {
            prbCargando_fpagopre.visibility = View.VISIBLE
        }
        if(isAdded) {
            requestQueue = Volley.newRequestQueue(requireActivity())
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            val jsObjectRequest = object: JsonObjectRequest(
                /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
                { response ->
                    try {
                        val pagopreJArray = Utilitarios.jsArrayDesencriptar(response["ListaProgramacionPagosxAlumnoPregradoResult"] as String, requireActivity().applicationContext)

                        if (pagopreJArray != null) {
                            if (pagopreJArray.length() > 0) {
                                val listPago = ArrayList<PagoPre>()
                                var ultimoPeriodo = ""
                                for (p in 0 until pagopreJArray.length()) {
                                    val pagopreJson = pagopreJArray[p] as JSONObject
                                    val codigo = pagopreJson["codigo"] as String
                                    val periodo = pagopreJson["periodo"] as String
                                    val moneda = pagopreJson["moneda"] as String
                                    val montoneto = pagopreJson["montoneto"] as String
                                    val monto = "$moneda $montoneto"
                                    val concepto = pagopreJson["preciodescripcion"] as String
                                    val vencimiento = pagopreJson["vencimiento"] as String

                                    if (p == 0) {
                                        ultimoPeriodo = periodo
                                        listPago.add(PagoPre(Utilitarios.TipoFila.CABECERA, ultimoPeriodo))
                                        listPago.add(PagoPre(Utilitarios.TipoFila.DETALLE, "", codigo, concepto, monto, vencimiento))
                                    } else {
                                        if (ultimoPeriodo == periodo) {
                                            listPago.add(PagoPre(Utilitarios.TipoFila.DETALLE, "", codigo, concepto, monto, vencimiento))
                                        } else {
                                            /*ultimoPeriodo = periodo*/
                                            listPago.add(PagoPre(Utilitarios.TipoFila.CABECERA, ultimoPeriodo))
                                            listPago.add(PagoPre(Utilitarios.TipoFila.DETALLE, "", codigo, concepto, monto, vencimiento))
                                        }
                                    }
                                }
                                val adapter = PagoPreAdapter(listPago) { pagoPre ->

                                    if (pagoPre.codigo?.trim() != "") {

                                        val webpageRecibo: Uri = Uri.parse(Utilitarios.getBoletasPreUrl(pagoPre.codigo!!))
                                        val intent = Intent(Intent.ACTION_VIEW, webpageRecibo)

                                        if(intent.resolveActivity(requireActivity().packageManager) != null){
                                            startActivity(intent)
                                        }

                                        //This uses a WebView which does not work well with PDF files
                                        /*val intentBoleta = Intent().setClass(requireActivity(), PagoPreBoletaActivity::class.java!!)
                                        intentBoleta.putExtra("KEY_CODBOLETA", pagoPre.codigo)
                                        intentBoleta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        requireActivity().startActivity(intentBoleta)*/
                                    }
                                }
                                rvPago_fpagopre.adapter = adapter
                                lblMensaje_fpagopre.visibility = View.GONE
                            } else {
                                lblMensaje_fpagopre.visibility = View.VISIBLE
                                lblMensaje_fpagopre.text = context!!.resources.getText(R.string.error_pago_no)
                            }
                        } else {
                            lblMensaje_fpagopre.visibility = View.VISIBLE
                            lblMensaje_fpagopre.text = context!!.resources.getText(R.string.error_respuesta_server)
                        }
                    } catch (jex: JSONException) {

                        lblMensaje_fpagopre.visibility = View.VISIBLE
                        lblMensaje_fpagopre.text = context!!.resources.getText(R.string.error_respuesta_server)
                    } catch (ccax: ClassCastException) {
                        lblMensaje_fpagopre.visibility = View.VISIBLE
                        lblMensaje_fpagopre.text = context!!.resources.getText(R.string.error_respuesta_server)
                    }

                    prbCargando_fpagopre.visibility = View.GONE
                    swPago_fpagopre.isRefreshing = false
                },
                { error ->
                    if(error is TimeoutError || error.networkResponse == null) {
                        if(view != null) {
                            rvPago_fpagopre.visibility = View.GONE
                            prbCargando_fpagopre.visibility = View.GONE
                            swPago_fpagopre.isRefreshing = false
                            lblMensaje_fpagopre.visibility = View.VISIBLE
                            lblMensaje_fpagopre.text = context!!.resources.getText(R.string.error_default)
                        }
                    } else if(error.networkResponse.statusCode == 401) {

                        requireActivity().renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                onPagoPre(url, request)
                            } else {
                                if(view != null) {
                                    rvPago_fpagopre.visibility = View.GONE
                                    prbCargando_fpagopre.visibility = View.GONE
                                    swPago_fpagopre.isRefreshing = false
                                    lblMensaje_fpagopre.visibility = View.VISIBLE
                                    lblMensaje_fpagopre.text = context!!.resources.getText(R.string.error_default)
                                }
                            }                        }
                    } else {
                        if(view != null) {
                            rvPago_fpagopre.visibility = View.GONE
                            prbCargando_fpagopre.visibility = View.GONE
                            swPago_fpagopre.isRefreshing = false
                            lblMensaje_fpagopre.visibility = View.VISIBLE
                            lblMensaje_fpagopre.text = context!!.resources.getText(R.string.error_default)
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

    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
    }

    override fun onRefresh() {
        swPago_fpagopre.isRefreshing = true
        showPagoPre()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
