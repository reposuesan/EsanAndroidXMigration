package pe.edu.esan.appostgrado.adapter

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_contacto.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 29/05/18.
 */
class ContactoAdapter (val listaAlumnos: List<Alumno>): androidx.recyclerview.widget.RecyclerView.Adapter<ContactoAdapter.ContactoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoHolder {
        return ContactoHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_contacto, parent, false))
    }

    override fun onBindViewHolder(holder: ContactoHolder, position: Int) {
        holder.setValores(listaAlumnos[position])
    }

    override fun getItemCount(): Int {
        return listaAlumnos.size
    }

    class ContactoHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(alumno: Alumno) {

            itemView.lblSCodigo_idirectorio.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblSNombre_idirectorio.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblSCorreo_idirectorio.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

            itemView.lblCodigo_idirectorio.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblNombre_idirectorio.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblCorreo_idirectorio.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

            itemView.lblCodigo_idirectorio.text = alumno.codigo
            itemView.lblNombre_idirectorio.text = alumno.nombreCompleto
            if (alumno.correo.isEmpty()) {
                itemView.lblSCorreo_idirectorio.visibility = View.GONE
                itemView.lblCorreo_idirectorio.visibility = View.GONE
                itemView.btnCorreo_idirectorio.visibility = View.GONE
            } else {
                itemView.lblSCorreo_idirectorio.visibility = View.VISIBLE
                itemView.lblCorreo_idirectorio.visibility = View.VISIBLE
                itemView.btnCorreo_idirectorio.visibility = View.VISIBLE
                itemView.lblCorreo_idirectorio.text = alumno.correo
            }

            Glide.with(itemView.context)
                    .load(Utilitarios.getUrlFoto(alumno.codigo, 90))
                    .into(itemView.imgFoto_idirectorio)

            itemView.btnCorreo_idirectorio.setOnClickListener { view ->
                val enviarCorreoIntent = Intent(Intent.ACTION_SEND)
                enviarCorreoIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(alumno.correo))
                enviarCorreoIntent.putExtra(Intent.EXTRA_SUBJECT, itemView.resources.getString(R.string.consultar_))
                enviarCorreoIntent.putExtra(Intent.EXTRA_TEXT, "")
                enviarCorreoIntent.type = "message/rfc822"

                if (enviarCorreoIntent.resolveActivity(itemView.context.packageManager) != null) {
                    itemView.context.startActivity(Intent.createChooser(enviarCorreoIntent, itemView.resources.getString(R.string.info_seleccionesclientecorreo)))
                }
                /*val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    //putExtra(Intent.EXTRA_EMAIL, alumno.correo)
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(alumno.correo))
                    putExtra(Intent.EXTRA_SUBJECT, itemView.resources.getString(R.string.consultar_))
                    putExtra(Intent.EXTRA_TEXT, "")
                }

                if (intent.resolveActivity(itemView.context.packageManager) != null) {
                    itemView.context.startActivity(intent)
                }*/
            }

        }
    }
}