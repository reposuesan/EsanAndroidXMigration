package pe.edu.esan.appostgrado.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_seguimientoalumno_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.AlumnoShort
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 31/05/18.
 */
class SeguimientoAlumnoLinearAdapter (con: Context, listaAlumnos: List<AlumnoShort>, val anchoPantalla: Int, val densidad: Float ): ArrayAdapter<AlumnoShort>(con, R.layout.item_seguimientoalumno_lista, listaAlumnos) {

    val lenght3 = (37 * densidad + 0.5f)
    val lenght2 = (31 * densidad + 0.5f)
    val lenght1 = (24 * densidad + 0.5f)
    val margen = (20 * densidad + 0.5f)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.item_seguimientoalumno_lista, parent, false)
        val alumno = getItem(position)

        itemView.lblAlumno_rowdetalleasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
        itemView.lblCodigo_rowdetalleasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)

        itemView.lblAlumno_rowdetalleasistencia.text = alumno?.nombre
        itemView.lblCodigo_rowdetalleasistencia.text = alumno?.codigo

        Glide.with(itemView.context)
                .load(Utilitarios.getUrlFoto(alumno!!.codigo, 100))
                .into(itemView.imgAlumno_rowdetalleasistencia)

        val porcAsistencia =  alumno.asistencias.toFloat() * 100 / alumno.totalsesiones.toFloat()
        val porcTardanza = alumno.tardanzas.toFloat() * 100 / alumno.totalsesiones.toFloat()
        val porcFalta = alumno.faltas.toFloat() * 100 / alumno.totalsesiones.toFloat()
        val porcRestante = 100 - (porcAsistencia + porcTardanza + porcFalta)

        //String.format("%.1f",
        println("------------")
        println("$porcAsistencia -  $porcTardanza  -  $porcFalta - $porcRestante")

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

        if (porcAsistencia < 1) {
            itemView.lblPorcentajeAsis_rowdetalleasistencia.text = ""
        } else {
            if (porcAsistencia >= 1 && porcAsistencia < 10) {
                if (anchoAsistencia >= lenght1) {
                    itemView.lblPorcentajeAsis_rowdetalleasistencia.layoutParams.width = lenght1.toInt()
                    itemView.lblPorcentajeAsis_rowdetalleasistencia.x = (anchoAsistencia / 2) - (lenght1 / 2)
                    itemView.lblPorcentajeAsis_rowdetalleasistencia.text = "%.1f".format(porcAsistencia) +"%"
                } else {
                    itemView.lblPorcentajeAsis_rowdetalleasistencia.text = ""
                }
            } else if (porcAsistencia >= 10 && porcAsistencia < 100) {
                if (anchoAsistencia >= lenght2) {
                    itemView.lblPorcentajeAsis_rowdetalleasistencia.layoutParams.width = lenght2.toInt()
                    itemView.lblPorcentajeAsis_rowdetalleasistencia.x = (anchoAsistencia / 2) - (lenght2 / 2)
                    itemView.lblPorcentajeAsis_rowdetalleasistencia.text = "%.1f".format(porcAsistencia) +"%"
                } else {
                    itemView.lblPorcentajeAsis_rowdetalleasistencia.text = ""
                }
            } else {
                itemView.lblPorcentajeAsis_rowdetalleasistencia.layoutParams.width = lenght3.toInt()
                itemView.lblPorcentajeAsis_rowdetalleasistencia.x = (anchoAsistencia / 2) - (lenght3 / 2)
                itemView.lblPorcentajeAsis_rowdetalleasistencia.text = "%.1f".format(porcAsistencia) +"%"
            }
        }

        if (porcTardanza < 1.0f) {
            itemView.lblPorcentajeTard_rowdetalleasistencia.text = ""
        } else {
            if (porcTardanza >= 1.0f && porcTardanza < 10.0f) {
                if (anchoTardaza >= lenght1) {
                    itemView.lblPorcentajeTard_rowdetalleasistencia.layoutParams.width = lenght1.toInt()
                    itemView.lblPorcentajeTard_rowdetalleasistencia.x = (anchoAsistenciaReal + itemView.viewAsistencia_rowdetalleasistencia.x) + ((anchoTardaza / 2) - lenght1 / 2) //itemView.viewAsistencia_rowdetalleasistencia.layoutParams.width.toFloat()
                    itemView.lblPorcentajeTard_rowdetalleasistencia.text = "%.1f".format(porcTardanza) +"%"
                } else {
                    itemView.lblPorcentajeTard_rowdetalleasistencia.text = ""
                }
            } else if (porcTardanza >= 10.0f && porcTardanza < 100.0f) {
                if (anchoTardaza >= lenght2) {
                    itemView.lblPorcentajeTard_rowdetalleasistencia.layoutParams.width = lenght2.toInt()
                    itemView.lblPorcentajeTard_rowdetalleasistencia.x = (anchoAsistenciaReal + itemView.viewAsistencia_rowdetalleasistencia.x) + ((anchoTardaza / 2) - lenght2 / 2) //itemView.viewAsistencia_rowdetalleasistencia.layoutParams.width.toFloat()
                    itemView.lblPorcentajeTard_rowdetalleasistencia.text = "%.1f".format(porcTardanza) +"%"
                } else {
                    itemView.lblPorcentajeTard_rowdetalleasistencia.text = ""
                }
            } else {
                itemView.lblPorcentajeTard_rowdetalleasistencia.layoutParams.width = lenght3.toInt()
                itemView.lblPorcentajeTard_rowdetalleasistencia.x = (anchoAsistenciaReal + itemView.viewAsistencia_rowdetalleasistencia.x) + ((anchoTardaza / 2) - lenght3 / 2) //itemView.viewAsistencia_rowdetalleasistencia.layoutParams.width.toFloat()
                itemView.lblPorcentajeTard_rowdetalleasistencia.text = "%.1f".format(porcTardanza) +"%"
            }
        }

        if (porcFalta < 1) {
            itemView.lblPorcentajeFalt_rowdetalleasistencia.text = ""
        } else {
            if (porcFalta >= 1 && porcFalta < 10) {
                if (anchoFalta >= lenght1) {
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.layoutParams.width = lenght1.toInt()
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.x = (anchoTardanzaReal + itemView.viewTardanza_rowdetalleasistencia.x) + ((anchoFalta / 2) - lenght1 / 2) //itemView.viewTardanza_rowdetalleasistencia.layoutParams.width.toFloat()
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.text = "%.1f".format(porcFalta) +"%"
                } else {
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.text = ""
                }
            } else if (porcFalta >= 10 && porcFalta < 100) {
                if (anchoFalta >= lenght2) {
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.layoutParams.width = lenght2.toInt()
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.x = (anchoTardanzaReal + itemView.viewTardanza_rowdetalleasistencia.x) + ((anchoFalta / 2) - lenght2 / 2) //itemView.viewTardanza_rowdetalleasistencia.layoutParams.width.toFloat()
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.text = "%.1f".format(porcFalta) +"%"
                } else {
                    itemView.lblPorcentajeFalt_rowdetalleasistencia.text = ""
                }
            } else {
                itemView.lblPorcentajeFalt_rowdetalleasistencia.layoutParams.width = lenght3.toInt()
                itemView.lblPorcentajeFalt_rowdetalleasistencia.x = (anchoTardanzaReal + itemView.viewTardanza_rowdetalleasistencia.x) + ((anchoFalta / 2) - lenght3 / 2) //itemView.viewTardanza_rowdetalleasistencia.layoutParams.width.toFloat()
                itemView.lblPorcentajeFalt_rowdetalleasistencia.text = "%.1f".format(porcFalta) +"%"
            }
        }

        if (porcRestante < 1) {
            itemView.lblPorcentajeRest_rowdetalleasistencia.text = ""
        } else {
            if (porcRestante >= 1 && porcRestante < 10) {
                if (anchoRestante >= lenght1) {
                    itemView.lblPorcentajeRest_rowdetalleasistencia.layoutParams.width = anchoRestante.toInt()
                    itemView.lblPorcentajeRest_rowdetalleasistencia.text = "%.1f".format(porcRestante) +"%"
                } else {
                    itemView.lblPorcentajeRest_rowdetalleasistencia.text = ""
                }
            } else if (porcRestante >= 10 && porcRestante < 100) {
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

        itemView.viewContentAsis_rowdetalleasistencia.layoutParams.width = anchoPantalla - (valor.toInt() * 2)
        itemView.viewContentAsis_rowdetalleasistencia.x = valor
        itemView.viewContentAsis_rowdetalleasistencia.setPadding(0, 0, 0, valor.toInt())

        return itemView
    }
}