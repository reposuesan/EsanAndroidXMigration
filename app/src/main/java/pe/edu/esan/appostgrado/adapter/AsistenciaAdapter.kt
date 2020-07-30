package pe.edu.esan.appostgrado.adapter

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_alumnoasistencia_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Alumno
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 23/05/18.
 */
class AsistenciaAdapter (val listaAlumno: List<Alumno>): androidx.recyclerview.widget.RecyclerView.Adapter<AsistenciaAdapter.AsistenciaHolder>() {
    override fun onBindViewHolder(holder: AsistenciaHolder, position: Int) {
        holder?.setValores(listaAlumno[position]) { alumno, position ->
            if (alumno.estadoAsistencia.isEmpty()) {
                alumno.estadoAsistencia = estados[0]
                alumno.actualizoEstadoAsistencia = true
            } else {
                for (i in 0 until estados.size) {
                    if (estados[i] == alumno.estadoAsistencia) {
                        if (i == 2) {
                            alumno.estadoAsistencia = estados[0]
                            alumno.actualizoEstadoAsistencia = true
                        } else {
                            alumno.estadoAsistencia = estados[i+1]
                            alumno.actualizoEstadoAsistencia = true
                        }
                        break
                    }
                }
            }
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsistenciaHolder {
        return AsistenciaHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_alumnoasistencia_lista, parent, false))
    }

    override fun getItemCount(): Int {
        return listaAlumno.size
    }

    class AsistenciaHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(alumno: Alumno, clickListener:(Alumno, Int) -> Unit) {

            itemView.lblNombre_itomarasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblCodigo_itomarasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblPorcFaltas.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblPorcInhabilitado.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

            itemView.lblNombre_itomarasistencia.text = alumno.nombreCompleto
            itemView.lblCodigo_itomarasistencia.text = alumno.codigo

            //Log.e("AsistenciaAdapter", alumno.estado)
            //Log.e("AsistenciaAdapter", alumno.porcInha.toString())

            if (alumno.estado == "A") {
                /*if (alumno.porcFaltas > alumno.porcInha.toFloat()) {
                    itemView.background = ContextCompat.getDrawable(itemView.context, R.color.md_grey_80)
                    itemView.btnAsistencia_itomarasistencia.text = "DPI"
                    itemView.btnAsistencia_itomarasistencia.background = null
                    itemView.btnAsistencia_itomarasistencia.setTextColor(ContextCompat.getColor(itemView.context, R.color.retirado))

                    itemView.lblPorcFaltas.text = itemView.resources.getString(R.string.desaprobado_inasistencias)
                    itemView.lblPorcFaltas.setTextColor(ContextCompat.getColor(itemView.context, R.color.retirado))
                    itemView.lblPorcInhabilitado.text = ""

                    val url = Utilitarios.getUrlFoto(alumno.codigo, 100)
                    Glide.with(itemView.context)
                            .load(url)
                            .into(itemView.imgAlumno_itomarasistencia)

                    itemView.setOnClickListener { null }
                } else {*/
                    itemView.background = ContextCompat.getDrawable(itemView.context, R.color.md_white_1000)
                    itemView.btnAsistencia_itomarasistencia.text = alumno.estadoAsistencia

                    if (alumno.estadoAsistencia.isEmpty()) {
                        itemView.btnAsistencia_itomarasistencia.background = ContextCompat.getDrawable(itemView.context, R.drawable.borde_punteado)
                    } else {
                        itemView.btnAsistencia_itomarasistencia.background = null
                        when (alumno.estadoAsistencia) {
                            "A" -> itemView.btnAsistencia_itomarasistencia.setTextColor(ContextCompat.getColor(itemView.context, R.color.asistencia))
                            "T" -> itemView.btnAsistencia_itomarasistencia.setTextColor(ContextCompat.getColor(itemView.context, R.color.tardanza))
                            else -> itemView.btnAsistencia_itomarasistencia.setTextColor(ContextCompat.getColor(itemView.context, R.color.falta))
                        }
                    }

                    val top = alumno.porcInha.toFloat()
                    val nivel1 = top / 3
                    val nivel2 = (2 * top)/3
                    //val nivel3 = top
                    //val nivel4 = nivel3 - niveles

                    when (alumno.porcFaltas) {
                        in 0f..nivel1 -> {
                            itemView.lblPorcFaltas.setTextColor(ContextCompat.getColor(itemView.context, R.color.asistencia))
                        }
                        in nivel1..nivel2 -> {
                            itemView.lblPorcFaltas.setTextColor(ContextCompat.getColor(itemView.context, R.color.tardanza))
                        }
                        in nivel2..top -> {
                            itemView.lblPorcFaltas.setTextColor(ContextCompat.getColor(itemView.context, R.color.falta))
                        }
                        in top..100.0f -> {
                            itemView.lblPorcFaltas.setTextColor(ContextCompat.getColor(itemView.context, R.color.falta))
                        }
                    }

                    itemView.lblPorcFaltas.text = itemView.resources.getString(R.string.inasistencias_) + String.format(itemView.resources.getString(R.string.val_procentaje), alumno.porcFaltas)
                    itemView.lblPorcInhabilitado.text = " / " + String.format(itemView.resources.getString(R.string.val_procentaje), alumno.porcInha.toFloat())

                    val url = Utilitarios.getUrlFoto(alumno.codigo, 100)
                    Glide.with(itemView.context)
                            .load(url)
                            .into(itemView.imgAlumno_itomarasistencia)


                    itemView.setOnClickListener { clickListener(alumno, adapterPosition) }
                /*}*/
            } else {
                itemView.background = ContextCompat.getDrawable(itemView.context, R.color.md_grey_80)
                itemView.btnAsistencia_itomarasistencia.text = "R"
                itemView.btnAsistencia_itomarasistencia.background = null
                itemView.btnAsistencia_itomarasistencia.setTextColor(ContextCompat.getColor(itemView.context, R.color.retirado))

                itemView.lblPorcFaltas.text = itemView.resources.getString(R.string.retirado)
                itemView.lblPorcFaltas.setTextColor(ContextCompat.getColor(itemView.context, R.color.retirado))
                itemView.lblPorcInhabilitado.text = ""

                val url = Utilitarios.getUrlFoto(alumno.codigo, 100)
                Glide.with(itemView.context)
                        .load(url)
                        .into(itemView.imgAlumno_itomarasistencia)

                itemView.setOnClickListener { null }
            }
        }
    }

    private val estados = arrayOf("A", "F", "T")
}