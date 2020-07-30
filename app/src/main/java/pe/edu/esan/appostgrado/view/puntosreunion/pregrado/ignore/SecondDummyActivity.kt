package pe.edu.esan.appostgrado.view.puntosreunion.pregrado.ignore

import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.KeyEvent
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_second_dummy.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.view.MenuPrincipalActivity

class SecondDummyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_dummy)

        setSupportActionBar(my_toolbar_second_dummy)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = "Second Dummy"

        my_toolbar_second_dummy.navigationIcon?.setColorFilter(
            ContextCompat.getColor(this, R.color.md_white_1000),
            PorterDuff.Mode.SRC_ATOP
        )

        my_toolbar_second_dummy.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        returnViewPagerButton.setOnClickListener {
            val intent = Intent(this@SecondDummyActivity, MenuPrincipalActivity::class.java)
            intent.putExtra("back_from_pregrado_prereserva", 0)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                //NavUtils.navigateUpFromSameTask(this)
                val intent = Intent(this@SecondDummyActivity, MenuPrincipalActivity::class.java)
                intent.putExtra("back_from_pregrado_prereserva", 0)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(
                    //Intent(this@PregradoCubsDetalleActivity, PregradoCubiculosPrincipalActivity::class.java)
                    intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(this@SecondDummyActivity, MenuPrincipalActivity::class.java)
            intent.putExtra("back_from_pregrado_prereserva", 0)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(
                //Intent(this@PregradoCubsDetalleActivity, PregradoCubiculosPrincipalActivity::class.java)
                intent)
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}
