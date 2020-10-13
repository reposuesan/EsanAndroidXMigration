package pe.edu.esan.appostgrado

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import androidx.core.content.pm.PackageInfoCompat
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.crashlytics.FirebaseCrashlytics
/*import com.crashlytics.android.Crashlytics*/
import org.json.JSONObject
import pe.edu.esan.appostgrado.helpers.ShowAlertHelper
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.login.LoginActivity

class SecondLauncherActivity : AppCompatActivity() {

    private val TAG = "SecondLauncherActivity"
    private var segundo = 1
    private var milisegundo = segundo * 1000

    private var requestQueue : RequestQueue? = null

    private val LOG = SecondLauncherActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_launcher)

        crearAnimacion()
    }

    private fun crearAnimacion() {

        object : CountDownTimer(milisegundo.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) { }

            override fun onFinish() {
                validateVersionApp()
            }
        }.start()
    }


    private fun validateVersionApp(){

        val request = JSONObject()

        requestQueue = Volley.newRequestQueue(this)

        val jsObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            Utilitarios.getUrl(Utilitarios.URL.VERSION_CODE),
            request,
            { response ->
                try {
                    val pInfo = packageManager.getPackageInfo(packageName, 0)
                    val code = PackageInfoCompat.getLongVersionCode(pInfo)

                    Log.i(LOG, "The versionCode is: $code")

                    if (!response.isNull("ObtenerVersionMovilResult")) {
                        val data = response["ObtenerVersionMovilResult"]  as? JSONObject
                        //println(data)
                        val versionMovilResult = data?.getInt("VersionAndroid") ?: 0
                        val flagAndroid = data?.getBoolean("FlagAndroid") ?: false

                        if(flagAndroid){

                            if(code < versionMovilResult){

                                val showAlertHelper = ShowAlertHelper(this)

                                showAlertHelper.showAlertError(getString(R.string.update), getString(R.string.update_description))

                                {
                                    val appPackageName = packageName // getPackageName() from Context or Activity object
                                    try {
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                                    } catch (anfe: android.content.ActivityNotFoundException) {
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                                    }
                                }


                            } else {
                                Log.i(LOG, "flagAndroid is true")
                                val i = Intent(applicationContext, LoginActivity::class.java)
                                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                //i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                startActivity(i)
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                finish()
                            }
                        } else {
                            Log.i(LOG, "flagAndroid is false")
                            val i = Intent(applicationContext, LoginActivity::class.java)
                            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            //i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(i)
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                            finish()

                        }
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e(LOG, "Volley error:  " + error.message.toString())

                val crashlytics = FirebaseCrashlytics.getInstance()

                crashlytics.log("E/SecondLauncherActivity: ${error.message.toString()}")
                FirebaseCrashlytics.getInstance().recordException(Exception("Volley Exception: ${error.message.toString()}"))

                val showAlertHelper = ShowAlertHelper(this)
                showAlertHelper.showAlertError(getString(R.string.error),getString(R.string.error_no_conexion), null);
            }
        )
        requestQueue?.add(jsObjectRequest)
    }

    override fun onStop() {
        super.onStop()
        requestQueue?.cancelAll(TAG)
    }
}