package pe.edu.esan.appostgrado.view.puntosreunion.postgrado


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_cursos.view.*
import kotlinx.android.synthetic.main.fragment_puntos_reunion_postgrado.*
import kotlinx.android.synthetic.main.fragment_puntos_reunion_postgrado.view.*
import org.json.JSONException
import org.json.JSONObject

import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PromocionArrayAdapter
import pe.edu.esan.appostgrado.adapter.PuntosReunionOpcionAdapter
import pe.edu.esan.appostgrado.adapter.TipoGrupoArrayAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.*
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken
import pe.edu.esan.appostgrado.view.puntosreunion.postgrado.confirmar.PRConfirmarActivity
import pe.edu.esan.appostgrado.view.puntosreunion.postgrado.crear.PRMiGrupoActivity
import pe.edu.esan.appostgrado.view.puntosreunion.postgrado.mireserva.PRMisReservasActivity
import pe.edu.esan.appostgrado.view.puntosreunion.postgrado.reserva.PRReservaPrimeraActivity


/**
 * A simple [Fragment] subclass.
 */
class PuntosReunionPosgradoFragment : androidx.fragment.app.Fragment() {

    private val TAG = "PuntosReunionPosgradoFragment"

    private var requestQueue : RequestQueue? = null
    private var requestQueue2 : RequestQueue? = null
    private var requestQueue3 : RequestQueue? = null

