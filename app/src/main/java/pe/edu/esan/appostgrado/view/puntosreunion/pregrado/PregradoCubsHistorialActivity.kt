package pe.edu.esan.appostgrado.view.puntosreunion.pregrado

import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_pregrado_cubs_historial.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.PregradoPrereservasHistorialAdapter
import pe.edu.esan.appostgrado.entidades.PrereservaDetalle
import pe.edu.esan.appostgrado.helpers.ShowAlertHelper
import pe.edu.esan.appostgrado.util.Utilitarios
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_malla_curricular.*
import pe.edu.esan.appostgrado.util.getHeaderForJWT
import pe.edu.esan.appostgrado.util.renewToken
import kotlin.text.StringBuilder

class PregradoCubsHistorialActivity : AppCompatActivity(), PregradoPrereservasHistorialAdapter.HistorialListener , PregradoPrereservasHistorialAdapter.HistorialMapaListener{

    private val LOG = PregradoCubsHistorialActivity::class.simpleName

    private var mRequestQueue: RequestQueue? = null

    private val TAG = "PregradoCubsHistorialActivity"

    private var URL_TEST = Utilitarios.getUrl(Utilitarios.URL.PREG_PP_LISTA_PRERESERVAS)
    private var URL_TEST_SECOND = Utilitarios.getUrl(Utilitarios.URL.PREG_PP_LISTA_ALUMNOS_X_GRUPO)

    private var prereservasList = ArrayList<PrereservaDetalle>()

    private var promocion: String = ""
    private var direccion: String = ""

