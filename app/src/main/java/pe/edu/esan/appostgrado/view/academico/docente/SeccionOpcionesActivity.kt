package pe.edu.esan.appostgrado.view.academico.docente

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.view.KeyEvent
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_seccion_opciones.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.academico.DirectorioActivity

class SeccionOpcionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seccion_opciones)

        val toolbar = main_seccionopcion as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = ""
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        val seccion = ControlUsuario.instance.currentSeccion
        if (seccion != null) {
            lblSDirectorio_secciondetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            lblSSeguimiento_secciondetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            lblSHistorial_secciondetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
            lblSResultado_secciondetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)

            lblSeccion_secciondetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.THIN)
            lblCurso_secciondetalle.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)

            lblSeccion_secciondetalle.text = seccion.seccionCodigo
            lblCurso_secciondetalle.text = seccion.nombreCurso

            viewDirectorioAlumnos_secciondetalle.setOnClickListener {
                val intentDirectorio = Intent(applicationContext, DirectorioActivity::class.java)
                intentDirectorio.putExtra("CodSeccion", seccion.seccionCodigo)
                startActivity(intentDirectorio)
            }

            viewSeguimientoAlumnos_secciondetalle.setOnClickListener {
                val intentSeguimiento = Intent(applicationContext, SeguimientoAlumnoActivity::class.java)
                intentSeguimiento.putExtra("CodSeccion", seccion.seccionCodigo)
                intentSeguimiento.putExtra("Curso", seccion.nombreCurso)
                startActivity(intentSeguimiento)
            }

            viewHistorialAsistencias_secciondetalle.setOnClickListener {
                val intentHistorico = Intent(applicationContext, HistorialAsistenciaProfesorActivity::class.java)
                intentHistorico.putExtra("CodSeccion", seccion.seccionCodigo)
                intentHistorico.putExtra("Curso", seccion.nombreCurso)
                startActivity(intentHistorico)
            }

            viewResultadoEncuesta_secciondetalle.setOnClickListener {
                val intentResultEncuesta = Intent(applicationContext, ResultadoEncuestaActivity::class.java)
                //intentResultEncuesta.putExtra("CodSeccion", seccion.seccionCodigo) 03998-1-17
                intentResultEncuesta.putExtra("CodSeccion", seccion.seccionCodigo)
                intentResultEncuesta.putExtra("Curso", seccion.nombreCurso)
                startActivity(intentResultEncuesta)
            }
        } else {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                ControlUsuario.instance.currentSeccion = null
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ControlUsuario.instance.currentSeccion = null
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
