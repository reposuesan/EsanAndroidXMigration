package pe.edu.esan.appostgrado.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_opcion_salir.view.*
import kotlinx.android.synthetic.main.item_opciones_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.MasOpcion
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 2/05/18.
 */
class MasOpcionAdapter(val listaOpciones: List<MasOpcion>, val invitado: Boolean, val clickListener: (MasOpcion?) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>(){

    override fun getItemViewType(position: Int): Int {
        return if (position == listaOpciones.size) { 0 } else { 1 }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == 1 )
            MasOpcionHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_opciones_lista, parent, false))
        else
            SalirOpcionHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_opcion_salir, parent, false))
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MasOpcionHolder -> {
                holder.setValores(listaOpciones[position], clickListener)
            }

            is SalirOpcionHolder -> {
                holder.setValores(invitado, clickListener)
            }
        }
    }

    override fun getItemCount(): Int {
        return listaOpciones.size + 1
    }

    fun actualizarCantidadMensajes(cantidad : Int){
        val index = listaOpciones.withIndex().filter { it.value.id == 1 }.map { it.index }

        for (i in index) {
            listaOpciones[i].cantidadNotificacion = cantidad
            notifyItemChanged(i)
        }
    }

    class MasOpcionHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(opcion: MasOpcion, clickListener: (MasOpcion?) -> Unit) {

            itemView.lblOpciones_imas.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)
            itemView.lblDetalle_imas.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblOpciones_imas.text = opcion.titulo
            itemView.lblDetalle_imas.text = opcion.detalle
            itemView.imgIcono_imas.setImageDrawable(opcion.icono)

            if (opcion.cantidadNotificacion ?: 0 > 0) {
                itemView.lblNotificacion_imas.visibility = View.VISIBLE
                itemView.lblNotificacion_imas.text = (opcion.cantidadNotificacion ?: 0).toString()
            } else {
                itemView.lblNotificacion_imas.visibility = View.GONE
            }

            itemView.setOnClickListener { clickListener(opcion) }
        }
    }

    class SalirOpcionHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(invitado: Boolean, clickListener: (MasOpcion?) -> Unit) {
            if (invitado)
                itemView.lblSalir_imas.text = itemView.resources.getString(R.string.salir)
            else
                itemView.lblSalir_imas.text = itemView.resources.getString(R.string.cerrar_sesion)

            itemView.setOnClickListener { clickListener(null) }
        }
    }
}