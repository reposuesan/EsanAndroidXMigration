package pe.edu.esan.appostgrado.view.mas.curricula

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.fragment_cursos.view.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.MallaPreAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.control.CustomDialog
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.CursoRequisito
import pe.edu.esan.appostgrado.entidades.CursosPre
import pe.edu.esan.appostgrado.entidades.MallaPre
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class MallaCurricularActivity : AppCompatActivity() {

    private val TAG = "MallaCurricularActivity"
    private var requestQueue : RequestQueue? = null

    private val LOG = MallaCurricularActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_malla_curricular)

        val toolbar = main_mallacurricular as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.malla_curricular)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)
        lblMensaje_mallacurricular.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        getMallaCurricular()
    }

    private fun getMallaCurricular() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            sendRequest()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        sendRequest()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
        }
    }

    private fun sendRequest(){
        val user = ControlUsuario.instance.currentUsuario[0]
        when (user) {
            is Alumno -> {
                val request = JSONObject()
                request.put("CodAlumno", user.codigo)

                val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                if (requestEncriptado != null) {
                    onMallaCurricular(Utilitarios.getUrl(Utilitarios.URL.MALLA_CURRICULAR), requestEncriptado)
                } else {
                    lblMensaje_mallacurricular.visibility = View.VISIBLE
                    lblMensaje_mallacurricular.text = resources.getString(R.string.error_encriptar)
                }
            }
        }
    }

    /*private fun onHistoricoResumen(url: String, request: JSONObject) {
        CustomDialog.instance.showDialogLoad(this)
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->

                CustomDialog.instance.dialogoCargando?.dismiss()
                try {
                    val historicoJArray = Utilitarios.jsObjectDesencriptar(response["ListarResumenAcademicoAlumnoPreResult"] as String, this)

                    if (historicoJArray != null) {
                        val creditosAprobados = historicoJArray["CreditosAprobados"] as Int
                        val cursosAprobados = historicoJArray["CursosAprobados"] as Int
                        val creditosPendientes = historicoJArray["CreditosPendientes"] as Int
                        val cursosPendientes = historicoJArray["CursosPendientes"] as Int
                    }

                } catch (jex: JSONException) {

                } catch (ccax: ClassCastException) {

                }

            },
            { error ->
                if(error.networkResponse.statusCode == 401) {
                    renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onHistoricoResumen(url, request)
                        } else {
                            Log.e(LOG,"An error occurred")
                        }
                    }
                } else {
                    Log.e(LOG,"An error occurred")
                }
            }
        )
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        {
            override fun getHeaders(): MutableMap<String, String> {
                return getHeaderForJWT()
            }
        }
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }*/

    private fun onMallaCurricular(url: String, request: JSONObject) {
        prbCargando_mallacurricular.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest(*/
                url,
                request,
            { response ->
                prbCargando_mallacurricular.visibility = View.GONE
                try {
                    if (!response.isNull("ListarAvanceCurricularAlumnoPreResult")) {
                        val mallaCurricularJSArray = Utilitarios.jsObjectDesencriptar(response["ListarAvanceCurricularAlumnoPreResult"] as String, this)
                        if (mallaCurricularJSArray != null) {
                            val mallJArray = mallaCurricularJSArray["Malla"] as JSONArray
                            val electivoJArray = mallaCurricularJSArray["Electivo"] as JSONArray
                            val listaMalla = ArrayList<MallaPre>()

                            for (i in 0 until mallJArray.length()) {
                                val cursosJObject = mallJArray[i] as JSONObject
                                val modulo = cursosJObject["Modulo"] as String
                                val cursosJArray = cursosJObject["Cursos"] as JSONArray

                                val listaCurso = ArrayList<CursosPre>()

                                for (j in 0 until cursosJArray.length()) {
                                    val cursoJObject = cursosJArray[j] as JSONObject
                                    val ciclo = cursoJObject["Ciclo"] as? String ?: ""
                                    val creditos = cursoJObject["Creditos"] as? Int ?: 0
                                    val estado = cursoJObject["Estado"] as? String ?: ""
                                    val nombre = cursoJObject["Nombre"] as? String ?: ""
                                    val nota = cursoJObject["Nota"] as? String ?: ""
                                    val veces = cursoJObject["Veces"] as? Int ?: 0
                                    val esIngles = cursoJObject["Esingles"] as? Int ?: 0 != 0
                                    val requisitoJArray = cursoJObject["Cursosrequisitos"] as JSONArray
                                    val listaRequisitos = ArrayList<CursoRequisito>()
                                    for (cr in 0 until requisitoJArray.length()) {
                                        val requisitoJObject = requisitoJArray[cr] as JSONObject
                                        val cursorq = requisitoJObject["Nombre"] as? String ?: ""
                                        val estadorq = requisitoJObject["Estadoreq"] as? String ?: ""
                                        listaRequisitos.add(CursoRequisito(cursorq, estadorq))
                                    }
                                    listaCurso.add(CursosPre(nombre, nota, creditos, veces, ciclo, estado, "", esIngles, listaRequisitos))
                                }
                                listaMalla.add(MallaPre(String.format(resources.getString(R.string.ciclo_), modulo), listaCurso))
                            }
                            val listaCurso = ArrayList<CursosPre>()
                            for (k in 0 until electivoJArray.length()) {
                                val electivoJObject = electivoJArray[k] as JSONObject
                                val cursosJArray = electivoJObject["Cursos"] as JSONArray
                                for (l in 0 until cursosJArray.length()) {
                                    val cursoJObject = cursosJArray[l] as JSONObject
                                    val ciclo = cursoJObject["Ciclo"] as? String ?: ""
                                    val creditos = cursoJObject["Creditos"] as? Int ?: 0
                                    val estado = cursoJObject["Estado"] as? String ?: ""
                                    val nombre = cursoJObject["Nombre"] as? String ?: ""
                                    val nota = cursoJObject["Nota"] as? String ?: ""
                                    val tipo = cursoJObject["Tipo"] as? String ?: ""
                                    val veces = cursoJObject["Veces"] as? Int ?: 0
                                    val esIngles = cursoJObject["Esingles"] as? Int ?: 0 != 0
                                    val requisitoJArray = cursoJObject["Cursosrequisitos"] as JSONArray
                                    val listaRequisitos = ArrayList<CursoRequisito>()
                                    for (cr in 0 until requisitoJArray.length()) {
                                        val requisitoJObject = requisitoJArray[cr] as JSONObject
                                        val cursorq = requisitoJObject["Nombre"] as? String ?: ""
                                        val estadorq = requisitoJObject["Estadoreq"] as? String ?: ""
                                        listaRequisitos.add(CursoRequisito(cursorq, estadorq))
                                    }
                                    listaCurso.add(CursosPre(nombre, nota, creditos, veces, ciclo, estado, tipo, esIngles, listaRequisitos))
                                }
                            }
                            listaMalla.add(MallaPre(resources.getString(R.string.electivos), listaCurso))

                            val mallaAdapter = MallaPreAdapter(listaMalla) { cursosPre ->
                                var mensaje = ""
                                for (curso in cursosPre.listaCursoRequisito) {
                                    mensaje += "- " + curso.nombre + " (" + curso.estado + ")\n"
                                }

                                val cursosRequisitos = AlertDialog.Builder(this)
                                    .setTitle(cursosPre.nombreCurso + ", cursos requisitos:")
                                    .setMessage(mensaje)
                                    .create()

                                cursosRequisitos.show()
                                //cursosRequisitos.getButton(android.app.Dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.esan_rojo))
                                cursosRequisitos.findViewById<TextView>(android.R.id.message)?.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
                            }
                            rvCursos_mallacurricular.layoutManager =
                                androidx.recyclerview.widget.LinearLayoutManager(
                                    this
                                )
                            rvCursos_mallacurricular.adapter = mallaAdapter
                        } else {
                            lblMensaje_mallacurricular.visibility = View.VISIBLE
                            lblMensaje_mallacurricular.text = resources.getText(R.string.error_desencriptar)
                        }
                    } else {
                        lblMensaje_mallacurricular.visibility = View.VISIBLE
                        lblMensaje_mallacurricular.text = resources.getText(R.string.error_respuesta_server)
                    }
                } catch (jex: JSONException) {
                    lblMensaje_mallacurricular.visibility = View.VISIBLE
                    lblMensaje_mallacurricular.text = resources.getText(R.string.error_respuesta_server)
                } catch (ccx: ClassCastException) {
                    lblMensaje_mallacurricular.visibility = View.VISIBLE
                    lblMensaje_mallacurricular.text = resources.getText(R.string.error_respuesta_server)
                }
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {
                    renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onMallaCurricular(url, request)
                        } else {
                            prbCargando_mallacurricular.visibility = View.GONE
                            lblMensaje_mallacurricular.visibility = View.VISIBLE
                            lblMensaje_mallacurricular.text = resources.getString(R.string.error_no_conexion)
                        }
                    }
                } else {
                    prbCargando_mallacurricular.visibility = View.GONE
                    lblMensaje_mallacurricular.visibility = View.VISIBLE
                    lblMensaje_mallacurricular.text = resources.getString(R.string.error_no_conexion)
                }
            }
        )
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        {
            override fun getHeaders(): MutableMap<String, String> {
                return getHeaderForJWT()
            }
        }

        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }

    /*private fun getResumenMalla() {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val user = ControlUsuario.instance.currentUsuario[0]
            when (user) {
                is Alumno -> {
                    val request = JSONObject()
                    request.put("CodAlumno", user.codigo)
                    val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
                    if (requestEncriptado != null) {
                        onHistoricoResumen(Utilitarios.getUrl(Utilitarios.URL.MALLA_CURRICULAR_RESUMEN), requestEncriptado)
                    } else {
                        /*lblMensaje_mallacurricular.visibility = View.VISIBLE
                        lblMensaje_mallacurricular.text = resources.getString(R.string.error_encriptar)*/
                    }
                }
            }
        } else {
            /*lblMensaje_mallacurricular.visibility = View.VISIBLE
            lblMensaje_mallacurricular.text = resources.getString(R.string.error_ingreso)*/
        }
    }*/

    /*** COMENTADO Próxima Versión DETALLE
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_mallacurricular, menu)
        return true
    }
    */

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            /*R.id.action_resumenmalla -> {
                getResumenMalla()
                return true
            }*/
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
    }

    override fun onDestroy() {
        if(CustomDialog.instance.dialogoCargando != null){
            CustomDialog.instance.dialogoCargando?.dismiss()
            CustomDialog.instance.dialogoCargando = null
        }
        super.onDestroy()
    }
}
