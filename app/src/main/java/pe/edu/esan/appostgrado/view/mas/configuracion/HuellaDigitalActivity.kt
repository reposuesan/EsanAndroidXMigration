@file:Suppress("DEPRECATION")

package pe.edu.esan.appostgrado.view.mas.configuracion

import android.Manifest
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_huella_digital.*
import kotlinx.android.synthetic.main.toolbar_titulo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.mas.configuracion.fingerprint.FingerprintAuthenticationDialogFragment
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

class HuellaDigitalActivity : AppCompatActivity(), FingerprintAuthenticationDialogFragment.FingerprintResults {

    private val DIALOG_FRAGMENT_TAG = "myFragment"
    private var fingerprintManager: FingerprintManager? = null
    private var keyguardManager: KeyguardManager? = null
    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private val KEY_NAME = "FINGER_KEY"
    private var cipher: Cipher? = null
    private var cryptoObject: FingerprintManager.CryptoObject? = null

    private var mSharedPreferences: SharedPreferences? = null

    private var LOG = HuellaDigitalActivity::class.simpleName

    private var fingerprintActionWasSuccessful = false
    private var fingerprintActionCancelByUser = false
    private var fingerprintActionError = false

    private var fingerFragmentDialog: FingerprintAuthenticationDialogFragment? = null

    private var activityVisible: Boolean = false

    @TargetApi(Build.VERSION_CODES.M)
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

