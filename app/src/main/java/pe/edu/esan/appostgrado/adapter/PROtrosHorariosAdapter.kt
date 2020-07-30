package pe.edu.esan.appostgrado.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.util.Util
import kotlinx.android.synthetic.main.item_prhorariootrocabecera_lista.view.*
import kotlinx.android.synthetic.main.item_prhorariootrodetalle_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.PRHorarioOtro
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 17/05/18.
 */
class PROtrosHorariosAdapter(val context: Context, val listaHorario: ArrayList<PRHorarioOtro>, val clickListener: (tipo: Int, horario: PRHorarioOtro?) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == 0) {
            CabeceraViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_prhorariootrocabecera_lista, parent, false))
        } else {
            OtroHorarioViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_prhorariootrodetalle_lista, parent, false))
        }
    }

    fun addHorarios(horarios: List<PRHorarioOtro>) {
        listaHorario.addAll(horarios)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CabeceraViewHolder -> {
                holder.setValores(clickListener)
            }
            is OtroHorarioViewHolder -> {
                holder.setValores(listaHorario[position-1], clickListener)
            }
        }
    }

    override fun getItemCount(): Int {
        return listaHorario.size + 1
    }

    class CabeceraViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores (clickListener: (tipo: Int, horario: PRHorarioOtro?) -> Unit) {
            itemView.lblSOtrosHorarios_itemprhoraotra.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.setOnClickListener { clickListener(0, null) }
        }
    }

    class OtroHorarioViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores (horario: PRHorarioOtro, clickListener: (tipo: Int, horario: PRHorarioOtro?) -> Unit) {
            itemView.lblHora_itemprhoraotra.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
            itemView.lblTiempo_itemprhoraotra.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)

            itemView.lblHora_itemprhoraotra.text = "${horario.horaInicio} - ${horario.horaFin}"
            itemView.lblTiempo_itemprhoraotra.text = String.format(itemView.context.resources.getString(R.string._horas), horario.cantHoras)

            itemView.setOnClickListener { clickListener(1, horario) }
        }
    }
}