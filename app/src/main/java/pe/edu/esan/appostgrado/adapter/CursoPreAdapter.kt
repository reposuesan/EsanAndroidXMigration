package pe.edu.esan.appostgrado.adapter

import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_cursopre_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.CursosPre
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 26/04/18.
 */
class CursoPreAdapter(val listaCursosPre: ArrayList<CursosPre>, val clickListener: (CursosPre) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<CursoPreAdapter.CursoHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoHolder {
        return CursoHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_cursopre_lista, parent, false))
    }

    override fun onBindViewHolder(holder: CursoHolder, position: Int) {
        holder?.setValores(listaCursosPre[position], Utilitarios.getFontRoboto(holder.itemView.context, Utilitarios.TypeFont.REGULAR), clickListener, position)
    }

    override fun getItemCount(): Int {
        return listaCursosPre.size
    }

    class CursoHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(curso: CursosPre, typeface: Typeface, clickListener: (CursosPre) -> Unit, position: Int) {
            itemView.lblCurso_icurso.typeface = typeface
            itemView.lblCurso_icurso.text = curso.nombreCurso
            itemView.setOnClickListener {
                clickListener(curso)
            }
        }
    }
}