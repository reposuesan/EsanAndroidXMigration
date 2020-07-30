package pe.edu.esan.appostgrado.adapter

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.item_alumno_prmigrupo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 25/07/18.
 */
class CreaGrupoMiGrupoAdapter(var listaMiGrupo: ArrayList<Alumno>): androidx.recyclerview.widget.RecyclerView.Adapter<CreaGrupoMiGrupoAdapter.MiGrupoHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): MiGrupoHolder {
        return MiGrupoHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_alumno_prmigrupo, parent, false))
    }

    override fun onBindViewHolder(holder: MiGrupoHolder, position: Int) {
        holder?.setValores(listaMiGrupo[position])
    }

    override fun getItemCount(): Int {
        return listaMiGrupo.size
    }

    fun agregarAlumno(alumno: Alumno) {
        listaMiGrupo.add(alumno)
        notifyItemInserted(listaMiGrupo.size - 1)
    }

    fun removerAlumno(alumno: Alumno) {
        for (x in 0 until listaMiGrupo.size) {
            if (alumno.codigo == listaMiGrupo[x].codigo) {
                listaMiGrupo.removeAt(x)
                notifyItemRemoved(x)
                break
            }
        }
    }

    class MiGrupoHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(alumno: Alumno) {

            Glide.with(itemView.context)
                    .load(Utilitarios.getUrlFoto(alumno.codigo, 100))
                    .into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            itemView.imgUsuario_prcreamigrupo.setImageBitmap(Utilitarios.getRoundedCornerDrawable(resource))
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            itemView.imgUsuario_prcreamigrupo.setImageResource(R.drawable.photo_default)
                        }
                    })
        }
    }
}