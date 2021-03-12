package pe.edu.esan.appostgrado.view.academico.pregrado

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_curso_detalle.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.NotasPreAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.CursosPre
import pe.edu.esan.appostgrado.helpers.Utils
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.academico.DirectorioActivity

class CursoDetalleActivity : AppCompatActivity() {

    private var cursoActual: CursosPre? = null

    private lateinit var controlViewModel: ControlViewModel

    private var anchoPantalla: Int = 0
    private var densidad: Float = 0f

    private val TAG = "CursoDetalleActivity"
    /*private var requestQueue: RequestQueue? = null*/

    private val LOG = CursoDetalleActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curso_detalle)

        val toolbar = main_cursopredetalle as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.cursos)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()

        if(ControlUsuario.instance.currentUsuario.size == 1 || ControlUsuario.instance.currentUsuarioGeneral != null){
            continueSetup()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        continueSetup()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
        }
    }

    private fun continueSetup(){

        cursoActual = ControlUsuario.instance.currentCursoPre

        if (cursoActual != null) {
            if(intent.hasExtra("alumno_esta_retirado")){
                if(intent.getBooleanExtra("alumno_esta_retirado", false)){
                    lblCurso_cursopredetalle.visibility = View.GONE
                    lblSPorfesor_cursopredetalle.visibility = View.GONE
                    lblProfesor_cursopredetalle.visibility = View.GONE
                    lblMensajeAsistencia_cursopredetalle.visibility = View.GONE
                    viewDetalleAsistencia_cursopredetalle.visibility = View.GONE
                    lblSNotas_cursopredetalle.visibility = View.GONE
                    rvNotas_cursopredetalle.visibility = View.GONE
                    lblSPromedio_cursopredetalle.visibility = View.GONE
                    lblPromedio_cursopredetalle.visibility = View.GONE
                    alumno_retirado.visibility = View.VISIBLE
                } else {
                    alumno_retirado.visibility = View.GONE
                    lblCurso_cursopredetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.BOLD)
                    lblSPorfesor_cursopredetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    lblProfesor_cursopredetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)

                    lblSAsistencias_cursopredetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
                    lblSTardanzas_cursopredetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
                    lblSFaltas_cursopredetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)

                    lblSNotas_cursopredetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    lblSPromedio_cursopredetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)
                    lblPromedio_cursopredetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)

                    val displaymetrics = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(displaymetrics)
                    anchoPantalla = displaymetrics.widthPixels
                    densidad = displaymetrics.density

                    var cont = 0
                    var profesores = ""
                    for (profesor in cursoActual!!.listProfesores) {
                        if (profesor.isResponsable) {
                            if (cont > 1) {
                                profesores += " - "
                            }
                            profesores += profesor.nombreCompleto
                            cont++
                        }
                    }

                    lblCurso_cursopredetalle.text = cursoActual!!.nombreCurso
                    lblProfesor_cursopredetalle.text = profesores
                    lblPromedio_cursopredetalle.text = cursoActual!!.promedio

                    rvNotas_cursopredetalle.layoutManager =
                        androidx.recyclerview.widget.LinearLayoutManager(this)
                    rvNotas_cursopredetalle.adapter = NotasPreAdapter(cursoActual!!.listNotasPre)

                    /*getBarraAsistencia(cursoActual.seccionCodigo)*/
                    cargarBarraAsistencia()
                }
            }

        } else {
            finish()
        }
    }

    /*private fun getBarraAsistencia(codigoSeccion: String) {
        if (ControlUsuario.instance.currentUsuario.size == 1) {
            val usuario = ControlUsuario.instance.currentUsuario[0] as UserEsan

            val request = JSONObject()
            request.put("CodAlumno", usuario.codigo)

            val requestEncriptado = Utilitarios.jsObjectEncrypted(request, this)
            if (requestEncriptado != null)
                onBarraAsistencia(Utilitarios.getUrl(Utilitarios.URL.ASIS_ALUMNO_PRE), requestEncriptado, codigoSeccion)
            else {
                /*lblMensaje_historiconotaspre.text = resources.getText(R.string.error_encriptar)
                lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
            }
        } else {
            /*lblMensaje_historiconotaspre.text = resources.getText(R.string.error_ingreso)
            lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
        }
    }*/

    /*private fun onBarraAsistencia(url: String, request: JSONObject, codSeccion: String) {
        //ResumenAsistenciaAlumnoPregradoResult
        requestQueue = Volley.newRequestQueue(this)
        val jsObjectRequest = JsonObjectRequest (
                url,
                request,
                Response.Listener { response ->
                    try {
                        val historicoJArray = Utilitarios.jsArrayDesencriptar(response["ResumenAsistenciaAlumnoPregradoResult"] as String, this)

                        if (historicoJArray != null) {
                            if (historicoJArray.length() > 0) {

                                for (i in 0 until historicoJArray.length()) {
                                    val historicoJObject = historicoJArray[i] as JSONObject


                                    val codigoSeccion = historicoJObject["SeccionCodigo"] as String

                                    if (codSeccion == codigoSeccion) {
                                        val tardanza = historicoJObject["CantTardanzas"] as Int
                                        val asistencia = historicoJObject["CantAsistencias"] as Int
                                        val faltas = historicoJObject["CantFalta"] as Int
                                        val sesiones = historicoJObject["CantidadSesiones"] as Int

                                        cursoActual!!.asistencias = asistencia
                                        cursoActual!!.tardanzas = tardanza
                                        cursoActual!!.faltas = faltas
                                        cursoActual!!.totalsesiones = sesiones

                                        kotlin.run {
                                            cargarBarraAsistencia()
                                        }

                                        break
                                    }

                                }

                                //rvCurso_historiconotaspre.adapter = HistoricoNotaPreAdapter(listaCursos)

                            } else {
                                /*lblMensaje_historiconotaspre.text = resources.getText(R.string.advertencia_no_informacion)
                                lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                            }
                        } else {
                            /*lblMensaje_historiconotaspre.text = resources.getText(R.string.error_desencriptar)
                            lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                        }
                    } catch (jex: JSONException) {

                        /*lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                        lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                    } catch (ccax: ClassCastException) {

                        /*lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                        lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                    }
                    //prbCargando_historiconotaspre.visibility = View.GONE
                },
                Response.ErrorListener { error ->

                    /*prbCargando_historiconotaspre.visibility = View.GONE
                    lblMensaje_historiconotaspre.text = resources.getText(R.string.error_respuesta_server)
                    lblMensaje_historiconotaspre.visibility = View.VISIBLE*/
                }
        )
        jsObjectRequest.tag = TAG
        requestQueue?.add(jsObjectRequest)
    }*/

    // Si kotlin genera error hazlo con java :v
    private fun cargarBarraAsistencia() {

        if (!cursoActual!!.errorasistencias) {
            lblMensajeAsistencia_cursopredetalle.visibility = View.GONE
            viewDetalleAsistencia_cursopredetalle.visibility = View.VISIBLE

             val porcRestante = 100 - (Utils().calcularPromedioNumber(cursoActual!!.totalsesiones, cursoActual!!.asistencias) + Utils().calcularPromedioNumber(cursoActual!!.totalsesiones, cursoActual!!.tardanzas) + Utils().calcularPromedioNumber(cursoActual!!.totalsesiones, cursoActual!!.faltas))



            lblSAsistencias_cursopredetalle.text = resources.getString(R.string.asistencia_cant_porc) + " \n " +  Utils().inToString(cursoActual!!.asistencias)  + " (" + Utils().calcularPromedioText(cursoActual!!.totalsesiones, cursoActual!!.asistencias) + "%)"
            lblSTardanzas_cursopredetalle.text = resources.getString(R.string.tardanza_cant_porc) + " \n " +  Utils().inToString(cursoActual!!.tardanzas) + " (" + Utils().calcularPromedioText(cursoActual!!.totalsesiones, cursoActual!!.tardanzas) + "%)"
            lblSFaltas_cursopredetalle.text = resources.getString(R.string.falta_cant_porc) + " \n " +  Utils().inToString(cursoActual!!.faltas) + " (" + Utils().calcularPromedioText(cursoActual!!.totalsesiones, cursoActual!!.faltas) + "%)"

            val valor = 16 * densidad + 0.5f
            val largoTotal = anchoPantalla - (valor.toInt() * 2)

            val widthAsis = largoTotal * Utils().calcularPromedioNumber(cursoActual!!.totalsesiones, cursoActual!!.asistencias) / 100
            val widthTard = largoTotal * Utils().calcularPromedioNumber(cursoActual!!.totalsesiones, cursoActual!!.tardanzas) / 100
            val widthFalta = largoTotal * Utils().calcularPromedioNumber(cursoActual!!.totalsesiones, cursoActual!!.faltas) / 100
            val widthRest = largoTotal * porcRestante / 100

            val widthTardReal = widthAsis + widthTard
            val widthFaltReal = widthTardReal + widthFalta

            viewMain.setBackgroundColor(ContextCompat.getColor(this,R.color.main_asistencia))
            viewAsistencia_cursopredetalle.setBackgroundColor(ContextCompat.getColor(this,R.color.asistencia))
            viewTardanza_cursopredetalle.setBackgroundColor(ContextCompat.getColor(this,R.color.tardanza))
            viewFalta_cursopredetalle.setBackgroundColor(ContextCompat.getColor(this,R.color.falta))

            viewAsistencia_cursopredetalle.layoutParams.width = widthAsis.toInt()
            viewTardanza_cursopredetalle.layoutParams.width = widthTardReal.toInt()
            viewFalta_cursopredetalle.layoutParams.width = widthFaltReal.toInt()

            /*val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )

            params.addRule(RelativeLayout.ALIGN_PARENT_START)

            viewAsistencia_cursopredetalle.layoutParams = params
            viewTardanza_cursopredetalle.layoutParams = params
            viewFalta_cursopredetalle.layoutParams = params*/

            viewMain.requestLayout()
            viewAsistencia_cursopredetalle.requestLayout()
            viewTardanza_cursopredetalle.requestLayout()
            viewFalta_cursopredetalle.requestLayout()

        } else {
            lblMensajeAsistencia_cursopredetalle.visibility = View.VISIBLE
            viewDetalleAsistencia_cursopredetalle.visibility = View.INVISIBLE
            lblMensajeAsistencia_cursopredetalle.text = "No se pudo obtener el detalle de sus asistencias"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {

                finish()
                return true
            }
            R.id.menu_partner -> {
                val intentContacto = Intent(this@CursoDetalleActivity, DirectorioActivity::class.java)
                intentContacto.putExtra("CodSeccion", cursoActual!!.seccionCodigo)
                intentContacto.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intentContacto)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        /*requestQueue?.cancelAll(TAG)*/
        controlViewModel.insertDataToRoom()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_partner, menu)
        return true
    }

}
