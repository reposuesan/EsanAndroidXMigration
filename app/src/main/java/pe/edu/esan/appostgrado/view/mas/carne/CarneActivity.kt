package pe.edu.esan.appostgrado.view.mas.carne

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import kotlinx.android.synthetic.main.activity_carne.*

import kotlinx.android.synthetic.main.toolbar_titulo.view.toolbar_title
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario

import pe.edu.esan.appostgrado.entidades.UserEsan
import pe.edu.esan.appostgrado.util.Utilitarios
import java.text.SimpleDateFormat
import java.util.*

class CarneActivity : AppCompatActivity() {

    private val TAG = "CarneActivity"

    private val LOG = CarneActivity::class.simpleName

    private var timer: Timer? = Timer()
    private lateinit var toolbar: Toolbar

    private var requestQueue: RequestQueue? = null

    private var contadorCursosRetirados = 0

    private var userIsOut = false

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carne)

        toolbar = main_acarnealumno as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.carne_virtual_title)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        progress_bar_carnet.visibility = View.VISIBLE
        empty_text_view_carnet.visibility = View.GONE
        alumno_foto_reloj.visibility = View.GONE
        alumno_data_container.visibility = View.GONE
        alumno_barcode.visibility = View.GONE

        userIsOut = false

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        val extras = intent.extras
        val carrera = extras!!["KEY_CARRERA"] as String

        if(carrera.equals("Sin Carrera") || carrera.isEmpty()){
            mostrarCarnet()
        } else {
            toolbar.toolbar_title.text = resources.getString(R.string.carne_virtual_title) + " - " + resources.getString(R.string.pregrado_titulo)
            validarAlumnoMatriculado()
        }

    }

    private fun validarAlumnoMatriculado(){

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

            progress_bar_carnet.visibility = View.VISIBLE
            empty_text_view_carnet.visibility = View.GONE
            alumno_foto_reloj.visibility = View.GONE
            alumno_data_container.visibility = View.GONE
            alumno_barcode.visibility = View.GONE
        }

    }

    private fun sendRequest(){

        val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

        val request = JSONObject()

        request.put("CodAlumno", usuario.codigo)

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)

        if (requestEncriptado != null) {
            consultarMatriculaAlumno(Utilitarios.getUrl(Utilitarios.URL.CURSOS_PRE), requestEncriptado)
        } else {
            empty_text_view_carnet.text = resources.getText(R.string.error_encriptar)
            ocultarCarnet()
        }
    }

    private fun consultarMatriculaAlumno(url: String, request: JSONObject){

        requestQueue = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest (
            url,
            request,
            { response ->

                try {
                    val cursosJArray = Utilitarios.jsArrayDesencriptar(response["ListarNotasActualesAlumnoPreResult"] as String, applicationContext)
                    if (cursosJArray != null) {
                        if (cursosJArray.length() > 0) {

                            for (curso in 0 until cursosJArray.length()) {
                                val cursoJson = cursosJArray[curso] as JSONObject

                                val estadoAlumno = cursoJson["Estado"] as String

                                if(estadoAlumno.equals("R")){
                                    contadorCursosRetirados++
                                }
                            }

                            if(contadorCursosRetirados == cursosJArray.length()){
                                empty_text_view_carnet.text = getString(R.string.mensaje_alumno_retirado_carnet)
                                ocultarCarnet()
                            } else {
                                mostrarCarnet()
                            }
                        } else {
                            ocultarCarnet()
                        }
                    } else {
                        empty_text_view_carnet.text = resources.getText(R.string.error_desencriptar)
                        ocultarCarnet()
                    }
                } catch (jex: JSONException) {
                    empty_text_view_carnet.text = resources.getText(R.string.error_respuesta_server)
                    ocultarCarnet()
                } catch (ccax: ClassCastException) {
                    empty_text_view_carnet.text = resources.getText(R.string.error_respuesta_server)
                    ocultarCarnet()
                }
            },

            { error ->
                empty_text_view_carnet.text = resources.getText(R.string.error_respuesta_server)
                ocultarCarnet()
            }
        )
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)

    }

    private fun mostrarCarnet(){

        progress_bar_carnet.visibility = View.GONE
        empty_text_view_carnet.visibility = View.GONE
        alumno_foto_reloj.visibility = View.VISIBLE
        alumno_data_container.visibility = View.VISIBLE
        alumno_barcode.visibility = View.VISIBLE

        val extras = intent.extras

        if (extras != null) {
            tv_nombre_carne_alumno.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            tv_apellido_carne_alumno.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            tv_carrera_alumno.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            tv_nombre_carne_alumno_contenido.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            tv_apellido_carne_alumno_contenido.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
            tv_carrera_alumno_contenido.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)

            tv_nombre_carne_alumno_contenido.text = extras["KEY_NOMBRE"] as String
            tv_apellido_carne_alumno_contenido.text = extras["KEY_APELLIDO"] as String

            val carrera = extras["KEY_CARRERA"] as String
            if(carrera.equals("Sin Carrera") || carrera.isEmpty()){
                tv_carrera_alumno_contenido.visibility = View.GONE
                tv_carrera_alumno.visibility = View.GONE
                toolbar.toolbar_title.text = resources.getString(R.string.carne_virtual_title) + " - " + resources.getString(R.string.posgrado_titulo)
            } else {
                tv_carrera_alumno_contenido.text = extras["KEY_CARRERA"] as String
                tv_carrera_alumno_contenido.visibility = View.VISIBLE
                tv_carrera_alumno.visibility = View.VISIBLE
                toolbar.toolbar_title.text = resources.getString(R.string.carne_virtual_title) + " - " + resources.getString(R.string.pregrado_titulo)
            }

            if(!userIsOut){
                Glide.with(this)
                    .load(Utilitarios.getUrlFoto(extras["KEY_CODIGO"] as String, 140))
                    .into(imgUsuario_carnealumno)
            }

            if(!userIsOut){
                Glide.with(this)
                    .load(Utilitarios.getCodeBarUrl(extras["KEY_CODIGO"] as String))
                    .apply(RequestOptions.timeoutOf(20 * 60 * 1000))
                    .into(imgCodeBar_carnealumno)
            }

            /*Glide.with(this)
                .load("https://bwipjs-api.metafloor.com/?bcid=code128&text=13200177&backgroundcolor=FFFFFF&textcolor=CF1111&barcolor=CF1111&scaleX=16&scaleY=4&includetext")
                .into(imgCodeBar_carnealumno)*/

        }

        timer?.schedule(object: TimerTask(){
            override fun run() {
                val sdf = SimpleDateFormat("hh:mm:ss a")
                val currentDateandTime = sdf.format(Date())

                runOnUiThread {
                    tv_reloj_carne_alumno.text = currentDateandTime
                }

            }
        },100L,1000L)
    }

    private fun ocultarCarnet(){
        progress_bar_carnet.visibility = View.GONE
        empty_text_view_carnet.visibility = View.VISIBLE
        alumno_foto_reloj.visibility = View.GONE
        alumno_data_container.visibility = View.GONE
        alumno_barcode.visibility = View.GONE
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
        controlViewModel.insertDataToRoom()
    }

    override fun onDestroy() {
        timer?.cancel()
        timer = null
        userIsOut = true
        super.onDestroy()
    }
}
