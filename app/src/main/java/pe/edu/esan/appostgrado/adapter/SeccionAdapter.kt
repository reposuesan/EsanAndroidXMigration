package pe.edu.esan.appostgrado.adapter

import android.content.Context
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_seccion_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Seccion
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 26/04/18.
 */
class SeccionAdapter(val listaSeccion: ArrayList<Seccion>, val clickListener: (Seccion) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<SeccionAdapter.SeccionHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeccionHolder {
        return SeccionHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_seccion_lista, parent, false))
    }

    override fun onBindViewHolder(holder: SeccionHolder, position: Int) {
        holder?.setValores(
                listaSeccion[position],
                holder.itemView.context,
                clickListener)
    }

    override fun getItemCount(): Int {
        return listaSeccion.size
    }

    class SeccionHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(seccion: Seccion, context: Context, clickListener: (Seccion) -> Unit) {
            val typefaceSeccion = Utilitarios.getFontRoboto(context, Utilitarios.TypeFont.LIGHT)
            val typefaceCurso = Utilitarios.getFontRoboto(context, Utilitarios.TypeFont.REGULAR)

            itemView.lblSeccion_iseccion.typeface = typefaceSeccion
            itemView.lblCurso_iseccion.typeface = typefaceCurso
            itemView.lblSeccion_iseccion.text = seccion.promocion
            itemView.lblCurso_iseccion.text = " ["+ seccion.seccionCodigo + "] " + seccion.nombreCurso
            itemView.setOnClickListener { clickListener(seccion) }
        }
    }
}