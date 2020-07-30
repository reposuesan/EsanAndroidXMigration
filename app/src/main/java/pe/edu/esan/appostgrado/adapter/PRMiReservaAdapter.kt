package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_prmisreservas_cabecera.view.*
import kotlinx.android.synthetic.main.item_prmisreservas_detalle.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.PRMiReserva
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 12/09/18.
 */
class PRMiReservaAdapter(val listaMisReservas: List<PRMiReserva>): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return listaMisReservas[position].tipo
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PRMiReservaCabeceraHolder -> holder.setValores(listaMisReservas[position])
            is PRMiReservaDetalleHolder -> holder.setValores(listaMisReservas[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            CABECERA -> PRMiReservaCabeceraHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_prmisreservas_cabecera, parent, false))
            else -> PRMiReservaDetalleHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_prmisreservas_detalle, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return listaMisReservas.size
    }

    class PRMiReservaCabeceraHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(reserva: PRMiReserva) {
            itemView.lblFecha_iMisReservas.text = reserva.fecha
        }
    }

    class PRMiReservaDetalleHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(reserva: PRMiReserva) {
            itemView.lblHora_iMisReservas.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.BOLD)
            itemView.lblAmbiente_iMisReservas.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

            itemView.lblHora_iMisReservas.text = "${reserva.horaInicio} - ${reserva.horaFin}"
            itemView.lblEstado_iMisReservas.text = reserva.estado
            itemView.lblAmbiente_iMisReservas.text = "${reserva.cubiculo}, ${reserva.descripcion}"
        }
    }

    private val CABECERA = 1
    private val DETALLE = 2
}