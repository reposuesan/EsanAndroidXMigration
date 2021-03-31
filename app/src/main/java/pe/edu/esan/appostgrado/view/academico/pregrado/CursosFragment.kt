package pe.edu.esan.appostgrado.view.academico.pregrado


import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_cursos.*
import kotlinx.android.synthetic.main.fragment_cursos.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.CursoPreAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.CursosPre
import pe.edu.esan.appostgrado.entidades.NotasPre
import pe.edu.esan.appostgrado.entidades.Profesor
import pe.edu.esan.appostgrado.entidades.UserEsan
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken
import pe.edu.esan.appostgrado.view.academico.EncuestaActivity
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass.
 */
class CursosFragment : androidx.fragment.app.Fragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    private val TAG = "CursosFragment"

    private var requestQueue : RequestQueue? = null

    private val LOG = CursosFragment::class.simpleName

    /*private var estadoAlumno: String = ""*/

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view = inflater.inflate(R.layout.fragment_cursos, container, false)
        } else{
            view = inflater.inflate(R.layout.fragment_cursos_old_version, container, false)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        controlViewModel = activity?.run {
            ViewModelProviders.of(this)[ControlViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        view.swCurso_fcurso.setOnRefreshListener(this)
        view.swCurso_fcurso.setColorSchemeResources(
            R.color.s1,
            R.color.s2,
            R.color.s3,
            R.color.s4
        )


        view.rvCurso_fcurso.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(activity)
        view.rvCurso_fcurso.adapter = null
        if(activity != null) {
            view.lblMensaje_fcurso.typeface =
                Utilitarios.getFontRoboto(activity!!, Utilitarios.TypeFont.THIN)
        }


        view.lblMensaje_fcurso.setOnClickListener {
            showCursos()
        }
    }



    override fun onResume() {
        super.onResume()
        if (ControlUsuario.instance.entroEncuesta) {
            ControlUsuario.instance.entroEncuesta = false
            showCursos()
        }
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showCursos()
    }



    private fun showCursos() {
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

        var requestEncriptar: JSONObject? = null

        if(activity != null) {
            requestEncriptar = Utilitarios.jsObjectEncrypted(request, activity!!)
        }
        if (requestEncriptar != null)
            onCursos(Utilitarios.getUrl(Utilitarios.URL.CURSOS_PRE), requestEncriptar)
        else {
            lblMensaje_fcurso.visibility = View.VISIBLE
            lblMensaje_fcurso.text = resources.getString(R.string.error_encriptar)
        }
    }



    private fun onCursos(url: String, request: JSONObject) {

        prbCargando_fcurso.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(activity)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                request,
            { response ->
                try {

                    var cursosJArray: JSONArray? = null
                    if(activity != null) {
                        cursosJArray = Utilitarios.jsArrayDesencriptar(
                            response["ListarNotasActualesAlumnoPreResult"] as String,
                            activity!!.applicationContext
                        )
                    }
                    if (cursosJArray != null) {

                        if (cursosJArray.length() > 0) {

                            val listCursosPre = ArrayList<CursosPre>()
                            val listEstado = ArrayList<String>()

                            var listaProfesores = ArrayList<Profesor>()
                            var listaNotas = ArrayList<NotasPre>()
                            var idSeccionActual = 0
                            var cursoActual = CursosPre()

                            for (f in 0 until cursosJArray.length()) {
                                val cursoJson = cursosJArray[f] as JSONObject
                                val idSeccion = cursoJson["IdSeccion"] as Int
                                val seccionCodigo = cursoJson["SeccionCodigo"] as String
                                val idProesoMatricula = cursoJson["IdProcesoMatricula"] as Int
                                val nombreCurso = cursoJson["Curso"] as String
                                val idProfesor = cursoJson["IdProfesor"] as Int
                                val nombreProfesor = cursoJson["NombreProfesor"] as String
                                val esResponsable = cursoJson["EsResponsable"] as Boolean
                                val estadoEncuesta = cursoJson["EstadoEncuesta"] as String

                                val nota = cursoJson["Nota"] as? String
                                val tipoNota = cursoJson["TipoNota"] as? String
                                val notaCodigo = cursoJson["NotaCodigo"] as? String
                                val notaPeso = cursoJson["NotaPeso"] as? Double

                                val idEncuesta = cursoJson["IdEncuesta"] as Int
                                val idProgramacion = cursoJson["IdProgramacion"] as Int
                                val estadoAlumno = cursoJson["Estado"] as String
                                /*estadoAlumno = cursoJson["Estado"] as String*/

                                if(esResponsable){
                                    if (idSeccion == idSeccionActual) {
                                        val profesor = Profesor(
                                            idProfesor,
                                            nombreProfesor,
                                            estadoEncuesta,
                                            esResponsable,
                                            idEncuesta,
                                            idProgramacion,
                                            estadoAlumno)
                                        if (!findProfesor(listaProfesores, idProfesor)) {
                                            listaProfesores.add(profesor)
                                        }

                                        if (nota != null && tipoNota != null && notaCodigo != null && notaPeso != null) {
                                            if (!findNotaPre(listaNotas, tipoNota)) {
                                                if (notaCodigo == "PG") {
                                                    cursoActual.cambiaPromedio(nota)
                                                } else {
                                                    if (notaPeso.toInt() > 0) {
                                                        listaNotas.add(
                                                            NotasPre(
                                                                tipoNota,
                                                                notaCodigo,
                                                                nota,
                                                                notaPeso.toString()
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {

                                        if (idSeccionActual != 0) {
                                            cursoActual.listProfesores = listaProfesores

                                            if (listaNotas.size > 0)
                                                cursoActual.listNotasPre = listaNotas
                                            else
                                                cursoActual.fillCursoDefault(context!!)

                                            listCursosPre.add(cursoActual)
                                        }

                                        idSeccionActual = idSeccion
                                        cursoActual =
                                                CursosPre(idSeccion, seccionCodigo, idProesoMatricula, nombreCurso)
                                        listaProfesores = ArrayList()
                                        listaProfesores.add(
                                            Profesor(
                                                idProfesor,
                                                nombreProfesor,
                                                estadoEncuesta,
                                                esResponsable,
                                                idEncuesta,
                                                idProgramacion, estadoAlumno)
                                        )

                                        listaNotas = ArrayList()
                                        if (nota != null && tipoNota != null && notaCodigo != null && notaPeso != null) {
                                            if (notaCodigo == "PG") {
                                                cursoActual.cambiaPromedio(nota)
                                            } else {
                                                if (notaPeso.toInt() > 0) {
                                                    listaNotas.add(
                                                        NotasPre(
                                                            tipoNota,
                                                            notaCodigo,
                                                            nota,
                                                            notaPeso.toString()
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            cursoActual.listProfesores = listaProfesores
                            if (listaNotas.size > 0)
                                cursoActual.listNotasPre = listaNotas
                            else
                                cursoActual.fillCursoDefault(context!!)

                            listCursosPre.add(cursoActual)



                            val adapter = CursoPreAdapter(listCursosPre) { curso ->
                                var encuestar = false
                                var alumnoRetirado = true
                                val lstProfesores = ArrayList<Profesor>()
                                for (profesor in curso.listProfesores ){
                                    if (profesor.encuesta.trim().toUpperCase() == "N" && profesor.estadoAlumno == "A") {
                                        encuestar = true
                                        lstProfesores.add(profesor)
                                    }
                                    if(profesor.estadoAlumno == "A"){
                                        alumnoRetirado = false
                                    }
                                }

                                ControlUsuario.instance.currentCursoPre = curso
                                if (encuestar) {
                                    curso.listProfesores = lstProfesores
                                    val intentEncuesta = Intent(activity, EncuestaActivity::class.java)
                                    intentEncuesta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intentEncuesta)
                                } else {
                                    getAsistencias(curso.seccionCodigo, alumnoRetirado)
                                }
                            }

                            if(view != null) {
                                view!!.rvCurso_fcurso.adapter = adapter
                                view!!.lblMensaje_fcurso.visibility = View.GONE
                            }
                        } else {
                            if(view != null) {
                                view!!.lblMensaje_fcurso.visibility = View.VISIBLE
                                view!!.lblMensaje_fcurso.text = context!!.resources.getText(R.string.error_curso_no)
                            }
                        }
                    } else {
                        if(view != null) {
                            view!!.lblMensaje_fcurso.visibility = View.VISIBLE
                            view!!.lblMensaje_fcurso.text = context!!.resources.getText(R.string.error_desencriptar)
                        }
                    }
                } catch (jex: JSONException) {
                    if(view != null) {
                        view!!.lblMensaje_fcurso.visibility = View.VISIBLE
                        view!!.lblMensaje_fcurso.text = context!!.resources.getText(R.string.error_default)
                    }
                }
                if(view != null) {
                    view!!.swCurso_fcurso.isRefreshing = false
                    view!!.prbCargando_fcurso.visibility = View.GONE
                }
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {

                    requireActivity().renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onCursos(url, request)
                        } else {
                            if(view != null) {
                                view!!.swCurso_fcurso.isRefreshing = false
                                view!!.prbCargando_fcurso.visibility = View.GONE
                                view!!.lblMensaje_fcurso.visibility = View.VISIBLE
                                view!!.rvCurso_fcurso.visibility = View.GONE
                                view!!.lblMensaje_fcurso.text = context!!.resources.getText(R.string.error_default)
                            }
                        }
                    }
                } else {
                    if(view != null) {
                        view!!.swCurso_fcurso.isRefreshing = false
                        view!!.prbCargando_fcurso.visibility = View.GONE
                        view!!.lblMensaje_fcurso.visibility = View.VISIBLE
                        view!!.rvCurso_fcurso.visibility = View.GONE
                        view!!.lblMensaje_fcurso.text = context!!.resources.getText(R.string.error_default)
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
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }



    private fun getAsistencias(codigoSeccion: String, alumRetirado: Boolean) {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

            val request = JSONObject()
            request.put("CodAlumno", usuario.codigo)

            var requestEncriptado:JSONObject? = null

            if(activity != null) {
                requestEncriptado = Utilitarios.jsObjectEncrypted(request, activity!!)
            }
            if (requestEncriptado != null)
                onAsistencias(Utilitarios.getUrl(Utilitarios.URL.ASIS_ALUMNO_PRE), requestEncriptado, codigoSeccion, alumRetirado)
            else {
                ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                val intentDetalleCurso = Intent(activity, CursoDetalleActivity::class.java)
                intentDetalleCurso.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intentDetalleCurso)
            }
        } else {
            ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
            val intentDetalleCurso = Intent(activity, CursoDetalleActivity::class.java)
            intentDetalleCurso.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intentDetalleCurso)
        }
    }



    private fun onAsistencias(url: String, request: JSONObject, codSeccion: String, alumnoRetirado: Boolean) {

        if(activity != null) {
            CustomDialog.instance.showDialogLoad(activity!!)
        }
        requestQueue = Volley.newRequestQueue(activity)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->
                try {
                    var historicoJArray: JSONArray? = null
                    if(activity != null) {
                        historicoJArray = Utilitarios.jsArrayDesencriptar(
                            response["ResumenAsistenciaAlumnoPregradoResult"] as String,
                            activity!!.applicationContext
                        )
                    }

                    if (historicoJArray != null) {
                        if (historicoJArray.length() > 0) {

                            for (i in 0 until historicoJArray.length()) {
                                val historicoJObject = historicoJArray[i] as JSONObject


                                val codigoSeccion = historicoJObject["SeccionCodigo"] as String

                                if (codSeccion == codigoSeccion) {
                                    val tardanza = historicoJObject["CantTardanzas"] as Double
                                    val asistencia = historicoJObject["CantAsistencias"] as Double
                                    val faltas = historicoJObject["CantFalta"] as Double
                                    val sesiones = historicoJObject["CantidadSesiones"] as Int

                                    ControlUsuario.instance.currentCursoPre!!.asistencias = asistencia.roundToInt()
                                    ControlUsuario.instance.currentCursoPre!!.tardanzas = tardanza.roundToInt()
                                    ControlUsuario.instance.currentCursoPre!!.faltas = faltas.roundToInt()
                                    ControlUsuario.instance.currentCursoPre!!.totalsesiones = sesiones

                                    break
                                }
                            }
                        } else {
                            ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                        }
                    } else {
                        ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                    }
                } catch (jex: JSONException) {
                    ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                } catch (ccax: ClassCastException) {
                    ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                }

                CustomDialog.instance.dialogoCargando?.dismiss()
                if(activity != null) {
                    val intentDetalleCurso = Intent(activity, CursoDetalleActivity::class.java)
                    if (alumnoRetirado) {
                        intentDetalleCurso.putExtra("alumno_esta_retirado", true)
                    } else {
                        intentDetalleCurso.putExtra("alumno_esta_retirado", false)
                    }
                    intentDetalleCurso.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intentDetalleCurso)
                }
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {

                    requireActivity().renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onAsistencias(url, request, codSeccion, alumnoRetirado)
                        } else {
                            if(view != null) {
                                if(ControlUsuario.instance.currentCursoPre != null){
                                    ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                                }

                                if(CustomDialog.instance.dialogoCargando != null){
                                    CustomDialog.instance.dialogoCargando?.dismiss()
                                }

                                if(activity != null){
                                    val intentDetalleCurso = Intent(activity, CursoDetalleActivity::class.java)
                                    intentDetalleCurso.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intentDetalleCurso)
                                }
                            }
                        }
                    }
                } else {
                    if(view != null) {
                        if(ControlUsuario.instance.currentCursoPre != null){
                            ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                        }

                        if(CustomDialog.instance.dialogoCargando != null){
                            CustomDialog.instance.dialogoCargando?.dismiss()
                        }

                        if(activity != null){
                            val intentDetalleCurso = Intent(activity, CursoDetalleActivity::class.java)
                            intentDetalleCurso.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intentDetalleCurso)
                        }
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



    private fun findProfesor (listProfesor: ArrayList<Profesor>, idProfesor: Int): Boolean{
        for (profesor in listProfesor) {
            if (profesor.idActor == idProfesor)
                return true
        }
        return false
    }



    private fun findNotaPre (listaNotas: ArrayList<NotasPre>, tipoNota: String): Boolean {
        for (notaPre in listaNotas) {
            if (notaPre.tipoCodigo == tipoNota)
                return true
        }
        return false
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_histonotas -> {
                val historicoNotas = Intent(activity, HistoricoNotasPreActivity::class.java)
                historicoNotas.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(historicoNotas)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_histonotas)?.isVisible = true
        super.onPrepareOptionsMenu(menu)
    }



    override fun onRefresh() {
        swCurso_fcurso.isRefreshing = true
        prbCargando_fcurso.visibility = View.GONE
        lblMensaje_fcurso.visibility = View.VISIBLE
        showCursos()
    }



    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
    }

    override fun onDestroy() {
        if(CustomDialog.instance.dialogoCargando != null){
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }
}
