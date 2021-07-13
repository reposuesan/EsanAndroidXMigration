package pe.edu.esan.appostgrado.util

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.widget.TextView
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.dialog_mesagno.view.*
import pe.edu.esan.appostgrado.R
import java.util.*

/**
 * Created by lventura on 28/05/18.
 */
class MesAgnoDialog : androidx.fragment.app.DialogFragment() {

    var clickListener: Click? = null

    interface Click {
        fun fechaselect (mes: String, m: Int, y: Int)
    }

    fun setOnClickListener (listener : Click) {
        clickListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val buider = AlertDialog.Builder(requireActivity())

        val infalter = requireActivity().layoutInflater
        val calendar = Calendar.getInstance()

        val dialog = infalter.inflate(R.layout.dialog_mesagno, null)

        dialog.picker_month.minValue = 1
        dialog.picker_month.maxValue = 12
        dialog.picker_month.displayedValues = resources.getStringArray(R.array.meses)
        dialog.picker_month.value = calendar[Calendar.MONTH] + 1

        dialog.picker_year.minValue = calendar[Calendar.YEAR] - 5
        dialog.picker_year.maxValue = calendar[Calendar.YEAR]
        dialog.picker_year.value = calendar[Calendar.YEAR]

        buider.setTitle(resources.getText(R.string.fecha))
                .setView(dialog)
                .setPositiveButton(resources.getText(R.string.aceptar), DialogInterface.OnClickListener { dialogInterface, i ->

                    clickListener?.fechaselect(dialog.picker_month.displayedValues[dialog.picker_month.value - 1], dialog.picker_month.value, dialog.picker_year.value)
                })
                .setNegativeButton(resources.getText(R.string.cancelar), null)

        return buider.create()
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.esan_rojo))
    }
}