        if (swActivar_huelladigital.isChecked) {
            lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_habilitada)
            fingerprintActionWasSuccessful = true
        } else {
            lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_deshabilitada)
            fingerprintActionWasSuccessful = false
        }

        swActivar_huelladigital.isEnabled = getManagers()

        swActivar_huelladigital.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (!isChecked) {
                fingerprintActionWasSuccessful = false
            }

            if (isChecked) {
                if(!fingerprintActionWasSuccessful) {
                    if (getManagers()) {
                        generateKey()
                        if (cipherInit()) {
                            cipher?.let {
                                cryptoObject = FingerprintManager.CryptoObject(it)
                            }

                            if (fingerprintManager != null && cryptoObject != null) {
                                fingerFragmentDialog = FingerprintAuthenticationDialogFragment()

                                /*fingerFragmentDialog?.setCryptoObject(cryptoObject)*/
                                fingerFragmentDialog?.let { it.setCryptoObject(cryptoObject) }

                                /*fingerFragmentDialog?.setFingerprintResultsInterface(this)*/
                                fingerFragmentDialog?.let { it.setFingerprintResultsInterface(this) }

                                val useFingerprintPreference =
                                    mSharedPreferences?.getBoolean("use_fingerprint_to_authenticate_key", true)

                                if (useFingerprintPreference!!) {
                                    /*fingerFragmentDialog?.setStage(FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT)*/
                                    fingerFragmentDialog?.let { it.setStage(FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT) }
                                } else {
                                    /*fingerFragmentDialog?.setStage(FingerprintAuthenticationDialogFragment.Stage.PASSWORD)*/
                                    fingerFragmentDialog?.let { it.setStage(FingerprintAuthenticationDialogFragment.Stage.PASSWORD) }
                                }

                                /*fingerFragmentDialog?.show(fragmentManager, DIALOG_FRAGMENT_TAG)*/
                                fingerFragmentDialog?.show(fragmentManager, DIALOG_FRAGMENT_TAG)
                            }
                            /*checkDialogFragment()*/
                        }
                    }
                }
            } else {
                val preferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
                val edit = preferencias.edit()
                edit.putBoolean("touchid", false)
                edit.putString("userWithFingerprint", "")
                edit.putString("passwordWithFingerprint", "")
                edit.putString("tipoperfilWithFingerprint", "")
                edit.apply()

                if (fingerprintActionCancelByUser) {
                    /*lblMensaje_huelladigital.text = resources.getString(R.string.huella_cancelado_usuario)*/
                    lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_deshabilitada)
                } else if (fingerprintActionError) {
                    lblMensaje_huelladigital.text = resources.getString(R.string.huella_error_lector)
                } else if (!fingerprintActionWasSuccessful) {
                    lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_deshabilitada)
                }

            }
        }
    }

    /*private fun checkDialogFragment(){
        if (fingerprintManager != null && cryptoObject != null) {
            fingerFragmentDialog = FingerprintAuthenticationDialogFragment()
            fingerFragmentDialog?.setCryptoObject(cryptoObject)
            fingerFragmentDialog?.setFingerprintResultsInterface(this)

            val useFingerprintPreference =
                mSharedPreferences?.getBoolean("use_fingerprint_to_authenticate_key", true)
            if (useFingerprintPreference!!) {
                fingerFragmentDialog?.setStage(FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT)
            } else {
                fingerFragmentDialog?.setStage(FingerprintAuthenticationDialogFragment.Stage.PASSWORD)
            }
            fingerFragmentDialog?.show(fragmentManager, DIALOG_FRAGMENT_TAG)
            }

    }*/


    @TargetApi(Build.VERSION_CODES.M)
    private fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        }

        try {
            keyStore?.load(null)
            keyGenerator?.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()
            )
            keyGenerator?.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun cipherInit(): Boolean {
        try {
            cipher =
                Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore?.load(null)
            val key = keyStore?.getKey(KEY_NAME, null) as SecretKey
            cipher?.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun getManagers(): Boolean {
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        /*fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager*/
        if (getSystemService(Context.FINGERPRINT_SERVICE) != null) {
            //fingerprintManager  = getSystemService(FingerprintManager::class.java)
            fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
        } else {
            lblMensaje_huelladigital.text = getString(R.string.ocurrio_error_sensor_huella_digital)
            return false
        }

        if (fingerprintManager?.isHardwareDetected == false) {
            //swActivar_huelladigital.isEnabled = false
            lblMensaje_huelladigital.text = resources.getString(R.string.huella_no_reconocimiento)
            //lblMensaje_huelladigital.text = getString(R.string.ocurrio_error_sensor_huella_digital)
            return false
        }

        if (keyguardManager?.isKeyguardSecure == false) {
            //swActivar_huelladigital.isEnabled = false
            lblMensaje_huelladigital.text = resources.getString(R.string.huella_configure_bloqueo)
            return false
        }

        if (fingerprintManager?.hasEnrolledFingerprints() == false) {
            //swActivar_huelladigital.isEnabled = false
            lblMensaje_huelladigital.text = resources.getString(R.string.huella_configure_una_huella)
            return false
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.USE_FINGERPRINT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //swActivar_huelladigital.isEnabled = false
            lblMensaje_huelladigital.text = resources.getString(R.string.huella_no_permiso)
            return false
        }

        return true
    }


    override fun onErrorFingerprint(errorId: Int) {
        if (errorId != FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
            fingerprintActionWasSuccessful = false
            fingerprintActionCancelByUser = false
            fingerprintActionError = true
        }
        swActivar_huelladigital.isChecked = false
        /*if(fingerFragmentDialog != null) {
            fingerFragmentDialog?.dismiss()
        }*/
    }

    override fun onCancel() {
        fingerprintActionWasSuccessful = false
        fingerprintActionCancelByUser = true
        fingerprintActionError = false
        swActivar_huelladigital.isChecked = false
        /*if(fingerFragmentDialog != null){
            fingerFragmentDialog?.dismiss()
        }*/
    }

    override fun onPurchased(withFingerprint: Boolean, cryptopObject: FingerprintManager.CryptoObject?) {
        if (withFingerprint) {
            lblMensaje_huelladigital.text = resources.getString(R.string.huella_digital_habilitada)
            val preferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
            val edit = preferencias.edit()
            edit.putBoolean("touchid", true)
            edit.putString("userWithFingerprint", preferencias.getString("code", ""))
            edit.putString("passwordWithFingerprint", preferencias.getString("password", ""))
            edit.putString("tipoperfilWithFingerprint", preferencias.getString("tipoperfil", ""))
            edit.apply()
            fingerprintActionWasSuccessful = true
            fingerprintActionCancelByUser = false
            fingerprintActionError = false
            swActivar_huelladigital.isChecked = true
            /*if(fingerFragmentDialog != null) {
                fingerFragmentDialog?.dismiss()
            }*/
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

    override fun onPause() {
        activityVisible = false
        super.onPause()

    }

    override fun onResume() {
        super.onResume()
        activityVisible = true
    }

    fun checkActivityVisibility(): Boolean {
        return activityVisible
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    /*override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }*/
}
