package pe.edu.esan.appostgrado.view.puntosreunion.pregrado.ignore

import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_first_dummy.*
import pe.edu.esan.appostgrado.R

class FirstDummyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_dummy)

        setSupportActionBar(my_toolbar_first_dummy)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = "First Dummmy"

        my_toolbar_first_dummy.navigationIcon?.setColorFilter(
            ContextCompat.getColor(this, R.color.md_white_1000),
            PorterDuff.Mode.SRC_ATOP
        )

        my_toolbar_first_dummy.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        goToSecondButton.setOnClickListener {
            val intent = Intent(this@FirstDummyActivity, SecondDummyActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                //NavUtils.navigateUpFromSameTask(this)
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
