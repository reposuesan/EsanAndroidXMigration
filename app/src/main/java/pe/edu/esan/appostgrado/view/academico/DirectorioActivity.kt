package pe.edu.esan.appostgrado.view.academico

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_directorio.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.ContactoAdapter
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios

class DirectorioActivity : AppCompatActivity() {

    private val TAG = "DirectorioActivity"
    private var requestQueue: RequestQueue? = null
    private var CodigoSeccion = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directorio)

        val toolbar = main_directorio as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.directorio)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        lblMensaje_directorio.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
        rvAlumno_directorio.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvAlumno_directorio.adapter = null

        if (savedInstanceState == null) {
            val extras = intent.extras
            CodigoSeccion = if (extras != null) extras.getString("CodSeccion") else ""
        } else {
            CodigoSeccion = savedInstanceState.getString("CodSeccion")
        }

        getAlumnos(CodigoSeccion)
    }

    private fun getAlumnos(seccion: String) {
        val request = JSONObject()
        request.put("codigoSeccion", seccion)

        onAlumnos(Utilitarios.getUrl(Utilitarios.URL.DIRECTORIO), request)
    }

    private fun onAlumnos(url: String, request: JSONObject) {
        println(url)
        println(request)
        prbCargando_directorio.visibility = View.VISIBLE
        requestQueue = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest (
                url,
                request,
                Response.Listener { response ->
                    println(response.toString())
                    prbCargando_directorio.visibility = View.GONE
                    try {
                        val directorioJArray = response["DirectorioAlumnosResult"] as JSONArray
                        val listaAlumnos = ArrayList<Alumno>()
                        for (i in 0 until directorioJArray.length()) {
                            val directorioJObject = directorioJArray[i] as JSONObject
                            val codigo = directorioJObject["codigo"] as String
                            var email = directorioJObject["email"] as String
                            val estadonombre = directorioJObject["estadonombre"] as String
                            val nombrecompleto = directorioJObject["nombrecompleto"] as String
                            val idactor = directorioJObject["idactor"] as Int
                            if (!(email.contains("@esan.edu.pe") || email.contains("@ue.edu.pe"))) {
                                email = ""
                            }
                            if (estadonombre == "Activo") {
                                listaAlumnos.add(Alumno(codigo, idactor, nombrecompleto, email))
                            }
                        }

                        if (listaAlumnos.isNotEmpty()) {
                            rvAlumno_directorio.adapter = ContactoAdapter(listaAlumnos)
                        } else {
                            lblMensaje_directorio.visibility = View.VISIBLE
                            lblMensaje_directorio.text = resources.getString(R.string.info_noalumnosmatriculados)
                        }
                    } catch (jex: JSONException) {
                        lblMensaje_directorio.visibility = View.VISIBLE
                        lblMensaje_directorio.text = resources.getString(R.string.error_no_conexion)
                    } catch (ccax : ClassCastException) {
                        lblMensaje_directorio.visibility = View.VISIBLE
                        lblMensaje_directorio.text = resources.getString(R.string.error_no_conexion)
                    }
                },
                Response.ErrorListener { error ->
                    println(error.toString())
                    prbCargando_directorio.visibility = View.GONE
                    lblMensaje_directorio.visibility = View.VISIBLE
                    lblMensaje_directorio.text = resources.getString(R.string.error_no_conexion)
                }
        )
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("CodSeccion", CodigoSeccion)
        super.onSaveInstanceState(outState)
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
    }
}
