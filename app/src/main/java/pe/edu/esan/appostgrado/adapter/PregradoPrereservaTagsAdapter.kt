package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.itemview_pregrado_lab_tag.view.*
import pe.edu.esan.appostgrado.R

class PregradoPrereservaTagsAdapter(private val listTags: List<String>) : androidx.recyclerview.widget.RecyclerView.Adapter<PregradoPrereservaTagsAdapter.TagItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemview_pregrado_lab_tag, parent, false)

        return TagItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listTags.size
    }

    override fun onBindViewHolder(holder: TagItemViewHolder, position: Int) {
        holder.tagItem.text = listTags[holder.adapterPosition]
    }


    class TagItemViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val tagItem: Button = view.tag_item_selected
    }
}