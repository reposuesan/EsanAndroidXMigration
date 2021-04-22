package pe.edu.esan.appostgrado.adapter

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.item_usuario.view.*
import kotlinx.android.synthetic.main.item_usuario_multiple.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.entidades.Profesor
import pe.edu.esan.appostgrado.util.Utilitarios
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import pe.edu.esan.appostgrado.entidades.UserEsan


/**
 * Created by lventura on 3/05/18.
 */
class UsuarioAdapter(val listaUsuarios: List<UserEsan>, val clickListener: (UserEsan, Int, View, String) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        private val UNSUARIO = 1
        private val MASUSUARIOS = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (listaUsuarios.size == 1) UNSUARIO else MASUSUARIOS
    }

    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == UNSUARIO)
                UsuarioSimpleHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false))
            else
                UsuarioMultipleHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_usuario_multiple, parent, false))
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UsuarioSimpleHolder -> {
                holder.setValores(listaUsuarios[position], clickListener)
            }
            is UsuarioMultipleHolder -> {
                holder.setValores(listaUsuarios[position], clickListener)
            }
        }
    }

    class UsuarioSimpleHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(usuario: UserEsan, clickListener: (UserEsan, Int, View, String) -> Unit) {
            itemView.lblNombre_imas.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblCorreo_imas.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE) //because file name is always same
                .skipMemoryCache(true)
                .timeout(60000)

            when (usuario) {
                is Alumno -> {

                    Glide.with(itemView.context)
                            .load(Utilitarios.getUrlFoto(usuario.codigo, 140))
                            .apply(requestOptions)
                            .into(object : SimpleTarget<Drawable>() {
                                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                    itemView.imgUsuario_imas.setImageBitmap(Utilitarios.getRoundedCornerDrawable(resource))
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    itemView.imgUsuario_imas.setImageResource(R.drawable.photo_default)
                                }
                            })
                    itemView.lblNombre_imas.text = usuario.nombreCompleto
                    itemView.lblCorreo_imas.text = usuario.correo
                }
                is Profesor -> {
                    Glide.with(itemView.context)
                            .load(Utilitarios.getUrlFoto(usuario.codigo, 140))
                            .apply(requestOptions)
                            .into(object : SimpleTarget<Drawable>() {
                                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                    itemView.imgUsuario_imas.setImageBitmap(Utilitarios.getRoundedCornerDrawable(resource))
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    itemView.imgUsuario_imas.setImageResource(R.drawable.photo_default)
                                }
                            })
                    itemView.lblNombre_imas.text = usuario.nombreCompleto
                    itemView.lblCorreo_imas.text = usuario.email
                }
                else -> {
                    itemView.imgUsuario_imas.setImageResource(R.drawable.photo_default)
                    itemView.lblNombre_imas.text = itemView.context.resources.getString(R.string.invitado)
                    itemView.lblCorreo_imas.text = ""
                }
            }
            itemView.setOnClickListener { clickListener(usuario as UserEsan, UNSUARIO, itemView, "") }
        }
    }

    class UsuarioMultipleHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(usuario: UserEsan, clickListener: (UserEsan, Int, View, String) -> Unit) {
            itemView.lblNombrem_imas.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            var tipoUsuario = ""

            val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //because file name is always same
                    .skipMemoryCache(true)

            when (usuario) {
                is Alumno -> {
                    Glide.with(itemView.context)
                            .load(Utilitarios.getUrlFoto(usuario.codigo, 140))
                            .apply(requestOptions)
                            .into(object : SimpleTarget<Drawable>() {
                                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                    itemView.imgUsuariom_imas.setImageBitmap(Utilitarios.getRoundedCornerDrawable(resource))
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    itemView.imgUsuariom_imas.setImageResource(R.drawable.photo_default)
                                }
                            })

                    if (usuario.tipoAlumno == Utilitarios.POS) {
                        tipoUsuario = Utilitarios.POS
                        itemView.lblNombrem_imas.text = itemView.context.resources.getString(R.string.alumnopos)
                    } else {
                        tipoUsuario = Utilitarios.PRE
                        itemView.lblNombrem_imas.text = itemView.context.resources.getString(R.string.alumnopre)
                    }

                    if (usuario.seleccionado) {
                        itemView.viewImgContent_prmigrupo.background = ContextCompat.getDrawable(itemView.context, R.drawable.borde_usuario_select)
                    }
                }
                is Profesor -> {
                    Glide.with(itemView.context)
                            .load(Utilitarios.getUrlFoto(usuario.codigo, 140))
                            .apply(requestOptions)
                            .into(object : SimpleTarget<Drawable>() {
                                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                    itemView.imgUsuariom_imas.setImageBitmap(Utilitarios.getRoundedCornerDrawable(resource))
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    itemView.imgUsuariom_imas.setImageResource(R.drawable.photo_default)
                                }
                            })
                    tipoUsuario = Utilitarios.DOC
                    itemView.lblNombrem_imas.text = itemView.context.resources.getString(R.string.profesor)
                    if (usuario.seleccionado) {
                        itemView.viewImgContent_prmigrupo.background = ContextCompat.getDrawable(itemView.context, R.drawable.borde_usuario_select)
                    }
                }
            }
            itemView.setOnClickListener { clickListener(usuario as UserEsan, MASUSUARIOS, itemView, tipoUsuario) }
        }
    }
}