package pe.edu.esan.appostgrado.view.mas

import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_generate_qrcode.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.architecture.viewmodel.ControlViewModel
import pe.edu.esan.appostgrado.control.ControlUsuario

class GenerateQRCodeActivity : AppCompatActivity() {

    private val LOG = GenerateQRCodeActivity::class.simpleName

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_qrcode)

        setSupportActionBar(my_toolbar_generacion_qr)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)

        title = getString(R.string.generacion_codigo_qr)

        my_toolbar_generacion_qr.navigationIcon?.setColorFilter(
            ContextCompat.getColor(this, R.color.md_white_1000),
            PorterDuff.Mode.SRC_ATOP
        )

        my_toolbar_generacion_qr.setTitleTextColor(ContextCompat.getColor(this, R.color.md_white_1000))

        setupQRCode()
    }

    private fun setupQRCode(){

        if(ControlUsuario.instance.currentUsuario.size == 1 && ControlUsuario.instance.currentUsuarioGeneral != null){
            generateQRCodeIW()
        } else {
            controlViewModel.dataWasRetrievedForActivityPublic.observe(this,
                androidx.lifecycle.Observer<Boolean> { value ->
                    if(value){
                        generateQRCodeIW()
                    }
                }
            )

            controlViewModel.getDataFromRoom()
        }

    }

    private fun generateQRCodeIW() {
        try{
            val barcodeEncoder = BarcodeEncoder()
            val bitmapQR = barcodeEncoder.encodeBitmap(ControlUsuario.instance.currentUsuarioGeneral!!.codigoAlumnoPre, BarcodeFormat.QR_CODE, 220, 220)

            image_view_for_qr_code.setImageBitmap(bitmapQR)
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
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
