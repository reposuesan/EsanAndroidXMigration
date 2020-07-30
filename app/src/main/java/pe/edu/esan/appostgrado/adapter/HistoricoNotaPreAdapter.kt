package pe.edu.esan.appostgrado.adapter

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_historiconotapre_cabecera.view.*
import kotlinx.android.synthetic.main.item_historiconotapre_detalle.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.CursosPre
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 31/05/18.
 */
class HistoricoNotaPreAdapter(val listaCursosPre: List<CursosPre>): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HistoricoNotaPreCabeceraHolder -> {
                holder.setValores(listaCursosPre[position])
            }
            is HistoricoNotaPreDetalleHolder -> {
                holder.setValores(listaCursosPre[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == 1) {
            HistoricoNotaPreCabeceraHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_historiconotapre_cabecera, parent, false))
        } else {
            HistoricoNotaPreDetalleHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_historiconotapre_detalle, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (listaCursosPre[position].esCabecera) 1 else 0
    }

    override fun getItemCount(): Int {
        return listaCursosPre.size
    }

    class HistoricoNotaPreCabeceraHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(curso: CursosPre) {
            itemView.lblSCurso_histonotapre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
            itemView.lblCiclo_histonotapre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblSPromedio_histonotapre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
            itemView.lblSCredito_histonotapre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
            itemView.lblSPorGlobal_histonotapre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
            itemView.lblCredito_histonotapre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblPromedio_histonotapre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

            itemView.lblCiclo_histonotapre.text = curso.proceso
            itemView.lblCredito_histonotapre.text = curso.credito
            itemView.lblPromedio_histonotapre.text = curso.promedio
        }
    }

    class HistoricoNotaPreDetalleHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(curso: CursosPre) {
            itemView.lblCurso_histonotapre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblNota_histonotapre.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)

            itemView.lblCurso_histonotapre.text = curso.nombreCurso
            when (curso.estado) {
                "RETIRADO" -> {
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                    itemView.lblCurso_histonotapre.setTextColor(ContextCompat.getColor(itemView.context, R.color.tardanza))
                    itemView.lblNota_histonotapre.setTextColor(ContextCompat.getColor(itemView.context, R.color.tardanza))
                    itemView.lblNota_histonotapre.text = itemView.resources.getString(R.string.retirado)
                    itemView.lblNota_histonotapre.textSize = 13.0f
                }
                "APROBADO" -> {
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                    itemView.lblCurso_histonotapre.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_grey_800))
                    itemView.lblNota_histonotapre.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_grey_900))
                    itemView.lblNota_histonotapre.text = curso.promedio
                    itemView.lblNota_histonotapre.textSize = 16.0f
                }
                else -> {
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.rojo_muyclaro))
                    itemView.lblCurso_histonotapre.setTextColor(ContextCompat.getColor(itemView.context, R.color.falta))
                    itemView.lblNota_histonotapre.setTextColor(ContextCompat.getColor(itemView.context, R.color.falta))
                    itemView.lblNota_histonotapre.text = curso.promedio
                    itemView.lblNota_histonotapre.textSize = 16.0f
                }
            }

            itemView.imgIngles_histonotapre.visibility = View.GONE //if (curso.esIngles) View.VISIBLE else View.GONE

        }
    }
}