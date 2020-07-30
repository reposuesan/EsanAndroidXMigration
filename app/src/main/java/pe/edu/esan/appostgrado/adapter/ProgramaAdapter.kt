package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_cursopre_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Programa
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 3/05/18.
 */
class ProgramaAdapter(val listPrograma: List<Programa>, val clickListener: (Programa) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<ProgramaAdapter.ProgramaHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramaHolder {
        return ProgramaHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_cursopre_lista, parent, false))
    }

    override fun onBindViewHolder(holder: ProgramaHolder, position: Int) {
        holder?.setValores(listPrograma[position], clickListener)
    }

    override fun getItemCount(): Int {
        return listPrograma.size
    }

    class ProgramaHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(programa: Programa, clickListener: (Programa) -> Unit) {
            itemView.lblCurso_icurso.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblCurso_icurso.text = programa.nombre
            itemView.setOnClickListener { clickListener(programa) }
        }
    }
}