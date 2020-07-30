package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_mallapre_cabecera.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.CursosPre
import pe.edu.esan.appostgrado.entidades.MallaPre
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 22/05/18.
 */
class MallaPreAdapter(val listaMallaPre: ArrayList<MallaPre>, val clickListener: (CursosPre) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<MallaPreAdapter.MallaPreHolder>() {

    override fun onBindViewHolder(holder: MallaPreHolder, position: Int) {
        holder?.setValores(listaMallaPre[position], clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MallaPreHolder {
        return MallaPreHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_mallapre_cabecera, parent, false))
    }

    override fun getItemCount(): Int {
        return listaMallaPre.size
    }

    class MallaPreHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(malla: MallaPre, clickListener: (CursosPre) -> Unit) {
            itemView.lblCiclo_mallaprecabecera.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblCiclo_mallaprecabecera.text = malla.modulo

            val adapterCurso = CursoPreMallaAdapter(malla.cursos, {cursosPre -> clickListener(cursosPre) })
            itemView.rvCurso_mallaprecabecera.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(
                    itemView.context,
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                    false
                )
            itemView.rvCurso_mallaprecabecera.adapter = adapterCurso
        }
    }
}