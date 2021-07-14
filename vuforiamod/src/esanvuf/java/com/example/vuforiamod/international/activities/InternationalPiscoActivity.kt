package com.example.vuforiamod.international.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.example.vuforiamod.R
import kotlinx.android.synthetic.esanvuf.activity_international_pisco.*

class InternationalPiscoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_international_pisco)

        setSupportActionBar(toolbar_international_controller_party_pisco)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val upArrow = getDrawable(R.drawable.ic_arrow_white_24dp)
            supportActionBar!!.setHomeAsUpIndicator(upArrow)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            // supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        /*toolbar_international_controller_party_pisco.setNavigationOnClickListener {

        }*/

        //wv_international_party_pisco.loadUrl("https://esan.sfo2.digitaloceanspaces.com/FiestaPisco.html")
        //img_international_party_pisco.setImageResource(R.drawable.fiesta_pisco_2019_2)

    }

    /*override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        finish()
        return true
    }*/

    override fun onBackPressed() {
       super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //Respond to the action bar's Up/Home button
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
