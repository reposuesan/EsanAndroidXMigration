package pe.edu.esan.appostgrado.view.puntosreunion.pregrado

import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_pregrado_cubs_detalle.*

import pe.edu.esan.appostgrado.R
import androidx.core.content.ContextCompat
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import pe.edu.esan.appostgrado.view.MenuPrincipalActivity
import java.util.*


class PregradoCubsDetalleActivity : AppCompatActivity() {

    private val LOG = PregradoCubsDetalleActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregrado_cubs_detalle)

        setSupportActionBar(my_toolbar_detalle)
        //Disable the button
        supportActionBar!!.setHomeButtonEnabled(false)
        //Remove the left caret
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        //Remove the icon
        supportActionBar!!.setDisplayShowHomeEnabled(false)

        title = getString(R.string.detalle_de_prereserva)

        my_toolbar_detalle.navigationIcon?.setColorFilter(ContextCompat.getColor(this, R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP)
        my_toolbar_detalle.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        val mensajePrereservaRealizada = intent.getStringExtra("mensaje_prereserva_realizada")
        val fecha = intent.getStringExtra("fecha")
        val rangoHoras = intent.getStringExtra("rango_horario")

        val language = Locale.getDefault().displayLanguage

        if(language.equals("English")){
            tv_mensaje_prereserva_realizada.text = "The pre-reservation was made successfully. Please go to the record section in the study rooms main screen to review the details of the pre-reservation."
            tv_mensaje_prereserva_realizada.setTextColor(ContextCompat.getColor(this, R.color.green))
        } else {
            /*tv_mensaje_prereserva_realizada.text = "${mensajePrereservaRealizada}.\n\nPor ejemplo si su pre-reserva inicia a las 17:00 Hrs. usted puede confirmar la pre-reserva desde las 16:50 Hrs. hasta las 17:10 Hrs."*/
            tv_mensaje_prereserva_realizada.text = "${mensajePrereservaRealizada}."
            tv_mensaje_prereserva_realizada.setTextColor(ContextCompat.getColor(this, R.color.green))
        }

        tv_fecha_detalle.text = fecha

        tv_horario_detalle.text = rangoHoras

        tv_solicitante_detalle.text = ""
        tv_solicitante_detalle.visibility = View.GONE
        tv_solicitante_detalle_label.visibility = View.GONE

        tv_estado_detalle.visibility = View.GONE
        tv_estado_detalle_label.visibility = View.GONE

        detalle_volver_pr_button.setOnClickListener {
            val intent = Intent(this@PregradoCubsDetalleActivity, MenuPrincipalActivity::class.java)
            intent.putExtra("back_from_pregrado_prereserva", 0)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this@PregradoCubsDetalleActivity, MenuPrincipalActivity::class.java)
                intent.putExtra("back_from_pregrado_prereserva", 0)
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
            val intent = Intent(this@PregradoCubsDetalleActivity, MenuPrincipalActivity::class.java)
            intent.putExtra("back_from_pregrado_prereserva", 0)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }


}
