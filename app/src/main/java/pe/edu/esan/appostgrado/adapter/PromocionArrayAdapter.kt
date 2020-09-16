package pe.edu.esan.appostgrado.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import pe.edu.esan.appostgrado.entidades.Promocion

/**
 * Created by lventura on 14/05/18.
 */
class PromocionArrayAdapter(context: Context, listaPromocion: List<Promocion>) : ArrayAdapter<Promocion>(context, android.R.layout.simple_expandable_list_item_1, listaPromocion) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = if (convertView != null) convertView else LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        itemView.findViewById<TextView>(android.R.id.text1).text= getItem(position)?.nombre
        return itemView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView = if (convertView != null) convertView else LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        itemView.findViewById<TextView>(android.R.id.text1).text= getItem(position)?.nombre
        return itemView
    }
}