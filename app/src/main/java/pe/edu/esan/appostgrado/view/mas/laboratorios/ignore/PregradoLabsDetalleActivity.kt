package pe.edu.esan.appostgrado.view.mas.laboratorios.ignore

import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_pregrado_labs_detalle.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.view.mas.laboratorios.PregradoLabsPrincipalActivity

class PregradoLabsDetalleActivity : AppCompatActivity() {

    private val LOG = PregradoLabsDetalleActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_labs_detalle)

        setSupportActionBar(my_toolbar_detalle_lab)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.laboratorios_title)

        my_toolbar_detalle_lab.navigationIcon?.setColorFilter(ContextCompat.getColor(this, R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP)
        my_toolbar_detalle_lab.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        if(ControlUsuario.instance.currentUsuarioGeneral != null){
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

        val mensajePrereservaRealizada = intent.getStringExtra("mensaje_prereserva")
        val fechaPrereserva = intent.getStringExtra("fecha_prereserva")
        val rangoHoras = intent.getStringExtra("rango_horas_prereserva")

        tv_fecha_detalle_lab.text = fechaPrereserva
        tv_horario_detalle_lab.text = rangoHoras
        tv_mensaje_prereserva_realizada_lab.text = mensajePrereservaRealizada

        val usuarioEnSesion = ControlUsuario.instance.currentUsuarioGeneral

        tv_solicitante_detalle_lab.text = "${usuarioEnSesion!!.nombre} ${usuarioEnSesion.apellido}"

        detalle_volver_menu_principal_button.setOnClickListener {
            val intent = Intent(this@PregradoLabsDetalleActivity, PregradoLabsPrincipalActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        detalle_volver_menu_principal_button.visibility = View.VISIBLE
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //Respond to the action bar's Up/Home button
                val intent = Intent(this@PregradoLabsDetalleActivity, PregradoLabsPrincipalActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(this@PregradoLabsDetalleActivity, PregradoLabsPrincipalActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        controlViewModel.insertDataToRoom()
    }
}