    private var userIsOut = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_cubs_historial)

        setSupportActionBar(my_toolbar_historial)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.salas_de_estudio_title)

        my_toolbar_historial.navigationIcon?.setColorFilter(ContextCompat.getColor(this, R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP)
        my_toolbar_historial.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        recycler_view_historial.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        recycler_view_historial.setHasFixedSize(true)

        progress_bar_historial.visibility = View.VISIBLE
        tv_cargando_integrantes.visibility = View.GONE
        tv_empty_historial.visibility = View.GONE

        tv_promocion_historial.visibility = View.GONE
        tv_direccion_historial.visibility = View.GONE

        val usuarioActual = intent.getStringExtra("codigo_alumno")

        promocion = intent.getStringExtra("promocion") ?: ""
        direccion = intent.getStringExtra("direccion") ?: ""

        userIsOut = false

        val request = JSONObject()

        request.put("CodAlumno", usuarioActual)

        listaPrereservasPorAlumnoServicio(URL_TEST, request)
    }

    fun listaPrereservasPorAlumnoServicio(url: String, request: JSONObject) {

        val fRequest = Utilitarios.jsObjectEncrypted(request, this)

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            val jsonObjectRequest = object: JsonObjectRequest(
            /*val jsonObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                fRequest,
                { response ->
                    if (!response.isNull("ListaPreReservasxAlumnoResult")) {
                        val jsResponse = Utilitarios.jsArrayDesencriptar(response.getString("ListaPreReservasxAlumnoResult"), this@PregradoCubsHistorialActivity)

                        try {

                            if(jsResponse!!.length() > 0){

                            for (i in 0..jsResponse.length() - 1) {
                                val reservaItem = jsResponse.getJSONObject(i)

                                val nomCubiculo = reservaItem.optString("NomCubiculo")
                                val fechaReserva = reservaItem.optString("FechaReserva")
                                val horaInicio = reservaItem.optString("HoraIni")
                                val horaFin = reservaItem.optString("HoraFin")
                                val valTabla = reservaItem.optString("ValTabla")
                                val refUbicacion = reservaItem.optString("RefUbicacion")
                                val idGrupoPre = reservaItem.getInt("IdGrupoPre")
                                val alumnoReserva = reservaItem.optString("AlumnoReserva")
                                val urlImgUbicacion = reservaItem.optString("UrlImgUbicacion")

                                prereservasList.add(
                                    PrereservaDetalle(
                                        nomCubiculo,
                                        alumnoReserva,
                                        "$horaInicio - $horaFin",
                                        fechaReserva,
                                        refUbicacion,
                                        valTabla, idGrupoPre, "$urlImgUbicacion.pdf"
                                    )
                                )
                            }
                                recycler_view_historial.adapter = PregradoPrereservasHistorialAdapter(prereservasList, false, this@PregradoCubsHistorialActivity,this@PregradoCubsHistorialActivity)
                                tv_empty_historial.visibility = View.GONE
                                progress_bar_historial.visibility = View.GONE
                                tv_cargando_integrantes.visibility = View.GONE
                                tv_promocion_historial.visibility = View.VISIBLE
                                tv_direccion_historial.visibility = View.VISIBLE
                                tv_promocion_historial.text = getString(R.string.promocion_mensaje, promocion)
                                tv_direccion_historial.text = getString(R.string.direccion_mensaje, direccion)

                            } else {
                                tv_empty_historial.visibility = View.VISIBLE
                                recycler_view_historial.visibility = View.GONE
                                progress_bar_historial.visibility = View.GONE
                                tv_cargando_integrantes.visibility = View.GONE
                                tv_promocion_historial.visibility = View.GONE
                                tv_direccion_historial.visibility = View.GONE
                            }

                        } catch (e: Exception) {
                            val snack = Snackbar.make(
                                findViewById(android.R.id.content),
                                getString(R.string.error_servidor_extraccion_datos),
                                Snackbar.LENGTH_LONG
                            )
                            snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.warning_text))
                            snack.show()
                            progress_bar_historial.visibility = View.GONE
                            tv_cargando_integrantes.visibility = View.GONE
                            tv_promocion_historial.visibility = View.GONE
                            tv_direccion_historial.visibility = View.GONE
                        }
                    }
                },
                { error ->
                    if(error.networkResponse.statusCode == 401) {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                listaPrereservasPorAlumnoServicio(url, request)
                            } else {
                                progress_bar_historial.visibility = View.GONE
                                tv_cargando_integrantes.visibility = View.GONE
                                tv_direccion_historial.visibility = View.GONE
                                tv_promocion_historial.visibility = View.GONE

                                val showAlertHelper = ShowAlertHelper(this)
                                showAlertHelper.showAlertError(
                                    getString(R.string.error),
                                    getString(R.string.error_no_conexion),
                                    null
                                )
                            }
                        }
                    } else {
                        progress_bar_historial.visibility = View.GONE
                        tv_cargando_integrantes.visibility = View.GONE
                        tv_direccion_historial.visibility = View.GONE
                        tv_promocion_historial.visibility = View.GONE

                        val showAlertHelper = ShowAlertHelper(this)
                        showAlertHelper.showAlertError(
                            getString(R.string.error),
                            getString(R.string.error_no_conexion),
                            null
                        )
                    }
                }
            )
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            {
                override fun getHeaders(): MutableMap<String, String> {
                    return getHeaderForJWT()
                }
            }
            jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            jsonObjectRequest.tag = TAG

            mRequestQueue?.add(jsonObjectRequest)

        }
    }

    override fun onClickItem(position: Int) {
        val item = prereservasList[position]
        val grupoId = item.grupoId

        val request = JSONObject()

        request.put("IdGrupoPre", grupoId)

        muestraIntegrantesDeGrupoServicio(URL_TEST_SECOND, request)

        progress_bar_historial.visibility = View.VISIBLE
        tv_cargando_integrantes.visibility = View.VISIBLE
        recycler_view_historial.visibility = View.GONE
        tv_empty_historial.visibility = View.GONE
    }

    fun muestraIntegrantesDeGrupoServicio(url: String, request: JSONObject){

        val fRequest = Utilitarios.jsObjectEncrypted(request, this)

        if (fRequest != null) {
            mRequestQueue = Volley.newRequestQueue(this)
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            val jsonObjectRequest = object: JsonObjectRequest(
            /*val jsonObjectRequest = JsonObjectRequest(*/
                Request.Method.POST,
                url,
                fRequest,
                { response ->
                    if (!response.isNull("ListaAlumnosxGrupoResult")) {
                        val jsResponse = Utilitarios.jsArrayDesencriptar(response.getString("ListaAlumnosxGrupoResult"), this@PregradoCubsHistorialActivity)

                        try {
                            if(jsResponse!!.length() > 0){

                                val stringBuilder = StringBuilder()
                                val creadorLabel = getString(R.string.creador_text_for_historial)
                                val responsableLabel = getString(R.string.responsable_text)

                                for (i in 0..jsResponse.length() - 1) {
                                    val grupoItem = jsResponse.getJSONObject(i)
                                    val codigo = grupoItem.optString("Codigo")
                                    val nombreAlumno = grupoItem.optString("NombreCompleto")
                                    val creador = grupoItem.optString("Creador")
                                    val responsable = grupoItem.optString("Responsable")

                                    if(creador.toInt() == 1){
                                        stringBuilder.append("$codigo - $nombreAlumno ($creadorLabel)\n\n")
                                        if(responsable.toInt() == 1){
                                            stringBuilder.replace(stringBuilder.lastIndex - 1, stringBuilder.length,"")
                                            stringBuilder.append(" ($responsableLabel)\n\n")
                                        }
                                    } else {
                                        stringBuilder.append("$codigo - $nombreAlumno\n\n")
                                        if(responsable.toInt() == 1){
                                            stringBuilder.replace(stringBuilder.lastIndex - 1, stringBuilder.length,"")
                                            stringBuilder.append(" ($responsableLabel)\n\n")
                                        }
                                    }

                                }
                                val messageString = stringBuilder.toString()
                                showMiembrosDelGrupo(messageString.substring(0, messageString.length - 2),true)

                            } else {
                                showMiembrosDelGrupo(getString(R.string.error_recuperacion_datos),false)
                            }

                        } catch (e: Exception) {
                            showMiembrosDelGrupo(getString(R.string.error_recuperacion_datos),false)
                        }
                    }
                },
                { error ->
                    if(error.networkResponse.statusCode == 401) {
                        renewToken { token ->
                            if(!token.isNullOrEmpty()){
                                muestraIntegrantesDeGrupoServicio(url, request)
                            } else {
                                error.printStackTrace()
                                showMiembrosDelGrupo(getString(R.string.no_respuesta_desde_servidor),false)
                            }
                        }
                    } else {
                        error.printStackTrace()
                        showMiembrosDelGrupo(getString(R.string.no_respuesta_desde_servidor),false)
                    }

                }
            )
            //IMPLEMENTACIÓN DE JWT (JSON WEB TOKEN)
            {
                override fun getHeaders(): MutableMap<String, String> {
                    return getHeaderForJWT()
                }
            }
            jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            jsonObjectRequest.tag = TAG

            mRequestQueue?.add(jsonObjectRequest)

        }
    }

    fun showMiembrosDelGrupo(mensaje: String, dataIsCorrect: Boolean){

        progress_bar_historial.visibility = View.GONE
        tv_cargando_integrantes.visibility = View.GONE
        recycler_view_historial.visibility = View.VISIBLE
        tv_empty_historial.visibility = View.GONE

        val builder = if(dataIsCorrect) {
            AlertDialog.Builder(this@PregradoCubsHistorialActivity, R.style.EsanAlertDialogSuccess)
        } else{
            AlertDialog.Builder(this@PregradoCubsHistorialActivity, R.style.EsanAlertDialog)

        }

        builder.setTitle(getString(R.string.integrantes_cub))
            .setMessage(mensaje)
            .setPositiveButton(getString(R.string.positive_dialog)) { dialog, which ->
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .setCancelable(false)

        val dialog: AlertDialog = builder.create()

        if(!userIsOut){
            dialog.show()
        }

    }

    override fun onClickMapaItem(urlString: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(urlString)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        userIsOut = true
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        mRequestQueue?.cancelAll(TAG)
    }

}