    private val LOG = PuntosReunionPosgradoFragment::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_puntos_reunion_postgrado, container, false)

        view.rvOpciones_fpuntosreunion.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(activity!!)
        view.rvOpciones_fpuntosreunion.adapter = null

        view.lblMensaje_fpuntosreunion.typeface = Utilitarios.getFontRoboto(activity!!, Utilitarios.TypeFont.THIN)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        view.rvOpciones_fpuntosreunion.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(activity!!)
        view.rvOpciones_fpuntosreunion.adapter = null

        view.lblMensaje_fpuntosreunion.typeface = Utilitarios.getFontRoboto(activity!!, Utilitarios.TypeFont.THIN)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        showPromocion()
    }

    override fun onResume() {
        super.onResume()
        if (ControlUsuario.instance.esReservaExitosa) {
            ControlUsuario.instance.esReservaExitosa = false
            showPromocion()
        }
    }

    private fun showPromocion() {
        viewTipoGrupo_fpuntosreunion.visibility = View.GONE
        viewPrograma_fpuntosreunion.visibility = View.GONE
        rvOpciones_fpuntosreunion.visibility = View.GONE
        lblMensaje_fpuntosreunion.visibility = View.GONE

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
        request.put("CodAlumno", usuario.codigo)
        request.put("Facultad", if (usuario.tipoAlumno.equals(Utilitarios.POS)) 1 else 2)

        val requestEncritado = Utilitarios.jsObjectEncrypted(request, activity!!)

        if (requestEncritado != null)
            onPromocion(Utilitarios.getUrl(Utilitarios.URL.PR_PROMOCION), requestEncritado)
        else {
            lblMensaje_fpuntosreunion.visibility = View.VISIBLE
            lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_encriptar)
        }
    }

    private fun onPromocion(url: String, request: JSONObject) {

        prbCargando_fpuntosreunion.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(activity!!)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
            { response ->
                try {
                    if (!response.isNull("ListarPromocionxAlumnoResult")) {
                        val promocionJArray = Utilitarios.jsArrayDesencriptar(response["ListarPromocionxAlumnoResult"] as String, context!!)

                        if (promocionJArray != null) {
                            if (promocionJArray.length() > 0) {
                                val listaPromocion = ArrayList<Promocion>()
                                for (i in 0 until promocionJArray.length()) {
                                    val promocionJObject = promocionJArray[i] as JSONObject
                                    val idPromo = promocionJObject["IdPromocion"] as Int
                                    val codigo = promocionJObject["PromocionCodigo"] as String
                                    val nombre = promocionJObject["PromocionNombre"] as String
                                    val idGrupoPromo = promocionJObject["IdGrupo"] as Int

                                    listaPromocion.add(Promocion(idPromo, codigo, nombre, idGrupoPromo))
                                }

                                val promocionAdapter = PromocionArrayAdapter(activity!!, listaPromocion)
                                cmbPromocion_fpuntosreunion.adapter = promocionAdapter
                                cmbPromocion_fpuntosreunion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                        lblMensaje_fpuntosreunion.visibility = View.GONE
                                        showTipoGrupo(parent.getItemAtPosition(position) as Promocion)
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>) {

                                    }
                                }

                                if (listaPromocion.size > 1) {
                                    viewPrograma_fpuntosreunion.visibility = View.VISIBLE
                                } else {
                                    showTipoGrupo(listaPromocion[0])
                                }
                            } else {
                                lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                                lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_permiso_opcion)
                            }
                        } else {
                            lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                            lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_desencriptar)
                        }
                    } else {
                        lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                        lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_permiso_opcion)
                    }
                } catch (jex: JSONException) {
                    rvOpciones_fpuntosreunion.visibility = View.GONE
                    lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                    lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_no_conexion)
                } catch (ccex: ClassCastException) {
                    rvOpciones_fpuntosreunion.visibility = View.GONE
                    lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                    lblMensaje_fpuntosreunion.text = context!!.resources.getText(R.string.error_respuesta_server)
                }
                prbCargando_fpuntosreunion.visibility = View.GONE
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {

                    requireActivity().renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onPromocion(url, request)
                        } else {
                            if(view != null) {
                                prbCargando_fpuntosreunion.visibility = View.GONE
                                lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                                lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_no_conexion)
                            }
                        }
                    }
                } else {
                    if(view != null) {
                        prbCargando_fpuntosreunion.visibility = View.GONE
                        lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                        lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_no_conexion)
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
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }

    private fun showTipoGrupo(promocion: Promocion) {
        rvOpciones_fpuntosreunion.visibility = View.GONE
        lblMensaje_fpuntosreunion.visibility = View.GONE

        val request = JSONObject()
        request.put("IdPromocion", promocion.id)
        request.put("IdGrupo", promocion.idgrupo)

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, context!!)

        if (requestEncriptado != null)
            onTipoGrupo(Utilitarios.getUrl(Utilitarios.URL.PR_CONFIGURACION), requestEncriptado, promocion.id, promocion.idgrupo)
        else {
            lblMensaje_fpuntosreunion.visibility = View.VISIBLE
            lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_encriptar)
        }
    }

    private fun onTipoGrupo(url: String, request: JSONObject, idPromocion: Int, idGrupo: Int) {

        prbCargando_fpuntosreunion.visibility = View.VISIBLE
        requestQueue2 = Volley.newRequestQueue(activity!!)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
            { response ->
                try {
                    if (!response.isNull("ListarConfiguracionPromocionxAlumnoResult")) {

                        val tipogrupoJArray = Utilitarios.jsArrayDesencriptar(response["ListarConfiguracionPromocionxAlumnoResult"] as String, context!!)

                        if (tipogrupoJArray != null) {
                            if (tipogrupoJArray.length() > 0) {
                                val listaTipoGrupo = ArrayList<TipoGrupo>()
                                for (i in 0 until tipogrupoJArray.length()) {
                                    val tipogrupoJObject = tipogrupoJArray[i] as JSONObject
                                    val creagrupos = tipogrupoJObject["CreaGrupos"] as? Int ?: 0
                                    val idConfiguracion = tipogrupoJObject["IdConfiguracion"] as Int
                                    val tipoGrupo = tipogrupoJObject["ValTabla"] as String
                                    val cantMaxAlumno = tipogrupoJObject["CantMaxAlumnos"] as Int

                                    listaTipoGrupo.add(TipoGrupo(idConfiguracion, idPromocion, idGrupo, creagrupos, tipoGrupo, cantMaxAlumno))
                                }

                                val tipogrupoAdapter = TipoGrupoArrayAdapter(context!!, listaTipoGrupo)
                                cmbTipoGrupo_fpuntosreunion.adapter = tipogrupoAdapter
                                cmbTipoGrupo_fpuntosreunion.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                        lblMensaje_fpuntosreunion.visibility = View.GONE
                                        if (listaTipoGrupo[p2].creagrupo == 1) {

                                            ControlUsuario.instance.prPromocionConfig = listaTipoGrupo[p2]
                                            val listaOpciones = ArrayList<MasOpcion>()
                                            listaOpciones.add(MasOpcion(1, Intent(activity!!, PRMiGrupoActivity::class.java), context!!.resources.getString(R.string.grupos), context!!.resources.getString(R.string.sub_grupos), activity!!.resources.getDrawable(R.drawable.tab_grupo)))

                                            val puntosAdapter = PuntosReunionOpcionAdapter(listaOpciones) { masOpcion ->
                                                masOpcion.intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                activity!!.startActivity(masOpcion.intent)
                                            }

                                            rvOpciones_fpuntosreunion.adapter = puntosAdapter
                                            rvOpciones_fpuntosreunion.visibility = View.VISIBLE
                                        } else {
                                            showDetalleAlumno(listaTipoGrupo[p2].idConfiguracion)
                                        }
                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {

                                    }
                                }



                                if (listaTipoGrupo.size > 1) {
                                    viewTipoGrupo_fpuntosreunion.visibility = View.VISIBLE
                                } else {
                                    if (listaTipoGrupo[0].creagrupo == 1) {
                                        viewTipoGrupo_fpuntosreunion.visibility = View.GONE
                                        val listaOpciones = ArrayList<MasOpcion>()

                                        ControlUsuario.instance.prPromocionConfig = listaTipoGrupo[0]
                                        listaOpciones.add(MasOpcion(1, Intent(activity!!, PRMiGrupoActivity::class.java), context!!.resources.getString(R.string.grupos), context!!.resources.getString(R.string.sub_grupos), activity!!.resources.getDrawable( R.drawable.tab_grupo)))

                                        val puntosAdapter = PuntosReunionOpcionAdapter(listaOpciones) { masOpcion ->
                                            masOpcion.intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            activity!!.startActivity(masOpcion.intent)
                                        }

                                        rvOpciones_fpuntosreunion.adapter = puntosAdapter
                                        rvOpciones_fpuntosreunion.visibility = View.VISIBLE
                                    } else {
                                        showDetalleAlumno(listaTipoGrupo[0].idConfiguracion)
                                    }
                                }
                            } else {
                                lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                                lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_permiso_opcion)
                            }
                        } else {
                            lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                            lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_desencriptar)
                        }
                    } else {
                        lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                        lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_permiso_opcion)
                    }
                }catch (jex: JSONException) {
                    lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                    lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_no_conexion)
                }
                prbCargando_fpuntosreunion.visibility = View.GONE
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {

                    requireActivity().renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onTipoGrupo(url, request, idPromocion, idGrupo)
                        } else {
                            if(view != null) {
                                prbCargando_fpuntosreunion.visibility = View.GONE
                                lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                                lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_no_conexion)
                            }
                        }
                    }
                } else {
                    if(view != null) {
                        prbCargando_fpuntosreunion.visibility = View.GONE
                        lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                        lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_no_conexion)
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
        jsObjectRequest.tag = TAG
        requestQueue2?.add(jsObjectRequest)
    }

    private fun showDetalleAlumno (idConfiguracion: Int) {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val usuario = ControlUsuario.instance.currentUsuario[0] as Alumno

            val request = JSONObject()
            request.put("CodAlumno", usuario.codigo)
            request.put("IdConfiguracion", idConfiguracion)
            request.put("Fecha", "0")

            val requestEncriptar = Utilitarios.jsObjectEncrypted(request, context!!)
            if (requestEncriptar != null)
                onDetalleAlumno(Utilitarios.getUrl(Utilitarios.URL.PR_DETALLEGRUPO), idConfiguracion, requestEncriptar)
            else {
                lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_encriptar)
            }
        } else {
            lblMensaje_fpuntosreunion.visibility = View.VISIBLE
            lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_ingreso)
        }
    }

    private fun onDetalleAlumno(url: String, idConfiguracion: Int, request: JSONObject) {

        prbCargando_fpuntosreunion.visibility = View.VISIBLE
        requestQueue3 = Volley.newRequestQueue(activity!!)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
            { response ->
                try {
                    if (!response.isNull("ListarHorasxAlumnoResult")) {
                        val detalleJArray = Utilitarios.jsArrayDesencriptar(response["ListarHorasxAlumnoResult"] as String, context!!)
                        if (detalleJArray != null) {
                            if (detalleJArray.length() == 1) {
                                val detalleJObject = detalleJArray[0] as JSONObject

                                val grupo = detalleJObject["NomGrupo"] as String
                                val cantHorasAntici = detalleJObject["CantHorasAnticipa"] as Int
                                val cantHorasReserv = detalleJObject["CantHorasReserva"] as Int
                                val cantHorasRestan = cantHorasReserv - detalleJObject["CantHorasUtil"] as Int

                                ControlUsuario.instance.prconfiguracion = PRConfiguracion(idConfiguracion, grupo, cantHorasAntici, cantHorasReserv, cantHorasRestan)

                                val listaOpciones = ArrayList<MasOpcion>()
                                listaOpciones.add(MasOpcion(1, Intent(activity!!, PRReservaPrimeraActivity::class.java), context!!.resources.getString(R.string.reservar), context!!.resources.getString(R.string.sub_reservar), activity!!.resources.getDrawable( R.drawable.tab_clock)))
                                listaOpciones.add(MasOpcion(2, Intent(activity!!, PRConfirmarActivity::class.java), context!!.resources.getString(R.string.confirmar_reserva), context!!.resources.getString(R.string.sub_confirmar_reserva), activity!!.resources.getDrawable( R.drawable.tab_check)))
                                listaOpciones.add(MasOpcion(3, Intent(activity!!, PRMisReservasActivity::class.java), context!!.resources.getString(R.string.mis_reservas), context!!.resources.getString(R.string.sub_mis_reservas), activity!!.resources.getDrawable( R.drawable.tab_note)))
                                //listaOpciones.add(MasOpcion(3, Intent(), context!!.resources.getString(R.string.mis_reservas), context!!.resources.getString(R.string.sub_mis_reservas), ContextCompat.getDrawable(activity!!, R.drawable.tab_note)))
                                //listaOpciones.add(MasOpcion(4, Intent(), context!!.resources.getString(R.string.disponibilidad_pr), context!!.resources.getString(R.string.sub_disponibilidad_pr), ContextCompat.getDrawable(activity!!, R.drawable.tab_grid)))

                                val puntosAdapter = PuntosReunionOpcionAdapter(listaOpciones) { masOpcion ->
                                    masOpcion.intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    activity!!.startActivity(masOpcion.intent)
                                }

                                rvOpciones_fpuntosreunion.adapter = puntosAdapter
                                rvOpciones_fpuntosreunion.visibility = View.VISIBLE
                            } else {
                                lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                                lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.advertencia_pr_notienegrupo)
                            }
                        } else {
                            lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                            lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_desencriptar)
                        }
                    } else {
                        lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                        lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.advertencia_pr_notienegrupo)
                    }
                } catch (jex: JSONException) {
                    lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                    lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_no_conexion)
                }
                prbCargando_fpuntosreunion.visibility = View.GONE
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {

                    requireActivity().renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onDetalleAlumno(url, idConfiguracion, request)
                        } else {
                            if(view != null) {
                                prbCargando_fpuntosreunion.visibility = View.GONE
                                lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                                lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_no_conexion)
                            }
                        }
                    }
                } else {
                    if(view != null) {
                        prbCargando_fpuntosreunion.visibility = View.GONE
                        lblMensaje_fpuntosreunion.visibility = View.VISIBLE
                        lblMensaje_fpuntosreunion.text = activity!!.resources.getString(R.string.error_no_conexion)
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
        jsObjectRequest.tag = TAG
        requestQueue3?.add(jsObjectRequest)
    }

    override fun onStop() {
        super.onStop()

        requestQueue?.cancelAll(TAG)
        requestQueue2?.cancelAll(TAG)
        requestQueue3?.cancelAll(TAG)
    }
}
