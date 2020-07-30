package pe.edu.esan.appostgrado.view.academico.pregrado


import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
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

        Log.w(LOG,"onCreate()")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        Log.w(LOG,"onCreateView()")

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
        Log.w(LOG, "ViewModel is: $controlViewModel")

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
        Log.w(LOG,"onResume()")
        super.onResume()
        if (ControlUsuario.instance.entroEncuesta) {
            ControlUsuario.instance.entroEncuesta = false
            showCursos()
        }
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.w(LOG,"onActivityCreated()")
        showCursos()
    }



    private fun showCursos() {
        Log.w(LOG,"showCursos()")
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForFragmentPublic.observe(this,
                Observer<Boolean> { value ->
                    if(value){
                        Log.w(LOG, "operationFinishedCursosPublic.observe() was called")
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

        Log.i(LOG, url)

        Log.i(LOG, request.toString())

        prbCargando_fcurso.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(activity)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                request,
                Response.Listener { response ->
                    try {

                        var cursosJArray: JSONArray? = null
                        if(activity != null) {
                            cursosJArray = Utilitarios.jsArrayDesencriptar(
                                response["ListarNotasActualesAlumnoPreResult"] as String,
                                activity!!.applicationContext
                            )
                        }
                        if (cursosJArray != null) {

                            Log.i(LOG, cursosJArray.toString())
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

                                    if(esResponsable == true){
                                        if (idSeccion == idSeccionActual) {
                                            val profesor = Profesor(
                                                idProfesor,
                                                nombreProfesor,
                                                estadoEncuesta,
                                                esResponsable,
                                                idEncuesta,
                                                idProgramacion, estadoAlumno)
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
                                        startActivity(intentEncuesta)
                                    } else {
                                        getAsistencias(curso.seccionCodigo, alumnoRetirado)

                                        /*
                                          val intentDetalleCurso = Intent(activity, CursoDetalleActivity::class.java)
                                          startActivity(intentDetalleCurso)
                                                                    */
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
                Response.ErrorListener { error ->

                    Log.e(LOG, error.message.toString())
                    if(view != null) {
                        view!!.swCurso_fcurso.isRefreshing = false
                        view!!.prbCargando_fcurso.visibility = View.GONE
                        view!!.lblMensaje_fcurso.visibility = View.VISIBLE
                        view!!.rvCurso_fcurso.visibility = View.GONE
                        view!!.lblMensaje_fcurso.text = context!!.resources.getText(R.string.error_default)
                    }
                }
        )
        jsObjectRequest.retryPolicy = DefaultRetryPolicy(1500, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }



    private fun getAsistencias(codigoSeccion: String, alumRetirado: Boolean) {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

            val request = JSONObject()
            request.put("CodAlumno", usuario.codigo)

            Log.i(LOG,request.toString())

            var requestEncriptado:JSONObject? = null

            if(activity != null) {
                requestEncriptado = Utilitarios.jsObjectEncrypted(request, activity!!)
            }
            if (requestEncriptado != null)
                onAsistencias(Utilitarios.getUrl(Utilitarios.URL.ASIS_ALUMNO_PRE), requestEncriptado, codigoSeccion, alumRetirado)
            else {
                /*lblMensaje_historiconotaspre.text = resources.getText(R.string.error_encriptar)
                lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                val intentDetalleCurso = Intent(activity, CursoDetalleActivity::class.java)
                startActivity(intentDetalleCurso)
            }
        } else {
            /*lblMensaje_historiconotaspre.text = resources.getText(R.string.error_ingreso)
            lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
            ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
            val intentDetalleCurso = Intent(activity, CursoDetalleActivity::class.java)
            startActivity(intentDetalleCurso)
        }
    }



    private fun onAsistencias(url: String, request: JSONObject, codSeccion: String, alumnoRetirado: Boolean) {

        Log.i(LOG,url)

        Log.i(LOG, request.toString())

        if(activity != null) {
            CustomDialog.instance.showDialogLoad(activity!!)
        }
        requestQueue = Volley.newRequestQueue(activity)
        val jsObjectRequest = JsonObjectRequest (
                url,
                request,
                Response.Listener { response ->
                    try {
                        var historicoJArray: JSONArray? = null
                        if(activity != null) {
                            historicoJArray = Utilitarios.jsArrayDesencriptar(
                                response["ResumenAsistenciaAlumnoPregradoResult"] as String,
                                activity!!.applicationContext
                            )
                        }

                        Log.i(LOG, historicoJArray.toString())
                        if (historicoJArray != null) {
                            if (historicoJArray.length() > 0) {

                                for (i in 0 until historicoJArray.length()) {
                                    val historicoJObject = historicoJArray[i] as JSONObject


                                    val CodigoSeccion = historicoJObject["SeccionCodigo"] as String

                                    Log.i(LOG, codSeccion)
                                    if (codSeccion == CodigoSeccion) {
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

                                /*rvCurso_historiconotaspre.adapter = HistoricoNotaPreAdapter(listaCursos)*/

                            } else {
                                /*lblMensaje_historiconotaspre.text = resources.getText(R.string.advertencia_no_informacion)
                                lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                                ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                            }
                        } else {
                            /*lblMensaje_historiconotaspre.text = resources.getText(R.string.error_desencriptar)
                            lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                            ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                        }
                    } catch (jex: JSONException) {

                        Log.e(LOG, jex.message)
                        /*lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                        lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                        ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                    } catch (ccax: ClassCastException) {

                        Log.e(LOG, ccax.message)
                        /*lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                        lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                        ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                    }
                    /*prbCargando_historiconotaspre.visibility = View.GONE*/
                    CustomDialog.instance.dialogoCargando?.dismiss()
                    if(activity != null) {
                        val intentDetalleCurso = Intent(activity, CursoDetalleActivity::class.java)
                        if (alumnoRetirado) {
                            intentDetalleCurso.putExtra("alumno_esta_retirado", true)
                        } else {
                            intentDetalleCurso.putExtra("alumno_esta_retirado", false)
                        }
                        startActivity(intentDetalleCurso)
                    }
                },
                Response.ErrorListener { error ->
                    Log.e(LOG, error.message.toString())
                    /*prbCargando_historiconotaspre.visibility = View.GONE
                    lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                    lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                    if(ControlUsuario.instance.currentCursoPre != null){
                        ControlUsuario.instance.currentCursoPre!!.errorasistencias = true
                    }

                    if(CustomDialog.instance.dialogoCargando != null){
                        CustomDialog.instance.dialogoCargando?.dismiss()
                    }

                    if(activity != null){
                        val intentDetalleCurso = Intent(activity, CursoDetalleActivity::class.java)
                        startActivity(intentDetalleCurso)
                    }

                }
        )
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
        Log.w(LOG, "Refresh Data")
        swCurso_fcurso.isRefreshing = true
        prbCargando_fcurso.visibility = View.GONE
        lblMensaje_fcurso.visibility = View.VISIBLE
        showCursos()
    }



    override fun onStop() {
        Log.w(LOG,"onStop()")
        super.onStop()
        requestQueue?.cancelAll(TAG)
    }

    override fun onDestroy() {
        Log.w(LOG,"onDestroy()")
        if(CustomDialog.instance.dialogoCargando != null){
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
        super.onDestroy()
    }



    override fun onPause() {
        Log.w(LOG,"onPause()")
        super.onPause()
    }
}// Required empty public constructor
