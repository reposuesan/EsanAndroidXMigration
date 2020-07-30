package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_mensajenotificaciones.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Mensaje
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 1/08/18.
 */
class MensajeAdapter(val listaMensajes: List<Mensaje>, val clickListener: (Mensaje, Position: Int) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<MensajeAdapter.MensajeHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeHolder {
        return MensajeHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_mensajenotificaciones, parent, false))
    }

    override fun onBindViewHolder(holder: MensajeHolder, position: Int) {
        holder?.setValues(listaMensajes[position], clickListener)
    }

    override fun getItemCount(): Int {
        return listaMensajes.size
    }

    fun actualizarMensajeComoLeido(position: Int) {
        listaMensajes[position].noLeido = false
        notifyItemChanged(position)
    }

    class MensajeHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValues(mensaje: Mensaje, clickListener: (Mensaje, Int) -> Unit) {
            itemView.lblFecha_mensaje.text = mensaje.fecha
            itemView.lblTitulo_mensaje.text = mensaje.titulo
            itemView.lblMensaje_mensaje.text = mensaje.mensaje

            if (mensaje.noLeido) {
                itemView.lblTitulo_mensaje.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)
                itemView.lblMensaje_mensaje.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)
            } else {
                itemView.lblTitulo_mensaje.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
                itemView.lblMensaje_mensaje.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
            }

            itemView.setOnClickListener { clickListener(mensaje, adapterPosition) }
        }
    }
}