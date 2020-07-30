package pe.edu.esan.appostgrado.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.item_agregaralumno_prmigrupo.view.*
import kotlinx.android.synthetic.main.item_participantes_prmigrupo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.puntosreunion.postgrado.crear.PRCrearGrupoActivity

/**
 * Created by lventura on 23/07/18.
 */
class MiGrupoAdapter (var listaAlumno: ArrayList<Alumno>, val puedeEliminarAgregar: Boolean, val cantMaxGrupo: Int, val eliminarListener: (codigo: String, position: Int) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    val CABECERA = 0
    val DETALLE = 1

    override fun getItemViewType(position: Int): Int {
        if (puedeEliminarAgregar && listaAlumno.isNotEmpty()) {
            if ((cantMaxGrupo - listaAlumno.size) > 0) {
                if (position == 0)
                    return CABECERA
            }
        }
        return DETALLE
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        var menosCabecera = 0
        if (puedeEliminarAgregar && listaAlumno.isNotEmpty()) {
            if ((cantMaxGrupo - listaAlumno.size) > 0) {
                menosCabecera = 1
            }
        }

        when (holder) {
            is AgregarAlumnosHolder -> {
                holder.setValores(cantMaxGrupo, listaAlumno.size) { context ->
                    ControlUsuario.instance.currentMiGrupo = listaAlumno
                    val intentCrear = Intent(context, PRCrearGrupoActivity::class.java)
                    context.startActivity(intentCrear)
                }
            }
            is MiGrupoHolder -> {
                holder.setValores(listaAlumno[position-menosCabecera], puedeEliminarAgregar, eliminarListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        when (viewType) {
            CABECERA -> {
                return AgregarAlumnosHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_agregaralumno_prmigrupo, parent, false))
            }
            else -> {
                return MiGrupoHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_participantes_prmigrupo, parent, false))
            }
        }
    }

    override fun getItemCount(): Int {
        if (puedeEliminarAgregar && listaAlumno.isNotEmpty()) {
            if ((cantMaxGrupo - listaAlumno.size) > 0) {
                return listaAlumno.size + 1
            }
        }
        return listaAlumno.size
    }

    fun agregarQuitarEliminar(estado : Boolean) {
        var sumar = 0
        if (puedeEliminarAgregar && listaAlumno.isNotEmpty()) {
            if ((cantMaxGrupo - listaAlumno.size) > 0) {
                sumar = 1
            }
        }

        for (x in 0 until this.listaAlumno.size) {
            this.listaAlumno[x].activarEliminar = estado
            notifyItemChanged(x + sumar)
        }
    }

    fun eliminarAlumnoLista(position: Int) {
        var sumar = 0
        if ((cantMaxGrupo - listaAlumno.size) > 0) {
            sumar = 1
        }

        this.listaAlumno.removeAt(position - sumar)

        if ((cantMaxGrupo - (listaAlumno.size + 1)) > 0) {
            notifyItemChanged(0)
            notifyItemRemoved(position)
        } else {
            notifyDataSetChanged()
        }
    }

    class MiGrupoHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(alumno: Alumno, puedeEliminar: Boolean, eliminarListener: (codigo: String, position: Int) -> Unit) {
            itemView.lblCreador_prmigrupo.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
            itemView.lblNombre_prmigrupo.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

            itemView.lblNombre_prmigrupo.text = alumno.nombreCompleto

            Glide.with(itemView.context)
                    .load(Utilitarios.getUrlFoto(alumno.codigo, 100))
                    .into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            itemView.imgUsuario_prmigrupo.setImageBitmap(Utilitarios.getRoundedCornerDrawable(resource))
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            itemView.imgUsuario_prmigrupo.setImageResource(R.drawable.photo_default)
                        }
                    })

            itemView.lblCreador_prmigrupo.visibility = if (alumno.esCreador) View.VISIBLE else View.GONE

            if (puedeEliminar) {
                if (alumno.activarEliminar) {
                    if (alumno.esCreador) {
                        itemView.btnEliminar_prmigrupo.visibility = View.GONE
                        itemView.btnEliminar_prmigrupo.setOnClickListener {  }
                    } else {
                        itemView.btnEliminar_prmigrupo.visibility = View.VISIBLE
                        itemView.btnEliminar_prmigrupo.setOnClickListener {
                            println("Eliminar")
                            eliminarListener(alumno.codigo, adapterPosition)
                        }
                    }
                } else {
                    itemView.btnEliminar_prmigrupo.visibility = View.GONE
                    itemView.btnEliminar_prmigrupo.setOnClickListener {  }
                }
            } else {
                itemView.btnEliminar_prmigrupo.visibility = View.GONE
                itemView.btnEliminar_prmigrupo.setOnClickListener {  }
            }
        }
    }

    class AgregarAlumnosHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(cantMaxGrupo: Int, totalGrupo: Int, clickListener: (context: Context)-> Unit){
            itemView.lblCabecera_prmigrupo.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblCabecera_prmigrupo.text = String.format(itemView.context.resources.getString(R.string.agregar_integrante_, (cantMaxGrupo - totalGrupo)))

            itemView.setOnClickListener {
                clickListener(itemView.context)
            }
        }
    }
}