package pe.edu.esan.appostgrado.adapter

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_seccionhistoricoasistencia_profesor.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.SeccionHistorial
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 1/06/18.
 */
class HistoricoAsistenciaProfesorAdapter(val listaSesiones: List<SeccionHistorial>): androidx.recyclerview.widget.RecyclerView.Adapter<HistoricoAsistenciaProfesorAdapter.HistoricoAsistenciaHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoAsistenciaHolder {
        return HistoricoAsistenciaHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_seccionhistoricoasistencia_profesor, parent, false))
    }

    override fun onBindViewHolder(holder: HistoricoAsistenciaHolder, position: Int) {
        holder?.setValues(listaSesiones[position])
    }

    override fun getItemCount(): Int {
        return listaSesiones.size
    }
    class HistoricoAsistenciaHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValues(seccion: SeccionHistorial) {
            itemView.lblFecha_seccionhistoricoprofesor.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblHora_seccionhistoricoprofesor.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblMensaje_seccionhistoricoprofesor.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
            itemView.lblMarco_seccionhistoricoprofesor.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)

            itemView.lblFecha_seccionhistoricoprofesor.text = seccion.fecha
            itemView.lblHora_seccionhistoricoprofesor.text = "${seccion.horainicio} - ${seccion.horafin}"

            when (seccion.estado) {
                0 -> {
                    itemView.setBackgroundResource(R.drawable.borde_tardanza)
                    itemView.lblMensaje_seccionhistoricoprofesor.text = itemView.resources.getString(R.string.no__marco)
                    itemView.lblMensaje_seccionhistoricoprofesor.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                    itemView.lblFecha_seccionhistoricoprofesor.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                    itemView.lblHora_seccionhistoricoprofesor.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                    itemView.lblMarco_seccionhistoricoprofesor.visibility = View.GONE
                }
                1 -> {
                    itemView.lblMarco_seccionhistoricoprofesor.text = seccion.horamarco
                    itemView.lblMensaje_seccionhistoricoprofesor.text = itemView.resources.getString(R.string.asis_marcada)
                    itemView.setBackgroundResource(R.drawable.borde_asistencia)
                    itemView.lblMensaje_seccionhistoricoprofesor.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                    itemView.lblFecha_seccionhistoricoprofesor.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                    itemView.lblHora_seccionhistoricoprofesor.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                    itemView.lblMarco_seccionhistoricoprofesor.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_white_1000))
                    itemView.lblMarco_seccionhistoricoprofesor.visibility = View.VISIBLE
                }
                else -> {
                    itemView.lblMensaje_seccionhistoricoprofesor.text = itemView.resources.getString(R.string.proxima__sesion)
                    itemView.setBackgroundResource(R.drawable.borde_proximo_t)
                    itemView.lblMensaje_seccionhistoricoprofesor.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_black_1000))
                    itemView.lblFecha_seccionhistoricoprofesor.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_black_1000))
                    itemView.lblHora_seccionhistoricoprofesor.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_black_1000))
                    itemView.lblMarco_seccionhistoricoprofesor.visibility = View.GONE
                }
            }
        }
    }
}