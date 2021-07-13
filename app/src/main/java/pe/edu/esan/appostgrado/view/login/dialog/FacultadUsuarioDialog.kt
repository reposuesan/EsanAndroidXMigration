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
class FacultadUsuarioDialog: androidx.fragment.app.DialogFragment() {

    var click : (opcion: String) -> Unit by Delegates.notNull()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater

        val viewDialog = inflater.inflate(R.layout.dialog_perfil_usuario, null, false)

        viewDialog.lblTitulo_login.text = resources.getString(R.string.elegir_facultar)
        viewDialog.lblTitulo_login.typeface = Utilitarios.getFontRoboto(requireContext(), Utilitarios.TypeFont.LIGHT)

        val facultad = resources.getStringArray(R.array.facultad)

        viewDialog.btnAlumnoOPre_login.text = facultad[0]
        viewDialog.btnDocenteOPost_login.text = facultad[1]

        viewDialog.btnAlumnoOPre_login.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_login_rojo)
        viewDialog.btnDocenteOPost_login.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_login_rojo)
        viewDialog.btnAlumnoOPre_login.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
        viewDialog.btnDocenteOPost_login.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))

        viewDialog.btnAlumnoOPre_login.setOnClickListener {
            click(Utilitarios.PRE)
        }

        viewDialog.btnDocenteOPost_login.setOnClickListener {
            click(Utilitarios.POS)
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