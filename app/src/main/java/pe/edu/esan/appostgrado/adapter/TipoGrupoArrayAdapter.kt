package pe.edu.esan.appostgrado.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import pe.edu.esan.appostgrado.entidades.TipoGrupo

/**
 * Created by lventura on 14/05/18.
 */
class TipoGrupoArrayAdapter (context: Context, listaTipoGrupo: List<TipoGrupo>) : ArrayAdapter<TipoGrupo>(context, android.R.layout.select_dialog_item, listaTipoGrupo) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = if (convertView != null) convertView else LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        itemView.findViewById<TextView>(android.R.id.text1).text= getItem(position)?.descripcion
        return itemView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView = if (convertView != null) convertView else LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        itemView.findViewById<TextView>(android.R.id.text1).text= getItem(position)?.descripcion
        return itemView
    }
}