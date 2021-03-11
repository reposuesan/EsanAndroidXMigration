package pe.edu.esan.appostgrado.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.itemview_pregrado_pr_lab_historial.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.PrereservaDetalle

class PregradoPrereservasHistorialAdapter(private val listPrereservas: ArrayList<PrereservaDetalle>, val esLaboratorio: Boolean, var listener: HistorialListener?, var mapListener: HistorialMapaListener?) :
    androidx.recyclerview.widget.RecyclerView.Adapter<PregradoPrereservasHistorialAdapter.HistorialViewHolder>() {

    interface HistorialListener {
        fun onClickItem(position: Int)
    }

    interface HistorialMapaListener {
        fun onClickMapaItem(urlString: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_pregrado_pr_lab_historial, parent, false)

        return HistorialViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listPrereservas.size
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        holder.creadorPrereservaTV.text = listPrereservas[position].creadorPrereserva
        holder.horarioPrereservaTV.text = listPrereservas[position].horarioPrereserva
        holder.fechaPrereservaTV.text = listPrereservas[position].fechaPrereserva

        holder.ubicacionPrereservaTV.text = listPrereservas[position].referenciaUbicacion

        if(esLaboratorio){
            //LABORATORIO
            holder.cubiculoLabLabel.text = holder.view.context.getString(R.string.maquina_lab_text)
            holder.creadorPrereservaTV.visibility = View.GONE
            holder.solicitanteLabel.visibility = View.GONE
            holder.buttonMapa.visibility = View.GONE
        } else {
            //CUBICULO
            holder.cubiculoLabLabel.text = holder.view.context.getString(R.string.cubiculo_text)
            holder.creadorPrereservaTV.visibility = View.VISIBLE
            holder.solicitanteLabel.visibility = View.VISIBLE
            holder.buttonMapa.visibility = View.VISIBLE
        }

        holder.cubiculoLabPrereservaTV.text = listPrereservas[position].nomLabCubiculo

        if (listPrereservas[position].prereservaEstado.equals("Confirmado")) {
            holder.estadoPrereservaTV.text = holder.view.context.getString(R.string.confirmada_text)
            holder.estadoPrereservaTV.setTextColor(ContextCompat.getColor(holder.view.context, R.color.green))
        } else if(listPrereservas[position].prereservaEstado.equals("Perdida")) {
            holder.estadoPrereservaTV.text = holder.view.context.getString(R.string.perdida_text)
            holder.estadoPrereservaTV.setTextColor(ContextCompat.getColor(holder.view.context, R.color.esan_red))
        } else {
            holder.estadoPrereservaTV.text = holder.view.context.getString(R.string.sin_confirmar_text)
            holder.estadoPrereservaTV.setTextColor(ContextCompat.getColor(holder.view.context, R.color.light_blue_color))
        }

        if(!esLaboratorio){
            holder.itemView.setOnClickListener {
                listener?.onClickItem(position)
            }
        } else {
            holder.itemView.setOnClickListener(null)
        }

        if(!esLaboratorio){
            holder.buttonMapa.setOnClickListener {
                val ubicacionEnMapaImagen = listPrereservas[position].imagenMapa
                mapListener?.onClickMapaItem(ubicacionEnMapaImagen)
            }
        } else {
            holder.buttonMapa.setOnClickListener(null)
        }

    }


    class HistorialViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val creadorPrereservaTV: TextView = view.tv_historial_creador_prereserva
        val horarioPrereservaTV: TextView = view.tv_historial_horario_prereserva
        val fechaPrereservaTV: TextView = view.tv_historial_fecha_prereserva
        val ubicacionPrereservaTV: TextView = view.tv_historial_ref_ubicacion_prereserva
        val estadoPrereservaTV: TextView = view.tv_historial_estado_prereserva
        val cubiculoLabPrereservaTV: TextView = view.tv_historial_nombre_cubiculo_lab_prereserva

        val cubiculoLabLabel: TextView = view.tv_cubiculo_lab_label
        val solicitanteLabel: TextView = view.tv_solicitante_historial
        val buttonMapa: Button = view.ver_mapa_labs_button;
    }

}