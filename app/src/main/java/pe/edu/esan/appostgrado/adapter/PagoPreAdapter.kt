package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_pagopre_cabecera.view.*
import kotlinx.android.synthetic.main.item_pagopre_detalle.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.PagoPre
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 27/04/18.
 */
class PagoPreAdapter (val listaPagoPre: ArrayList<PagoPre>, val clickListener: (PagoPre) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        when (listaPagoPre[position].tipo) {
            Utilitarios.TipoFila.CABECERA -> return 1
            else -> return 2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) CabeceraPagoPreHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pagopre_cabecera, parent, false))
            else DetallePagoPreHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pagopre_detalle, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CabeceraPagoPreHolder -> holder.setValores(listaPagoPre[position])
            is DetallePagoPreHolder -> holder.setValores(listaPagoPre[position], clickListener)
        }
    }

    override fun getItemCount(): Int {
        return listaPagoPre.size
    }

    class CabeceraPagoPreHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun setValores(pago: PagoPre) {
            itemView.lblMatricula_ipagopre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)
            itemView.lblConceptoT_ipagopre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblMontoT_ipagopre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblVencimientoT_ipagopre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

            itemView.lblMatricula_ipagopre.text = pago.matricula
        }
    }

    class DetallePagoPreHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(pago: PagoPre, clickListener: (PagoPre) -> Unit) {
            itemView.lblConcepto_ipagopre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblMonto_ipagopre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblFechaVencimiento_ipagopre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

            itemView.lblConcepto_ipagopre.text = pago.concepto
            itemView.lblMonto_ipagopre.text = pago.monto
            itemView.lblFechaVencimiento_ipagopre.text = pago.vencimiento
            itemView.setOnClickListener { clickListener(pago) }
        }
    }
}