package pe.edu.esan.appostgrado.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_horario_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Horario
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 24/04/18.
 */
class HorarioAdapter(val listaSemana: ArrayList<ArrayList<String>>, val hoy: Int, val context: Context, val clickListener: (Int) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<HorarioAdapter.HorarioHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioHolder {

        return HorarioHolder(LayoutInflater.from(context).inflate(R.layout.item_horario_lista, parent, false))
    }

    override fun onBindViewHolder(holder: HorarioHolder, position: Int) {
        holder?.lblHoy?.typeface = Utilitarios.getFontRoboto(context, Utilitarios.TypeFont.THIN)
        holder?.lblDiaSemana?.typeface = Utilitarios.getFontRoboto(context, Utilitarios.TypeFont.REGULAR)

        if (position == hoy) {
            holder?.lblHoy?.visibility = View.VISIBLE
            holder?.viewIndicador?.visibility = View.VISIBLE
            holder?.lblDiaSemana?.setTextColor(ContextCompat.getColor(context, R.color.esan_rojo))
        } else {
            holder?.lblHoy?.visibility = View.GONE
            holder?.viewIndicador?.visibility = View.INVISIBLE
            holder?.lblDiaSemana?.setTextColor(ContextCompat.getColor(context, R.color.md_black_1000))
        }

        holder?.lblDiaSemana?.text = listaSemana[position][1]
        holder?.onclick(clickListener)
    }

    override fun getItemCount(): Int {
        return listaSemana.size
    }

    class HorarioHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val lblHoy = view.lblHoy_ihorario
        val lblDiaSemana = view.lblDia_ihorario
        val viewIndicador = view.vIndicador_ihorario

        fun onclick(clickListener: (Int) -> Unit){
            itemView.setOnClickListener { clickListener(layoutPosition) }
        }
    }
}