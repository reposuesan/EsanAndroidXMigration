package pe.edu.esan.appostgrado.view.academico.docente

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.Toolbar
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import kotlinx.android.synthetic.main.activity_seguimiento_alumno.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.SeguimientoAlumnoLinearAdapter
import pe.edu.esan.appostgrado.entidades.AlumnoShort
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken

class SeguimientoAlumnoActivity : AppCompatActivity() {

    private val TAG = "SeguimientoAlumnoActivity"
    private var requestQueue: RequestQueue? = null
    private var CodigoSeccion: String? = ""
    private var nombreCurso: String? = ""

    private var anchoPantalla = 0
    private var densidad = .0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguimiento_alumno)

        val toolbar = main_seguiminetoalumno as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.seguimiento_alumno)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        if (savedInstanceState == null) {
            val extras = intent.extras
            CodigoSeccion = if (extras != null) extras.getString("CodSeccion") else ""
            nombreCurso = if (extras != null) extras.getString("Curso") else ""
        } else {
            CodigoSeccion = savedInstanceState.getString("CodSeccion")
            nombreCurso = savedInstanceState.getString("Curso")
        }

        val displaymetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaymetrics)
        anchoPantalla = displaymetrics.widthPixels
        densidad = displaymetrics.density

        lblMensaje_seguimientoalumno.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
        lblSeccion_seguiminetoalumno.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
        lblCurso_seguimientoalumno.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)

        lblSeccion_seguiminetoalumno.text = CodigoSeccion
        lblCurso_seguimientoalumno.text = nombreCurso

        rvAlumno_seguimientoalumno.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvAlumno_seguimientoalumno.adapter = null

        getAlumnos(CodigoSeccion)
    }

    private fun getAlumnos(seccion: String?) {
        val request = JSONObject()
        request.put("codigoSeccion", seccion)

        val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)

        onAlumnosAsistencia(Utilitarios.getUrl(Utilitarios.URL.RECOR_ASISTENCIA_ALUMNO), requestEncriptado)
    }

    private fun onAlumnosAsistencia(url: String, request: JSONObject?) {
        prbCargando_seguimientoalumno.visibility = View.VISIBLE
        lblMensaje_seguimientoalumno.visibility = View.GONE

        requestQueue = Volley.newRequestQueue(this)
        //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
        val jsObjectRequest = object: JsonObjectRequest(
        /*val jsObjectRequest = JsonObjectRequest (*/
                url,
                request,
            { response ->
                prbCargando_seguimientoalumno.visibility = View.GONE
                try {

                    val alumnosJArray = Utilitarios.jsArrayDesencriptar(response["ListarRecordAsistenciaPorAlumnoResult"] as String, this)
                    if (alumnosJArray!!.length() > 0) {
                        val listaAlumnos = ArrayList<AlumnoShort>()
                        for (i in 0 until alumnosJArray.length()) {
                            val alumnosJObject = alumnosJArray[i] as JSONObject
                            val codigo = alumnosJObject["codigo"] as String
                            val nombre = alumnosJObject["Nombrecompleto"] as String
                            val cantAsis = alumnosJObject["asistencia"] as Double
                            val cantTard = alumnosJObject["tardanza"] as Double
                            val cantFalt = alumnosJObject["falta"] as Double
                            val total = alumnosJObject["TotalSesiones"] as Int

                            listaAlumnos.add(AlumnoShort(codigo, nombre, cantAsis.toInt(), cantTard.toInt(), cantFalt.toInt(), total))
                        }


                        lstAlumno_seguimientoalumno.adapter = SeguimientoAlumnoLinearAdapter(this, listaAlumnos,  anchoPantalla, densidad)
                    } else {
                        lblMensaje_seguimientoalumno.visibility = View.VISIBLE
                        lblMensaje_seguimientoalumno.text = resources.getString(R.string.info_noalumnosmatriculados)
                    }
                } catch (jex: JSONException) {
                    lblMensaje_seguimientoalumno.visibility = View.VISIBLE
                    lblMensaje_seguimientoalumno.text = resources.getString(R.string.error_no_conexion)
                } catch (ccax: ClassCastException) {
                    lblMensaje_seguimientoalumno.visibility = View.VISIBLE
                    lblMensaje_seguimientoalumno.text = resources.getString(R.string.error_no_conexion)
                }
            },
            { error ->
                if(error.networkResponse.statusCode == 401) {
                    renewToken { token ->
                        if(!token.isNullOrEmpty()){
                            onAlumnosAsistencia(url, request)
                        } else {
                            prbCargando_seguimientoalumno.visibility = View.GONE
                            lblMensaje_seguimientoalumno.visibility = View.VISIBLE
                            lblMensaje_seguimientoalumno.text = resources.getString(R.string.error_no_conexion)
                        }
                    }
                } else {
                    prbCargando_seguimientoalumno.visibility = View.GONE
                    lblMensaje_seguimientoalumno.visibility = View.VISIBLE
                    lblMensaje_seguimientoalumno.text = resources.getString(R.string.error_no_conexion)
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("CodSeccion", CodigoSeccion)
        outState.putString("Curso", nombreCurso)
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
    }
}
