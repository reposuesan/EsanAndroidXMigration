package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_pagopost.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.PagoPost
import pe.edu.esan.appostgrado.util.Utilitarios
import java.util.ArrayList

/**
 * Created by lventura on 27/06/18.
 */
class PagoPostAdapter(val listaPagoPost: ArrayList<PagoPost>): androidx.recyclerview.widget.RecyclerView.Adapter<PagoPostAdapter.PagoPostHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagoPostHolder {
        return PagoPostHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_pagopost, parent, false))
    }

    override fun onBindViewHolder(holder: PagoPostHolder, position: Int) {
        holder?.setValores(listaPagoPost[position])
    }

    override fun getItemCount(): Int {
        return listaPagoPost.size
    }

    class PagoPostHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(pago: PagoPost) {
            itemView.lblSFechavencimiento_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblSDiasvencimiento_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblSImporte_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblSPenalidad_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblSInteres_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblSTotal_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

            itemView.lblFechavencimiento_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblDiasvencimiento_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblImporte_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblPenalidad_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblInteres_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblTotal_pagopost.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

            itemView.lblFechavencimiento_pagopost.text = pago.vencimiento
            itemView.lblDiasvencimiento_pagopost.text = "${pago.diasvencimiento}"
            itemView.lblImporte_pagopost.text = pago.importe
            itemView.lblPenalidad_pagopost.text = pago.penalidad
            itemView.lblInteres_pagopost.text = pago.interes
            itemView.lblTotal_pagopost.text = pago.total
        }
    }
}