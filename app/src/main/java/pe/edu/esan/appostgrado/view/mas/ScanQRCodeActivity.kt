package pe.edu.esan.appostgrado.view.mas

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.crashlytics.FirebaseCrashlytics
/*import com.crashlytics.android.Crashlytics*/
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_scan_qrcode.*
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.helpers.ShowAlertHelper
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.login.LoginActivity

class ScanQRCodeActivity : AppCompatActivity() {

    private var requestQueue : RequestQueue? = null

    private val LOG = ScanQRCodeActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qrcode)

        setSupportActionBar(my_toolbar_lectura_qr)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.lectura_codigo_qr)

        my_toolbar_lectura_qr.navigationIcon?.setColorFilter(
            ContextCompat.getColor(this, R.color.md_white_1000),
            PorterDuff.Mode.SRC_ATOP
        )

        my_toolbar_lectura_qr.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        read_qr_code_button.setOnClickListener {
            IntentIntegrator(this@ScanQRCodeActivity).initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if(result != null){
            if(result.contents == null){
                Toast.makeText(this,"Cancelled", Toast.LENGTH_LONG).show()
            } else {
                //TODO: USAR STRING RESOURCES PARA LA TRADUCCIÓN AL INGLÉS
                AlertDialog.Builder(this)
                    .setTitle("Código QR")
                    .setMessage("Valor obtenido: " + result.contents)
                    .setPositiveButton("OK") { _ , _ -> }
                    .setCancelable(false)
                    .show()

                text_view_qr_read_result.text = result.contents

                sendDataToBackEnd(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun sendDataToBackEnd(qrValue: String){

        val request = JSONObject()

        request.put("codeqr", qrValue)

        requestQueue = Volley.newRequestQueue(this)

        val jsObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            "<URL DEL SERVICIO WEB PARA EL CÓDIGO QR>",
            request,
            { response ->
                try {

                    //THIS DATA IS FOR REFERENCE ONLY
                    //val responseBackEnd = response["ObtenerVersionMovilResult"]  as? JSONObject
                    //val versionMovilResult = responseBackEnd?.getInt("VersionAndroid") ?: 0
                    //val flagAndroid = responseBackEnd?.getBoolean("FlagAndroid") ?: false

                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.log("E/ScanQRCodeActivity: ${error.message.toString()}")
                FirebaseCrashlytics.getInstance().recordException(Exception("Volley Exception: ${error.message.toString()}"))

                val showAlertHelper = ShowAlertHelper(this)
                showAlertHelper.showAlertError(getString(R.string.error),getString(R.string.error_no_conexion), null);
            }
        )
        requestQueue?.add(jsObjectRequest)

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
}
