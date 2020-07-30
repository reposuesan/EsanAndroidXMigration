package pe.edu.esan.appostgrado.adapter

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.util.Util
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import kotlinx.android.synthetic.main.item_resultadoencuesta_pregunta.view.*
import kotlinx.android.synthetic.main.item_resultadoencuesta_resumen.view.*
import kotlinx.android.synthetic.main.item_titulosimplegris.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.GrupoPregunta
import pe.edu.esan.appostgrado.entidades.ResultadoEncuesta
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 18/06/18.
 */
class ResultadoEncuestaAdapter(val resultadoEncuesta: ResultadoEncuesta, val densidad: Int) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            CABECERA
        else if (position == 1)
            RESUMEN
        else
            if (resultadoEncuesta.preguntas[position-2].tipo == Utilitarios.TipoFila.CABECERA) CABECERA else PREGUNTA

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            CABECERA -> CabeceraResultadoEncuestaHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_titulosimplegris, parent, false))
            RESUMEN -> ResumenResultadoEncuestaHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_resultadoencuesta_resumen, parent, false))
            else -> PreguntaResultadoEncuestaHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_resultadoencuesta_pregunta, parent, false))
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CabeceraResultadoEncuestaHolder -> holder.setValores(resultadoEncuesta, position)
            is ResumenResultadoEncuestaHolder -> holder.setValores(resultadoEncuesta, densidad)
            is PreguntaResultadoEncuestaHolder -> holder.setValores(resultadoEncuesta.preguntas[position-2])
        }
    }

    override fun getItemCount(): Int {
        return if (resultadoEncuesta.preguntas.isEmpty()) 0 else resultadoEncuesta.preguntas.size + 2
    }

    class CabeceraResultadoEncuestaHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(resultado: ResultadoEncuesta, position: Int) {
            itemView.lblTitulo_generico.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            if (position == 0)
                itemView.lblTitulo_generico.text = resultado.titulo
            else
                itemView.lblTitulo_generico.text = resultado.preguntas[position-2].grupo
        }
    }

    class ResumenResultadoEncuestaHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(resultado: ResultadoEncuesta, densidad: Int) {

            itemView.davParticipantes_itemcabresultencuesta.configureAngles(270, 0)

            val seriesItem = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
                    .setRange(0f, 5.0f, 0f)
                    .setInitialVisibility(true)
                    .build()

            val mBackIndex = itemView.davParticipantes_itemcabresultencuesta.addSeries(seriesItem)

            val seriesItem1 = SeriesItem.Builder(Color.parseColor("#FF3478F6"))
                    .setRange(0f, 5.0f, 0f)
                    .setInitialVisibility(false)
                    .build()

            seriesItem1.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
                override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
                    val percentFilled = (currentPosition - seriesItem1.minValue) / (seriesItem1.maxValue - seriesItem1.minValue)
                    itemView.lblPorcentaje_itemcabresultencuesta.text = String.format("%.0f%%", percentFilled * 100f)
                }

                override fun onSeriesItemDisplayProgress(percentComplete: Float) {

                }
            })

            seriesItem1.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
                override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
                    itemView.lblParticipantes_itemcabresultencuesta.text = String.format("%.2f", seriesItem1.minValue + currentPosition)

                }

                override fun onSeriesItemDisplayProgress(percentComplete: Float) {

                }
            })

            val mSeries1Intex = itemView.davParticipantes_itemcabresultencuesta.addSeries(seriesItem1)
            itemView.davParticipantes_itemcabresultencuesta.executeReset()
            itemView.davParticipantes_itemcabresultencuesta.addEvent(DecoEvent.Builder(5.0f)
                    .setIndex(mBackIndex)
                    .setDuration(2000)
                    .setDelay(100)
                    .build())

            itemView.davParticipantes_itemcabresultencuesta.addEvent(DecoEvent.Builder(resultado.AVG.toFloat())
                    .setIndex(mSeries1Intex)
                    .setDuration(1500)
                    .setDelay(1000)
                    .build())



            //val altura = (320 * densidad * 0.5f)
            val altura = (160 * densidad / 160f)

            //println("------------------")

            val  siParticiparon: Float

            if(resultado.totalEncuesta == 0){
                siParticiparon = 1.0f
            } else {
                siParticiparon = resultado.siEvaluaron.toFloat() / resultado.totalEncuesta.toFloat()
            }
            // val noParticiparon = resultado.noEvaluaron.toFloat()/ resultado.totalEncuesta.toFloat()

            itemView.lblAP_itemcabresultencuesta.text = String.format(itemView.resources.getString(R.string.valor_porcentaje), resultado.siEvaluaron, siParticiparon * 100)
            //itemView.lblDP_itemcabresultencuesta.text = String.format(itemView.resources.getString(R.string.valor_porcentaje), resultado.noEvaluaron, noParticiparon * 100)

            itemView.viewAP_itemcabresultencuesta.layoutParams.height = (altura * siParticiparon).toInt()
            //itemView.viewDP_itemcabresultencuesta.layoutParams.height = (altura * noParticiparon).toInt()
        }
    }

    class PreguntaResultadoEncuestaHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(grupoPregunta: GrupoPregunta) {

            itemView.lblPregunta_ipresultadoencuesta.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblAP_ipresultadoencuesta.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
            itemView.lblDP_ipresultadoencuesta.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
            itemView.lblAVG_ipresultadoencuesta.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)


            itemView.lblPregunta_ipresultadoencuesta.text = grupoPregunta.pregunta
            itemView.lblAP_ipresultadoencuesta.text = String.format(itemView.resources.getString(R.string.ap_porcentaje), grupoPregunta.AP)
            itemView.lblDP_ipresultadoencuesta.text = String.format(itemView.resources.getString(R.string.dp_porcentaje), grupoPregunta.DP)
            itemView.lblAVG_ipresultadoencuesta.text = String.format(itemView.resources.getString(R.string.avg_porcentaje), grupoPregunta.AVG)
        }
    }

    private val CABECERA = 0
    private val RESUMEN = 1
    private val PREGUNTA = 2

}