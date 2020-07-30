package pe.edu.esan.appostgrado.view.mas.configuracion.pversion

import android.annotation.TargetApi
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.P)
@TargetApi(Build.VERSION_CODES.P)
class BiometricHelper(callback: BiometricCallback): BiometricPrompt.AuthenticationCallback() {

    private var mCallback = callback

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)
        mCallback.onAuthenticationSuccesful(result)
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        super.onAuthenticationHelp(helpCode, helpString)
        mCallback.onAuthenticationHelp(helpCode, helpString)
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        super.onAuthenticationError(errorCode, errString)
        mCallback.onAuthenticationError(errorCode, errString)
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        mCallback.onAuthenticationFailed()
    }

    interface BiometricCallback {

        fun onAuthenticationSuccesful(result: BiometricPrompt.AuthenticationResult?)
        fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?)
        fun onAuthenticationError(errorCode: Int, errString: CharSequence?)
        fun onAuthenticationFailed()
        fun onAuthenticationCancelled()

    }
}