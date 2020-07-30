package pe.edu.esan.appostgrado.adapter

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_mallapre_detalle.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.CursosPre
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 22/05/18.
 */
class CursoPreMallaAdapter (val listaCursosPre: ArrayList<CursosPre>, val clickListener: (CursosPre) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<CursoPreMallaAdapter.CursoPreMallaHolder>() {

    override fun onBindViewHolder(holder: CursoPreMallaHolder, position: Int) {
        holder?.setValores(listaCursosPre[position], clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoPreMallaHolder {
        return CursoPreMallaHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_mallapre_detalle, parent, false))
    }

    override fun getItemCount(): Int {
        return listaCursosPre.size
    }

    class CursoPreMallaHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(curso: CursosPre, clickListener: (CursosPre) -> Unit) {
            itemView.lblCredito_mallapredetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblCurso_mallapredetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblPeriodo_mallapredetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN_ITALIC)
            itemView.lblNota_mallapredetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)

            itemView.lblCredito_mallapredetalle.text = String.format(itemView.resources.getString(R.string.cred_), curso.creditos)
            itemView.lblCurso_mallapredetalle.text = curso.nombreCurso
            itemView.lblPeriodo_mallapredetalle.text = "${curso.periodo} (${curso.veces})"

            if (curso.estado == "APROBADO") {
                itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.borde_curso_aprobado)
                itemView.lblNota_mallapredetalle.text = curso.promedio
                //itemView.imgAprobado_mallapredetalle.visibility = View.VISIBLE
            } else if (curso.estado == "CONVALIDADO") {
                itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.borde_curso_convalidado)
                itemView.lblNota_mallapredetalle.text = itemView.resources.getString(R.string.convalidado)
                //itemView.imgAprobado_mallapredetalle.visibility = View.VISIBLE
            } else {
                itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.borde_curso_pendiente)
                itemView.lblNota_mallapredetalle.text = "-"
                //itemView.imgAprobado_mallapredetalle.visibility = View.GONE
            }

            if (curso.listaCursoRequisito.size > 0) {
                itemView.imgDetalle_mallapredetalle.visibility = View.VISIBLE
                itemView.setOnClickListener { clickListener(curso) }
            } else {
                itemView.imgDetalle_mallapredetalle.visibility = View.GONE
                itemView.setOnClickListener { }
            }

            itemView.imgFlag_mallapredetalle.visibility = View.GONE //if (curso.esIngles) View.VISIBLE else View.GONE
        }
    }
}