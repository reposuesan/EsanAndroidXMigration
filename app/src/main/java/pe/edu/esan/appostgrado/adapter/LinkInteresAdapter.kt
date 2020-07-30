package pe.edu.esan.appostgrado.adapter

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_linkinteres.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Link
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 31/07/18.
 */
class LinkInteresAdapter(val listaLink: List<Link>): androidx.recyclerview.widget.RecyclerView.Adapter<LinkInteresAdapter.LinkHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkHolder {
        return LinkHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_linkinteres, parent, false))
    }

    override fun onBindViewHolder(holder: LinkHolder, position: Int) {
        holder?.setValores(listaLink[position])
    }

    override fun getItemCount(): Int {
        return listaLink.size
    }

    class LinkHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(link: Link) {
            itemView.lblOpciones_ilink.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)
            itemView.lblOpciones_ilink.text = link.titulo

            itemView.imgIcono_ilink.setImageResource(link.icon)
            itemView.setOnClickListener {
                val intentLink = Intent("android.intent.action.VIEW", Uri.parse(link.url))
                intentLink.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intentLink.resolveActivity(itemView.context.packageManager) != null) {
                    itemView.context.startActivity(intentLink)
                }
            }
        }
    }
}