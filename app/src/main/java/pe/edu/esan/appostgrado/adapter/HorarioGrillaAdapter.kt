package pe.edu.esan.appostgrado.adapter

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_horario_grilla.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.HorarioGrilla

/**
 * Created by lventura on 20/08/18.
 */
class HorarioGrillaAdapter(val listaHorario: List<HorarioGrilla>): androidx.recyclerview.widget.RecyclerView.Adapter<HorarioGrillaAdapter.GrillaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrillaViewHolder {
        return GrillaViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_horario_grilla, parent, false))
    }

    override fun onBindViewHolder(holder: GrillaViewHolder, position: Int) {
        holder?.setValues(listaHorario[position])
    }

    override fun getItemCount(): Int {
        return listaHorario.size
    }

    class GrillaViewHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValues(horario: HorarioGrilla) {
            itemView.lblValueInferior_rowhorariogrilla.text = horario.valorInferior
            /**LINEA ROJA (HORA ACTUAL)*/

            if (horario.actual) {
                val positiony: Float
                //Log.e("HorarioGrillaAdapter", "Minuto de Horario: ${horario.minuto}")
                if (horario.minuto >= 50) {
                    //positiony = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 69f, itemView.context.resources.displayMetrics)
                    positiony = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 99f, itemView.context.resources.displayMetrics)
                } else {
                    //positiony = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, horario.minuto * 7.0f / 5.0f, itemView.context.resources.displayMetrics)
                    positiony = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, horario.minuto * 10.0f / 5.0f, itemView.context.resources.displayMetrics)
                }
                itemView.viewHorario_rowhorariogrilla.y = positiony
                itemView.viewHorario_rowhorariogrilla.visibility = View.VISIBLE
            } else {
                itemView.viewHorario_rowhorariogrilla.visibility = View.GONE
            }

            when (horario.tipo) {
                0 -> {
                    itemView.lblValueSuperior_rowhorariogrilla.setBackgroundResource(0)
                    itemView.lblValueInferior_rowhorariogrilla.setBackgroundResource(0)
                    itemView.setBackgroundResource(R.drawable.borde_horario_hora)
                    itemView.lblValueInferior_rowhorariogrilla.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                    itemView.lblValueInferior_rowhorariogrilla.textSize = 13f
                    itemView.lblValueSuperior_rowhorariogrilla.visibility = View.GONE
                    itemView.viewDivisor_rowhorariogrilla.visibility = View.GONE
                }
                1 -> {
                    itemView.lblValueSuperior_rowhorariogrilla.setBackgroundResource(0)
                    itemView.lblValueInferior_rowhorariogrilla.setBackgroundResource(0)
                    itemView.setBackgroundResource(R.drawable.borde_horario)
                    itemView.lblValueInferior_rowhorariogrilla.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_black_1000))
                    itemView.lblValueInferior_rowhorariogrilla.textSize = 8f
                    itemView.lblValueSuperior_rowhorariogrilla.visibility = View.GONE
                    itemView.viewDivisor_rowhorariogrilla.visibility = View.GONE
                }
                2 -> {
                    itemView.lblValueInferior_rowhorariogrilla.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_black_1000))
                    itemView.lblValueInferior_rowhorariogrilla.textSize = 8f

                    itemView.lblValueSuperior_rowhorariogrilla.visibility = View.VISIBLE
                    itemView.viewDivisor_rowhorariogrilla.visibility = View.VISIBLE

                    itemView.lblValueSuperior_rowhorariogrilla.text = horario.valorSuperior
                    itemView.lblValueSuperior_rowhorariogrilla.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_black_1000))
                    itemView.lblValueSuperior_rowhorariogrilla.textSize = 8f

                    if (horario.valorSuperior == "") {
                        itemView.lblValueSuperior_rowhorariogrilla.setBackgroundResource(R.drawable.borde_horario_vacio)
                    } else {
                        itemView.lblValueSuperior_rowhorariogrilla.setBackgroundResource(R.drawable.borde_horario)
                    }

                    if (horario.valorInferior == "") {
                        itemView.lblValueInferior_rowhorariogrilla.setBackgroundResource(R.drawable.borde_horario_vacio)
                    } else {
                        itemView.lblValueInferior_rowhorariogrilla.setBackgroundResource(R.drawable.borde_horario)
                    }
                }
                3 -> {
                    itemView.lblValueSuperior_rowhorariogrilla.setBackgroundResource(0)
                    itemView.lblValueInferior_rowhorariogrilla.setBackgroundResource(0)
                    itemView.setBackgroundResource(R.drawable.borde_horario_vacio)
                    itemView.viewDivisor_rowhorariogrilla.visibility = View.GONE
                    itemView.lblValueInferior_rowhorariogrilla.text = ""
                    itemView.lblValueSuperior_rowhorariogrilla.text = ""
                    itemView.lblValueInferior_rowhorariogrilla.visibility = View.VISIBLE
                    itemView.lblValueSuperior_rowhorariogrilla.visibility = View.VISIBLE
                }
            }
        }
    }
}