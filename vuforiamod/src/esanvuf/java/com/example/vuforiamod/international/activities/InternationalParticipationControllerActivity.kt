package com.example.vuforiamod.international.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat

import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vuforiamod.R
import com.example.vuforiamod.helpers.PermissionHelper
import com.example.vuforiamod.util.Utilitarios
import kotlinx.android.synthetic.esanvuf.activity_international_controller_participation.*


class InternationalParticipationControllerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_international_controller_participation)
        setSupportActionBar(toolbar_insternational_controller_participation)
        toolbar_insternational_controller_participation.setTitle(getString(R.string.augmented_reality))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val upArrow = getDrawable(R.drawable.ic_arrow_white_24dp)
            supportActionBar!!.setHomeAsUpIndicator(upArrow)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        img_international_location.setOnClickListener {
            if (Utilitarios.comprobarSensor(applicationContext)) {
                if(PermissionHelper.getCamera(this@InternationalParticipationControllerActivity)){
                    if(PermissionHelper.getLocation(this@InternationalParticipationControllerActivity)){
                        try{
                            val raIntent = Intent(this, Class.forName("pe.edu.esan.appostgrado.view.mas.ra.PrincipalRAActivity"))
                            raIntent.putExtra("accion", "busqueda")
                            startActivity(raIntent)
                        } catch (e: ClassNotFoundException){
                            e.printStackTrace()
                        }
                        /*val links = Intent().setClass(applicationContext, PrincipalRAActivity::class.java).putExtra("accion", "busqueda")
                        startActivity(links)*/
                    } else {
                        val snack = Snackbar.make(findViewById(R.id.content_internaional_content), resources.getString(R.string.error_permiso_gps), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(
                            ContextCompat.getColor(applicationContext, R.color.warning_text))
                        snack.show()
                    }
                } else {
                    val snack = Snackbar.make(findViewById(R.id.content_internaional_content), resources.getString(R.string.error_permiso_camara), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.warning))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(
                        ContextCompat.getColor(applicationContext, R.color.warning_text))
                    snack.show()
                }
            } else {
                val snack = Snackbar.make(findViewById(R.id.content_internaional_content), resources.getString(R.string.error_sensores_ra), Snackbar.LENGTH_LONG)
                snack.view.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.danger))
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.REGULAR)
                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(
                    ContextCompat.getColor(applicationContext, R.color.danger_text))
                snack.show()
            }
        }

        img_international_video.setOnClickListener {
            if(PermissionHelper.getCamera(this@InternationalParticipationControllerActivity)){
                //val intentVuforia = Intent().setClass(applicationContext,  VideoPlayback::class.java)
                //val intentVuforia = Intent().setClass(applicationContext,  AboutScreen::class.java)
                val intentVuforia = Intent().setClassName("appostgrado.esan.edu.pe",  "com.example.vuforiamod.international.activities.engine.Video.app.VideoPlayback.VideoPlayback")
                //val intentVuforia = Intent().setClassName("appostgrado.esan.edu.pe",  "com.example.vuforiamod.international.activities.engine.Video.ui.ActivityList.AboutScreen")
                startActivity(intentVuforia)
            } else {
                Toast.makeText(
                    this@InternationalParticipationControllerActivity,
                    resources.getString(R.string.error_permiso_camara),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

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
