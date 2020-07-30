package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_notaspre.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.NotasPre

/**
 * Created by lventura on 28/06/18.
 */
class NotasPreAdapter(val listaNotasPre: ArrayList<NotasPre>): androidx.recyclerview.widget.RecyclerView.Adapter<NotasPreAdapter.NotasPreHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotasPreHolder {
        return NotasPreHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_notaspre, parent, false))
    }

    override fun onBindViewHolder(holder: NotasPreHolder, position: Int) {
        //holder?.setValores()
        holder?.setValores(listaNotasPre[position])
    }

    override fun getItemCount(): Int {
        return listaNotasPre.size
    }

    class NotasPreHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(nota: NotasPre) {
            itemView.lblTipoNota_notapre.text = nota.tipo
            itemView.lblPeso_notapre.text = nota.peso
            itemView.lblNota_notapre.text = nota.nota
        }
    }
}