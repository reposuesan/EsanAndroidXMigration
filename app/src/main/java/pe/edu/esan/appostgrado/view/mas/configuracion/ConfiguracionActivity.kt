package pe.edu.esan.appostgrado.view.mas.configuracion

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_configuracion.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.adapter.ConfiguracionAdapter
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.MasOpcion
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.mas.configuracion.pversion.HuellaDigitalActivityPAndUp

class ConfiguracionActivity : AppCompatActivity() {

    private var LOG = ConfiguracionActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        val toolbar = main_configuracion as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.configuracion)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow!!.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        if (ControlUsuario.instance.currentUsuario.size == 1) {
            mainSetup()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        mainSetup()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
        }
    }

    private fun mainSetup(){
        rvOpciones_configuracion.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        rvOpciones_configuracion.adapter = ConfiguracionAdapter(getOpcionesConfiguracion()) { masOpcion ->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val mispreferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
                val user = mispreferencias.getString("userWithFingerprint", "")
                val agreetouchId = mispreferencias.getBoolean("touchid", false)

                if(agreetouchId){
                    if (user == ControlUsuario.instance.currentUsuarioGeneral?.usuario) {
                        /**.-.-.-.-.-VALIDAR-..-.-.-.-.-.-.**/
                        masOpcion.intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(masOpcion.intent)
                    } else {
                        val dialog = AlertDialog.Builder(this)
                        dialog.setTitle(getString(R.string.alerta_title)).setMessage(getString(R.string.huella_configurada_previamente_mensaje))
                        dialog.setPositiveButton(getString(R.string.aceptar), null)
                        dialog.create().show()
                    }
                } else {
                    masOpcion.intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(masOpcion.intent)
                }
            } else {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(resources.getString(R.string.huella_digital)).setMessage(getString(R.string.advertencia_huella_digital_version_android))
                dialog.setPositiveButton(resources.getString(R.string.aceptar), null)
                dialog.create().show()
            }

        }
    }

    private fun getOpcionesConfiguracion(): List<MasOpcion> {
        val listaOpciones = ArrayList<MasOpcion>()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            listaOpciones.add(MasOpcion(1, Intent(this, HuellaDigitalActivityPAndUp::class.java), resources.getString(R.string.huella_digital), "", ContextCompat.getDrawable(this, R.drawable.ico_huella)!!))
        } else {
            listaOpciones.add(MasOpcion(1, Intent(this, HuellaDigitalActivity::class.java), resources.getString(R.string.huella_digital), "", ContextCompat.getDrawable(this, R.drawable.ico_huella)!!))
        }

        /*listaOpciones.add(MasOpcion(1, Intent(this, HuellaDigitalActivity::class.java), resources.getString(R.string.huella_digital), "", ContextCompat.getDrawable(this, R.drawable.ico_huella)!!))*/
        return listaOpciones
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
        controlViewModel.insertDataToRoom()
    }
}
