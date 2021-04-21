package pe.edu.esan.appostgrado.view.mas.configuracion.pversion


import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_huella_digital.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.util.Utilitarios

@TargetApi(Build.VERSION_CODES.P)
@RequiresApi(Build.VERSION_CODES.P)
class HuellaDigitalActivityPAndUp : AppCompatActivity(), BiometricHelper.BiometricCallback {

    private var mSharedPreferences: SharedPreferences? = null

    private var LOG = HuellaDigitalActivityPAndUp::class.simpleName

    private var fingerprintActionWasSuccessful = false
    private var fingerprintActionError = false

    private var mCancellationSignal = CancellationSignal()

    private lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_huella_digital)

        val toolbar = main_huelladigital as Toolbar
        toolbar.toolbar_title.typeface = Utilitarios.getFontRoboto(applicationContext, Utilitarios.TypeFont.BLACK)
        toolbar.toolbar_title.text = resources.getString(R.string.huella_digital)
        setSupportActionBar(toolbar)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setContentInsetsRelative(0, toolbar.contentInsetStartWithNavigation)

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        lblMensaje_huelladigital.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.LIGHT)
        lblTexto_huelladigital.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR_ITALIC)
        swActivar_huelladigital.typeface = Utilitarios.getFontRoboto(this, Utilitarios.TypeFont.REGULAR)

        val misPreferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
        val agreetouchid = misPreferencias.getBoolean("touchid", false)

        swActivar_huelladigital.isChecked = agreetouchid

        if(swActivar_huelladigital.isChecked){
            lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_habilitada)
            fingerprintActionWasSuccessful = true
        } else {
            lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_deshabilitada)
            fingerprintActionWasSuccessful = false
        }

        swActivar_huelladigital.setOnCheckedChangeListener { compoundButton, isChecked ->

            if(!isChecked){
                fingerprintActionWasSuccessful = false
            }

            if (isChecked) {
                if(!fingerprintActionWasSuccessful) {
                    biometricHelper = BiometricHelper(this)
                    mCancellationSignal = CancellationSignal()
                    displayBiometricPrompt()
                }
            } else {
                val preferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
                val edit = preferencias.edit()
                edit.putBoolean("touchid", false)
                edit.putString("userWithFingerprint", "")
                edit.putString("passwordWithFingerprint", "")
                edit.putString("tipoperfilWithFingerprint", "")
                edit.apply()
                if(!fingerprintActionWasSuccessful && !fingerprintActionError){
                    lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_deshabilitada)
                }
            }
        }
    }

    override fun onAuthenticationSuccesful(result: BiometricPrompt.AuthenticationResult?) {

        lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_habilitada)
        val preferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
        val edit = preferencias.edit()
        edit.putBoolean("touchid", true)
        edit.putString("userWithFingerprint", preferencias.getString("code", ""))
        edit.putString("passwordWithFingerprint", preferencias.getString("password", ""))
        edit.putString("tipoperfilWithFingerprint", preferencias.getString("tipoperfil", ""))
        edit.apply()
        fingerprintActionWasSuccessful = true
        fingerprintActionError = false

        val snack = Snackbar.make(findViewById(android.R.id.content), getString(R.string.opcion_huella_digital_conf_correct) , Snackbar.LENGTH_LONG)
        snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.success))
        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.success_text))
        snack.show()

        swActivar_huelladigital.isChecked = true

    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        fingerprintActionWasSuccessful = false
        lblMensaje_huelladigital.text = helpString.toString()
        fingerprintActionError = false
        swActivar_huelladigital.isChecked = false
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        if(errorCode != BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED && errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED){
            //lblMensaje_huelladigital.text = resources.getString(R.string.huella_error_lector)
            //lblMensaje_huelladigital.text = errString.toString()
            if(errorCode == BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS){
                lblMensaje_huelladigital.text = getString(R.string.porfavor_registre_huella_digital_en_dispositivo)
            } else {
                //lblMensaje_huelladigital.text = resources.getString(R.string.huella_no_reconocida)
                lblMensaje_huelladigital.text = errString.toString()
            }
        } else {
            lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_deshabilitada)
            //lblMensaje_huelladigital.text = errString.toString()
        }
        fingerprintActionWasSuccessful = false
        fingerprintActionError = true
        swActivar_huelladigital.isChecked = false
    }

    override fun onAuthenticationFailed() {
        fingerprintActionWasSuccessful = false
        lblMensaje_huelladigital.text = resources.getString(R.string.huella_no_reconocida)
        fingerprintActionError = true
        swActivar_huelladigital.isChecked = false
    }

    override fun onAuthenticationCancelled() {
        if (!mCancellationSignal.isCanceled) {
            mCancellationSignal.cancel()
        }
        fingerprintActionWasSuccessful = false
        fingerprintActionError = false
        lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_deshabilitada)
        swActivar_huelladigital.isChecked = false
    }

    fun displayBiometricPrompt() {
        BiometricPrompt.Builder(this)
            .setTitle(getString(R.string.valida_huella_title))
            /*.setSubtitle("Huella Digital Subtitle")*/
            .setDescription(getString(R.string.confirma_con_tu_huella_para_continuar))
            .setNegativeButton(getString(R.string.cancelar), mainExecutor, DialogInterface.OnClickListener { dialog, i ->
                onAuthenticationCancelled()
            })
            .build()
            .authenticate(mCancellationSignal, mainExecutor, biometricHelper)
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
