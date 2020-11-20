package pe.edu.esan.appostgrado.view.mas.comedor

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import org.json.JSONArray
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.AlmuerzoAdapter
import pe.edu.esan.appostgrado.entidades.AlmuerzoComedor
import pe.edu.esan.appostgrado.util.Utilitarios
import java.util.*
import kotlin.collections.ArrayList

class MenuActivity : AppCompatActivity() {

    private val TAG = "MenuActivity"
    private var requestQueue: RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val toolbar = main_menu as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)

        val idComedor = intent.extras!!["IdComedor"] as String

        if (idComedor == "1") {
            toolbar.toolbar_title.text = resources.getString(R.string.charlotte)
        } else if (idComedor == "2") {
            toolbar.toolbar_title.text = resources.getString(R.string.delisabores)
        } else {
            toolbar.toolbar_title.text = getString(R.string.cafeteria_la_lupe)
        }

        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        rvAlumerzo_menu.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvAlumerzo_menu.adapter = null

        getMenu(Utilitarios.getUrl(Utilitarios.URL.MENU_COMEDOR) + idComedor)
    }

    private fun getMenu(url: String) {
        prbCargando_menu.visibility = View.VISIBLE
        lblMensaje_menu.visibility = View.GONE

        requestQueue = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
            { response ->
                prbCargando_menu.visibility = View.GONE

                val almuerzoJArray = response["MenuComedorResult"] as JSONArray
                if (almuerzoJArray.length() > 0) {
                    val listaAlmuerzo = ArrayList<AlmuerzoComedor>()
                    for (a in 0 until almuerzoJArray.length()) {
                        val almuerzoJObject = almuerzoJArray[a] as JSONObject
                        val descripcion = almuerzoJObject["descripcion"] as String
                        val dia = almuerzoJObject["dia"] as Int
                        val entrada = almuerzoJObject["entrada"] as String
                        val fecha = almuerzoJObject["fecha"] as String
                        val guarnicion = almuerzoJObject["guarnicion"] as String
                        val postre = almuerzoJObject["postre"] as String
                        val precio = almuerzoJObject["precio"] as String
                        val refresco = almuerzoJObject["refresco"] as String
                        val segundo = almuerzoJObject["segundo"] as String
                        val sopa = almuerzoJObject["sopa"] as String

                        val fechaFormat = Utilitarios.getStringToDateddMMyyyy(fecha)
                        listaAlmuerzo.add(AlmuerzoComedor(1,"",descripcion,dia,entrada,fechaFormat,guarnicion,postre,precio,refresco,segundo,sopa,"",""))
                    }
                    getListaOrdenadaMenu(listaAlmuerzo)
                } else {
                    lblMensaje_menu.visibility = View.VISIBLE
                    lblMensaje_menu.text = resources.getString(R.string.advertencia_no_existe_menu)
                }
            },
            { error ->
                prbCargando_menu.visibility = View.GONE
                lblMensaje_menu.visibility = View.VISIBLE
                lblMensaje_menu.text = resources.getString(R.string.error_no_conexion)
            }
        )
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }

    private fun getListaOrdenadaMenu(listaAuxAlmuerzo: ArrayList<AlmuerzoComedor>) {
        val listaAUXAlmuerzoComedor = ArrayList<AlmuerzoComedor>()
        val listaAlmuerzoComedor = ArrayList<AlmuerzoComedor>()
        var cont = 1
        while (cont <= 7) {
            for (almuer in listaAuxAlmuerzo) {
                if (cont == almuer.dia) {
                    listaAUXAlmuerzoComedor.add(almuer)
                }
            }
            cont++
        }

        var iDia = 0
        var diaActual = ""
        var fecha = ""
        var scrollPositionStar = -1

        for (i in 0 until listaAUXAlmuerzoComedor.size) {
            val alm = listaAUXAlmuerzoComedor[i]
            if (scrollPositionStar == -1) {
                val diaString = Utilitarios.getDateToStringddMMyyyy(alm.fecha ?: Date())
                if (diaString == Utilitarios.getDateToStringddMMyyyy(Date())) {
                    scrollPositionStar = listaAlmuerzoComedor.size
                }
            }

            if (i == 0) {
                iDia = alm.dia ?: 0
                diaActual = Utilitarios.getDia(iDia, this)
                //fecha = Utilitarios.getDateToStringddMMyyyy(alm.fecha ?: Date())
                fecha = Utilitarios.getDateToStringddMMyyyyWithoutHHmm(alm.fecha ?: Date())
                listaAlmuerzoComedor.add(AlmuerzoComedor(0, "$diaActual , $fecha"))
                listaAlmuerzoComedor.add(alm)
            } else {
                if (iDia == alm.dia) {
                    listaAlmuerzoComedor.add(alm)
                } else {
                    iDia = alm.dia ?: 0
                    diaActual = Utilitarios.getDia(iDia, this)
                    //fecha = Utilitarios.getDateToStringddMMyyyy(alm.fecha ?: Date())
                    fecha = Utilitarios.getDateToStringddMMyyyyWithoutHHmm(alm.fecha ?: Date())
                    listaAlmuerzoComedor.add(AlmuerzoComedor(0, "$diaActual , $fecha"))
                    listaAlmuerzoComedor.add(alm)
                }
            }
        }

        rvAlumerzo_menu.adapter = AlmuerzoAdapter(listaAlmuerzoComedor)
        rvAlumerzo_menu.scrollToPosition(scrollPositionStar)
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
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
}
