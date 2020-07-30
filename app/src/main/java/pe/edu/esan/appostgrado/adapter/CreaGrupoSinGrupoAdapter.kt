package pe.edu.esan.appostgrado.adapter

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.item_alumno_prcreagrupo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 25/07/18.
 */
class CreaGrupoSinGrupoAdapter(var listaAlumnosSinGrupo: ArrayList<Alumno>, val clickListener: (alumno: Alumno, position: Int) -> Unit ): androidx.recyclerview.widget.RecyclerView.Adapter<CreaGrupoSinGrupoAdapter.AlumnosSinGrupoHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnosSinGrupoHolder {
        return AlumnosSinGrupoHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_alumno_prcreagrupo, parent, false))
    }

    override fun onBindViewHolder(holder: AlumnosSinGrupoHolder, position: Int) {
        holder?.setValores(listaAlumnosSinGrupo[position], clickListener)
    }

    override fun getItemCount(): Int {
        return listaAlumnosSinGrupo.size
    }

    fun actualizarEstado(position: Int, estado: Boolean) {
        listaAlumnosSinGrupo[position].seleccionado = estado
        notifyItemChanged(position)
    }

    class AlumnosSinGrupoHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(alumno: Alumno, clickListener: (alumno: Alumno, position: Int) -> Unit ) {
            itemView.lblNombre_prcreagrupo.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

            itemView.lblNombre_prcreagrupo.text = alumno.nombreCompleto

            Glide.with(itemView.context)
                    .load(Utilitarios.getUrlFoto(alumno.codigo, 100))
                    .into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            itemView.imgUsuario_prcreagrupo.setImageBitmap(Utilitarios.getRoundedCornerDrawable(resource))
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            itemView.imgUsuario_prcreagrupo.setImageResource(R.drawable.photo_default)
                        }
                    })

            itemView.imgSelect_prcreagrupo.visibility = if (alumno.seleccionado) View.VISIBLE else View.GONE

            itemView.setOnClickListener { clickListener(alumno, adapterPosition) }
        }
    }
}