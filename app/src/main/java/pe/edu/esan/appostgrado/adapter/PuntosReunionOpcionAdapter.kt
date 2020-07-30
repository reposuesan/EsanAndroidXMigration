package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_opciones_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.MasOpcion
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 14/05/18.
 */
class PuntosReunionOpcionAdapter(val listaOpciones: List<MasOpcion>, val clickListener: (MasOpcion) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<PuntosReunionOpcionAdapter.MasOpcionHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasOpcionHolder {
        return MasOpcionHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_opciones_lista, parent, false))
    }

    override fun onBindViewHolder(holder: MasOpcionHolder, position: Int) {
        holder?.setValores(listaOpciones[position], clickListener)
    }

    override fun getItemCount(): Int {
        return listaOpciones.size
    }

    class MasOpcionHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        fun setValores(opcion: MasOpcion, clickListener: (MasOpcion) -> Unit) {
            itemView.lblOpciones_imas.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)
            itemView.lblDetalle_imas.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblOpciones_imas.text = opcion.titulo
            itemView.lblDetalle_imas.text = opcion.detalle
            itemView.imgIcono_imas.setImageDrawable(opcion.icono)
            itemView.setOnClickListener { clickListener(opcion) }
        }

    }

}