package pe.edu.esan.appostgrado

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class LauncherActivity : AppCompatActivity() {

    private var segundo = 1
    private var milisegundo = segundo * 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        crearAnimacion()
    }

    private fun crearAnimacion() {

        object : CountDownTimer(milisegundo.toLong(), 1000) {
            //var i: Intent? = null

            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                val i = Intent(applicationContext, SecondLauncherActivity::class.java)
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }.start()
    }
}
