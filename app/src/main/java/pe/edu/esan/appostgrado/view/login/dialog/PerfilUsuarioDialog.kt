package pe.edu.esan.appostgrado.view.login.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_perfil_usuario.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.util.Utilitarios
import kotlin.properties.Delegates

/**
 * Created by lventura on 16/08/18.
 */
class PerfilUsuarioDialog : androidx.fragment.app.DialogFragment() {

    var click : (opcion: String) -> Unit by Delegates.notNull()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        val inflater = activity!!.layoutInflater

        val viewDialog = inflater.inflate(R.layout.dialog_perfil_usuario, null, false)

        viewDialog.lblTitulo_login.text = resources.getString(R.string.elegir_perfil)
        viewDialog.lblTitulo_login.typeface = Utilitarios.getFontRoboto(context!!, Utilitarios.TypeFont.LIGHT)

        val perfil = resources.getStringArray(R.array.perfiles)

        viewDialog.btnAlumnoOPre_login.text = perfil[0]
        viewDialog.btnDocenteOPost_login.text = perfil[1]

        viewDialog.btnAlumnoOPre_login.background = ContextCompat.getDrawable(context!!, R.drawable.btn_login_blanco)
        viewDialog.btnDocenteOPost_login.background = ContextCompat.getDrawable(context!!, R.drawable.btn_login_blanco)
        viewDialog.btnAlumnoOPre_login.setTextColor(ContextCompat.getColor(context!!, R.color.esan_rojo))
        viewDialog.btnDocenteOPost_login.setTextColor(ContextCompat.getColor(context!!, R.color.esan_rojo))

        viewDialog.btnAlumnoOPre_login.setOnClickListener {
            click(Utilitarios.ALU)
        }

        viewDialog.btnDocenteOPost_login.setOnClickListener {
            click(Utilitarios.DOC)
        }

        builder.setView(viewDialog)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog
    }

    fun onClickOptionCustomListener (valor : (opcion: String) -> Unit) {
        click = valor
    }

}