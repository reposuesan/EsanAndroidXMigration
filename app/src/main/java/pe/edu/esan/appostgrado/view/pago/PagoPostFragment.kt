package pe.edu.esan.appostgrado.view.pago


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
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
import kotlinx.android.synthetic.main.fragment_pago_post.*
import kotlinx.android.synthetic.main.fragment_pago_post.view.*
import org.json.JSONException
import org.json.JSONObject

import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PagoPostAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.PagoPost
import pe.edu.esan.appostgrado.util.Utilitarios


/**
 * A simple [Fragment] subclass.
 */
class PagoPostFragment : androidx.fragment.app.Fragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    private val TAG = "PagoPostFragment"

    private var requestQueue : RequestQueue? = null

    private val LOG = PagoPostFragment::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pago_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        Log.w(LOG, "ViewModel is: $controlViewModel")
        view.lblMensaje_fpagopost.typeface = Utilitarios.getFontRoboto(context!!, Utilitarios.TypeFont.REGULAR)

        view.swPago_fpagopost.setOnRefreshListener(this)
        view.swPago_fpagopost.setColorSchemeResources(
            R.color.s1,
            R.color.s2,
            R.color.s3,
            R.color.s4
        )
        view.lblMensaje_fpagopost.typeface = Utilitarios.getFontRoboto(activity!!, Utilitarios.TypeFont.THIN)
        view.rvPago_fpagopost.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(activity!!)
        view.rvPago_fpagopost.adapter = null
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        showPagoPost()
    }



    private fun showPagoPost() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForFragmentPublic.observe(viewLifecycleOwner,
                Observer<Boolean> { value ->
                    if(value){
                        Log.w(LOG, "operationFinishedPagoPostPublic.observe() was called")
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
        val usuario = ControlUsuario.instance.currentUsuario[0] as Alumno

        val request = JSONObject()
        request.put("codigo", usuario.codigo)
        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, activity!!)
        if (requestEncriptado != null) {
            onPagoPost(Utilitarios.getUrl(Utilitarios.URL.PAGO_POST), requestEncriptado)
        } else {
            lblMensaje_fpagopost.visibility = View.VISIBLE
            lblMensaje_fpagopost.text = activity!!.resources.getString(R.string.error_encriptar)
        }
    }



    private fun onPagoPost(url: String, request: JSONObject) {

        Log.i(LOG, url)

        Log.i(LOG, request.toString())

        prbCargando_fpagopost.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(activity!!)

        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
            { response ->
                try {

                    Log.i(LOG, response.toString())
                    val pagopostJArray = Utilitarios.jsArrayDesencriptar(response["ListaProgramacionPagosxAlumnoPosgradoResult"] as String, activity!!)

                    Log.i(LOG, pagopostJArray.toString())
                    if (pagopostJArray != null) {
                        if (pagopostJArray.length() > 0) {
                            val listaPago = ArrayList<PagoPost>()
                            for (p in 0 until pagopostJArray.length()) {
                                val pagopostJson = pagopostJArray[p] as JSONObject
                                val diasvencidos = pagopostJson["DiasVencimiento"] as Double
                                val fechavencimiento = pagopostJson["FechaVencimiento"] as String
                                val importe = pagopostJson["Importe"] as Double
                                val moneda = pagopostJson["Moneda"] as String
                                val interes = pagopostJson["Intereses"] as Double
                                val penalidad = pagopostJson["Mora"] as Double
                                val pagodolares = pagopostJson["PagarDolares"] as Double
                                val pagoSoles = pagopostJson["PagarSoles"] as Double
                                var simbolo = ""
                                var total = ""
                                if (moneda == "PEN") {
                                    simbolo = "S/"
                                    total = "S/ $pagoSoles"
                                } else {
                                    simbolo = "$"
                                    total = "$ $pagodolares"
                                }

                                listaPago.add(PagoPost(fechavencimiento, diasvencidos, simbolo + importe, simbolo + penalidad, simbolo + interes, total))
                            }
                            rvPago_fpagopost.adapter = PagoPostAdapter(listaPago)
                            lblMensaje_fpagopost.visibility = View.GONE
                        } else {
                            lblMensaje_fpagopost.visibility = View.VISIBLE
                            lblMensaje_fpagopost.text = context!!.resources.getText(R.string.error_pago_no)
                        }
                    } else {
                        lblMensaje_fpagopost.visibility = View.VISIBLE
                        lblMensaje_fpagopost.text = context!!.resources.getText(R.string.error_respuesta_server)
                    }
                } catch (jex: JSONException) {
                    lblMensaje_fpagopost.visibility = View.VISIBLE
                    lblMensaje_fpagopost.text = context!!.resources.getText(R.string.error_respuesta_server)
                } catch (ccax: ClassCastException) {
                    lblMensaje_fpagopost.visibility = View.VISIBLE
                    lblMensaje_fpagopost.text = context!!.resources.getText(R.string.error_respuesta_server)
                }

                prbCargando_fpagopost.visibility = View.GONE
                swPago_fpagopost.isRefreshing = false
            },
            { error ->
                Log.e(LOG, error.message.toString())
                prbCargando_fpagopost.visibility = View.GONE
                rvPago_fpagopost.visibility = View.GONE
                swPago_fpagopost.isRefreshing = false
                lblMensaje_fpagopost.visibility = View.VISIBLE
                lblMensaje_fpagopost.text =  context!!.resources.getString(R.string.error_default);
                /*lblMensaje_fpagopost.text = context!!.resources.getText(R.string.error_no_conexion)*/
            }
        )
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }



    override fun onRefresh() {
        swPago_fpagopost.isRefreshing = true
        showPagoPost()
    }



    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
    }

}
