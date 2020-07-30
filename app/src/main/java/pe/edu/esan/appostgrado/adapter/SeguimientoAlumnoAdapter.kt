package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_seguimientoalumno_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.AlumnoShort
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 30/05/18.
 */
class SeguimientoAlumnoAdapter (val listaAlumnos: List<AlumnoShort>, val anchoPantalla: Int, val densidad: Float): androidx.recyclerview.widget.RecyclerView.Adapter<SeguimientoAlumnoAdapter.SeguimientoAlumnoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeguimientoAlumnoHolder {
        return SeguimientoAlumnoHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_seguimientoalumno_lista, parent, false), densidad)
    }

    override fun onBindViewHolder(holder: SeguimientoAlumnoHolder, position: Int) {
        holder?.setValues(listaAlumnos[position], anchoPantalla, densidad)
    }

    override fun getItemCount(): Int {
        return listaAlumnos.size
    }

    class SeguimientoAlumnoHolder(view: View, densidad: Float) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValues(alumno: AlumnoShort, anchoPantalla: Int, densidad: Float) {
            itemView.lblAlumno_rowdetalleasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblCodigo_rowdetalleasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)

            itemView.lblAlumno_rowdetalleasistencia.text = alumno.nombre
            itemView.lblCodigo_rowdetalleasistencia.text = alumno.codigo

            Glide.with(itemView.context)
                    .load(Utilitarios.getUrlFoto(alumno.codigo, 100))
                    .into(itemView.imgAlumno_rowdetalleasistencia)

            val porcAsistencia =  alumno.asistencias.toFloat() * 100 / alumno.totalsesiones.toFloat()
            val porcTardanza = alumno.tardanzas.toFloat() * 100 / alumno.totalsesiones.toFloat()
            val porcFalta = alumno.faltas.toFloat() * 100 / alumno.totalsesiones.toFloat()
            val porcRestante = 100 - (porcAsistencia + porcTardanza + porcFalta)

            //String.format("%.1f",
            //println("$porcAsistencia -  $porcTardanza  -  $porcFalta - $porcRestante")

            val valor = (10 * densidad + 0.5f)
            val largoTotal = (anchoPantalla - (valor * 2)) - margen

            println(valor)
            println(largoTotal)
            val anchoAsistencia = largoTotal * porcAsistencia / 100
            val anchoTardaza = largoTotal * porcTardanza / 100
            val anchoFalta = largoTotal * porcFalta / 100
            val anchoRestante = largoTotal * porcRestante / 100

            println("$anchoAsistencia -  $anchoTardaza  -  $anchoFalta - $anchoRestante")

            val anchoAsistenciaReal = anchoAsistencia
            val anchoTardanzaReal = anchoAsistenciaReal + anchoTardaza
            val anchoFaltaReal = anchoTardanzaReal + anchoFalta

            itemView.viewAsistencia_rowdetalleasistencia.layoutParams.width = anchoAsistenciaReal.toInt()
            itemView.viewTardanza_rowdetalleasistencia.layoutParams.width = anchoTardanzaReal.toInt()
            itemView.viewFalta_rowdetalleasistencia.layoutParams.width = anchoFaltaReal.toInt()

            if (porcAsistencia < 1.0f) {
                itemView.lblPorcentajeAsis_rowdetalleasistencia.text = ""
            } else {
                if (porcAsistencia >= 1.0f && porcAsistencia < 10.0f) {
                    if (anchoAsistencia >= lenght1) {
                        itemView.lblPorcentajeAsis_rowdetalleasistencia.layoutParams.width = anchoAsistencia.toInt()
                        itemView.lblPorcentajeAsis_rowdetalleasistencia.text = "%.1f".format(porcAsistencia) +"%"
                    } else {
                        itemView.lblPorcentajeAsis_rowdetalleasistencia.text = ""
                    }
                } else if (porcAsistencia >= 10.0f && porcAsistencia < 100.0f) {
                    if (anchoAsistencia >= lenght2) {
                        itemView.lblPorcentajeAsis_rowdetalleasistencia.layoutParams.width = anchoAsistencia.toInt()
                        itemView.lblPorcentajeAsis_rowdetalleasistencia.text = "%.1f".format(porcAsistencia) +"%"
                    } else {
                        itemView.lblPorcentajeAsis_rowdetalleasistencia.text = ""
                    }
                } else {
                    itemView.lblPorcentajeAsis_rowdetalleasistencia.layoutParams.width = anchoAsistencia.toInt()
                    itemView.lblPorcentajeAsis_rowdetalleasistencia.text = "%.1f".format(porcAsistencia) +"%"
                }
            }

            if (porcTardanza < 1.0f) {
                itemView.lblPorcentajeTard_rowdetalleasistencia.text = ""
            } else {
                if (porcTardanza >= 1.0f && porcTardanza < 10.0f) {
                    if (anchoTardaza >= lenght1) {
                        itemView.lblPorcentajeTard_rowdetalleasistencia.layoutParams.width = anchoTardaza.toInt()
                        itemView.lblPorcentajeTard_rowdetalleasistencia.x = itemView.viewAsistencia_rowdetalleasistencia.layoutParams.width.toFloat()
                        itemView.lblPorcentajeTard_rowdetalleasistencia.text = "%.1f".format(porcTardanza) +"%"
                    } else {
                        itemView.lblPorcentajeTard_rowdetalleasistencia.text = ""
                    }
                } else if (porcTardanza >= 10.0f && porcTardanza < 100.0f) {
                    if (anchoTardaza >= lenght2) {
                        itemView.lblPorcentajeTard_rowdetalleasistencia.layoutParams.width = anchoTardaza.toInt()
                        itemView.lblPorcentajeTard_rowdetalleasistencia.x = itemView.viewAsistencia_rowdetalleasistencia.layoutParams.width.toFloat()
                        itemView.lblPorcentajeTard_rowdetalleasistencia.text = "%.1f".format(porcTardanza) +"%"
                    } else {
                        itemView.lblPorcentajeTard_rowdetalleasistencia.text = ""
                    }
                } else {
                    itemView.lblPorcentajeTard_rowdetalleasistencia.layoutParams.width = anchoTardaza.toInt()
                    itemView.lblPorcentajeTard_rowdetalleasistencia.x = itemView.viewAsistencia_rowdetalleasistencia.layoutParams.width.toFloat()
                    itemView.lblPorcentajeTard_rowdetalleasistencia.text = "%.1f".format(porcTardanza) +"%"
                }
            }

            if (porcFalta < 1.0f) {
                itemView.lblPorcentajeFalt_rowdetalleasistencia.text = ""
            } else {
                if (porcFalta >= 1.0f && porcFalta < 10.0f) {
                    if (anchoFalta >= lenght1) {
                        itemView.lblPorcentajeFalt_rowdetalleasistencia.layoutParams.width = anchoFalta.toInt()
                        itemView.lblPorcentajeFalt_rowdetalleasistencia.x = itemView.viewTardanza_rowdetalleasistencia.layoutParams.width.toFloat()
                        itemView.lblPorcentajeFalt_rowdetalleasistencia.text = "%.1f".format(porcFalta) +"%"
                    } else {
                        itemView.lblPorcentajeFalt_rowdetalleasistencia.text = ""
                    }
                } else if (porcFalta >= 10.0f && porcFalta < 100.0f) {
                    if (anchoFalta >= lenght2) {
                        itemView.lblPorcentajeFalt_rowdetalleasistencia.layoutParams.width = anchoFalta.toInt()
                        itemView.lblPorcentajeFalt_rowdetalleasistencia.x = itemView.viewTardanza_rowdetalleasistencia.layoutParams.width.toFloat()
                        itemView.lblPorcentajeFalt_rowdetalleasistencia.text = "%.1f".format(porcFalta) +"%"
                    } else {
                        itemView.lblPorcentajeFalt_rowdetalleasistencia.text = ""
                    }
                } else {
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.layoutParams.width = anchoFalta.toInt()
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.x = itemView.viewTardanza_rowdetalleasistencia.layoutParams.width.toFloat()
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.text = "%.1f".format(porcFalta) +"%"
                }
            }

            if (porcRestante < 1.0f) {
                itemView.lblPorcentajeRest_rowdetalleasistencia.text = ""
            } else {
                if (porcRestante >= 1.0f && porcRestante < 10.0f) {
                    if (anchoRestante >= lenght1) {
                        itemView.lblPorcentajeRest_rowdetalleasistencia.layoutParams.width = anchoRestante.toInt()
                        itemView.lblPorcentajeRest_rowdetalleasistencia.text = "%.1f".format(porcRestante) +"%"
                    } else {
                        itemView.lblPorcentajeRest_rowdetalleasistencia.text = ""
                    }
                } else if (porcRestante >= 10.0f && porcRestante < 100.0f) {
                    if (anchoRestante >= lenght2) {
                        itemView.lblPorcentajeRest_rowdetalleasistencia.layoutParams.width = anchoRestante.toInt()
                        itemView.lblPorcentajeRest_rowdetalleasistencia.text = "%.1f".format(porcRestante) +"%"
                    } else {
                        itemView.lblPorcentajeRest_rowdetalleasistencia.text = ""
                    }
                } else {
                    itemView.lblPorcentajeRest_rowdetalleasistencia.layoutParams.width = anchoRestante.toInt()
                    itemView.lblPorcentajeRest_rowdetalleasistencia.text = "%.1f".format(porcRestante) +"%"
                }
            }
        }

        val lenght2 = (31 * densidad + 0.5f)
        val lenght1 = (24 * densidad + 0.5f)
        val margen = (20 * densidad + 0.5f)
    }


}