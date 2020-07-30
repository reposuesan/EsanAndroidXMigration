package pe.edu.esan.appostgrado.control

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.widget.TextView
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 24/05/18.
 */
class CustomDialog private constructor(){

    private val LOG = CustomDialog::class.simpleName

    init {
        Log.d(LOG,"This ($this) is a singleton dialog")
    }

    private object Holder {
        val INSTANCE = CustomDialog()
    }

    companion object {
        val instance: CustomDialog by lazy { Holder.INSTANCE }
    }

    var dialogoCargando: AlertDialog? = null

    fun showDialogLoad(context: Context) {

        if(dialogoCargando == null) {
            dialogoCargando = AlertDialog.Builder(context)
                .setView(R.layout.item_cargando)
                .setCancelable(false)
                .create()

            dialogoCargando?.show()
            dialogoCargando?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogoCargando?.findViewById<TextView>(R.id.lblCargando_icarga)?.typeface = Utilitarios.getFontRoboto(context, Utilitarios.TypeFont.LIGHT)
        } else {
            Log.w(LOG, "CustomDialog is: ${dialogoCargando.toString()}, so it is not null, we are using a previous dialog")
        }
    }
}