package pe.edu.esan.appostgrado.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_prhorario_grilla.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.PRHorarioReserva
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 16/05/18.
 */
class PRHorarioAdapter(val context: Context, val listaHorario: List<PRHorarioReserva>, val clickListener: (PRHorarioReserva, View, Int, List<PRHorarioReserva>) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<PRHorarioAdapter.PRHorarioHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PRHorarioHolder {
        return PRHorarioHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_prhorario_grilla, parent, false))
    }

    override fun onBindViewHolder(holder: PRHorarioHolder, position: Int) {
        holder.setValores(listaHorario[position], listaHorario , clickListener)
    }

    override fun getItemCount(): Int {
        return listaHorario.size
    }

    class PRHorarioHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(horario: PRHorarioReserva,lista: List<PRHorarioReserva>, clickListener: (PRHorarioReserva, View, Int, List<PRHorarioReserva>) -> Unit) {

            itemView.lblHInicio_itemprhorario.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblHFin_itemprhorario.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblLinea_itemprhorario.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

            itemView.lblHInicio_itemprhorario.text = horario.horainicio
            itemView.lblHFin_itemprhorario.text = horario.horafin

            if (horario.seleccionado) {
                itemView.viewHorario_itemprhorario.background = ContextCompat.getDrawable(itemView.context, R.drawable.borde_radiohorario_select)
                itemView.lblHInicio_itemprhorario.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                itemView.lblHFin_itemprhorario.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                itemView.lblLinea_itemprhorario.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
            } else {
                itemView.viewHorario_itemprhorario.background = ContextCompat.getDrawable(itemView.context, R.drawable.borde_radiohorario)
                itemView.lblHInicio_itemprhorario.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_black_1000))
                itemView.lblHFin_itemprhorario.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_black_1000))
                itemView.lblLinea_itemprhorario.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_black_1000))
            }

            itemView.setOnClickListener { clickListener(horario, itemView , adapterPosition, lista) }
        }
    }
}