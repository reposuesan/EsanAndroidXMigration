package pe.edu.esan.appostgrado.adapter

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_linkinteres.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Comedor
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.mas.comedor.MenuActivity

/**
 * Created by lventura on 2/08/18.
 */
class ComedorAdapter(val listaComedores: List<Comedor>): androidx.recyclerview.widget.RecyclerView.Adapter<ComedorAdapter.ComedorHolder>() {

    override fun onBindViewHolder(holder: ComedorHolder, position: Int) {
        holder?.setValores(listaComedores[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComedorHolder {
        return ComedorHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_linkinteres, parent, false))
    }

    override fun getItemCount(): Int {
        return listaComedores.size
    }

    class ComedorHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(comedor: Comedor) {
            itemView.lblOpciones_ilink.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)
            itemView.lblOpciones_ilink.text = comedor.nombre

            itemView.imgIcono_ilink.setImageResource(comedor.icono)
            itemView.imgIcono_ilink.layoutParams.width = 80
            itemView.imgIcono_ilink.layoutParams.height = 80
            itemView.setOnClickListener {
                val intentComedor = Intent(itemView.context, MenuActivity::class.java)
                intentComedor.putExtra("IdComedor", comedor.id)
                itemView.context.startActivity(intentComedor)
            }
        }
    }
}