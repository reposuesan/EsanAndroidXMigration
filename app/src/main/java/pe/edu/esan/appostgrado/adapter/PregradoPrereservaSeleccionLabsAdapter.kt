package pe.edu.esan.appostgrado.adapter

import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.itemview_pregrado_pr_lab.view.*
import pe.edu.esan.appostgrado.R

import pe.edu.esan.appostgrado.entidades.ProgramaDescripcionItem

class PregradoPrereservaSeleccionLabsAdapter(private val listLaboratorios: List<ProgramaDescripcionItem>, var listener: LabsListener, var countForAdapter: Int, var cantidadMaxProgramasXAlumno: Int) : androidx.recyclerview.widget.RecyclerView.Adapter<PregradoPrereservaSeleccionLabsAdapter.LabItemViewHolder>() {

    private var count: Int = countForAdapter

    private val LOG = PregradoPrereservaSeleccionLabsAdapter::class.simpleName

    interface LabsListener{
        fun itemClick(position: Int, itemSelected: Boolean, programaId: String, laboratorioId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemview_pregrado_pr_lab, parent, false)

        return LabItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listLaboratorios.size
    }

    override fun onBindViewHolder(holder: LabItemViewHolder, position: Int) {

        holder.buttonLabItem.text = listLaboratorios[holder.adapterPosition].programaNombre

        //Item seleccionado
        if(listLaboratorios[holder.adapterPosition].itemSeleccionado) {
            holder.buttonLabItem.setBackgroundResource(R.drawable.shape_item_seleccionado_software)
            holder.buttonLabItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
        //Item no seleccionado
        } else {
            holder.buttonLabItem.setBackgroundResource(R.drawable.shape_item_no_seleccionado)
            holder.buttonLabItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_black_1000))
        }

        holder.buttonLabItem.setOnClickListener {

            if(listLaboratorios[holder.adapterPosition].itemSeleccionado){
                //if(count == 1){
                    //Item ha sido deseleccionado
                    listLaboratorios[holder.adapterPosition].itemSeleccionado = false
                    holder.buttonLabItem.setBackgroundResource(R.drawable.shape_item_no_seleccionado)
                    holder.buttonLabItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_black_1000))
                    count--
                    listener.itemClick(holder.adapterPosition, listLaboratorios[holder.adapterPosition].itemSeleccionado, listLaboratorios[holder.adapterPosition].programaId, listLaboratorios[holder.adapterPosition].laboratorioId)
                //}
            } else{
                //Item ha sido seleccionado
                if(count < cantidadMaxProgramasXAlumno){
                    listLaboratorios[holder.adapterPosition].itemSeleccionado = true
                    holder.buttonLabItem.setBackgroundResource(R.drawable.shape_item_seleccionado_software)
                    holder.buttonLabItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                    count++
                    listener.itemClick(holder.adapterPosition, listLaboratorios[holder.adapterPosition].itemSeleccionado, listLaboratorios[holder.adapterPosition].programaId, listLaboratorios[holder.adapterPosition].laboratorioId)
                } else {
                    val snack = Snackbar.make(holder.view, holder.view.context.getString(R.string.max_cantidad_programas_x_alumno, count), Snackbar.LENGTH_SHORT)
                    snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                    snack.show()
                }
            }
        }


    }

    class LabItemViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val buttonLabItem: Button = view.button_item
    }
}