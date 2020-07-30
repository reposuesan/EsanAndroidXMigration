package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_encuesta_cabecera.view.*
import kotlinx.android.synthetic.main.item_encuesta_preguntados.view.*
import kotlinx.android.synthetic.main.item_encuesta_preguntauno.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.PreguntaEncuesta
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 25/05/18.
 */
class EncuestaAdapter (val listaPreguntas: List<PreguntaEncuesta>, val changeListener: () -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        when (listaPreguntas[position].tipo) {
            Utilitarios.TipoPreguntaEncuesta.CABECERA -> {
                return 1
            }
            Utilitarios.TipoPreguntaEncuesta.PREGUNTAUNO -> {
                return 2
            }
            Utilitarios.TipoPreguntaEncuesta.PREGUNTADOS -> {
                return 3
            }
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val pregunta = listaPreguntas[position]
        when (holder) {
            is EncuestaCabeceraHolder -> {
                holder.setValores(pregunta)
            }
            is EncuestaPreguntaUnoHolder -> {
                holder.setValores(pregunta, changeListener)
            }
            is EncuestaPreguntaDosHolder -> {
                holder.setValores(pregunta, changeListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        if (viewType == 1) {
            return EncuestaCabeceraHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_encuesta_cabecera, parent, false))
        } else if (viewType == 2) {
            return EncuestaPreguntaUnoHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_encuesta_preguntauno, parent, false))
        } else {
            return EncuestaPreguntaDosHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_encuesta_preguntados, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return listaPreguntas.size
    }

    class EncuestaCabeceraHolder (view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(pregunta: PreguntaEncuesta) {
            itemView.lblCabecera_encuestacabecera.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblCabecera_encuestacabecera.text = pregunta.cabecera
        }
    }

    class EncuestaPreguntaUnoHolder (view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(pregunta: PreguntaEncuesta, changeListener: () -> Unit) {
            itemView.lblPregunta_encuestapreguntauno.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblPregunta_encuestapreguntauno.text = pregunta.pregunta

            if (pregunta.puntaje == "0") {
                itemView.sgRespuesta_encuestapreguntauno.check(R.id.ck_default)
            } else {
                when (pregunta.puntaje) {
                    "1" -> itemView.sgRespuesta_encuestapreguntauno.check(R.id.ck_rpta1)
                    "2" -> itemView.sgRespuesta_encuestapreguntauno.check(R.id.ck_rpta2)
                    "3" -> itemView.sgRespuesta_encuestapreguntauno.check(R.id.ck_rpta3)
                    "4" -> itemView.sgRespuesta_encuestapreguntauno.check(R.id.ck_rpta4)
                    "5" -> itemView.sgRespuesta_encuestapreguntauno.check(R.id.ck_rpta5)
                }
            }

            itemView.sgRespuesta_encuestapreguntauno.setOnCheckedChangeListener { radioGroup, i ->
                when (i) {
                    R.id.ck_rpta1 -> pregunta.puntaje = "1"
                    R.id.ck_rpta2 -> pregunta.puntaje = "2"
                    R.id.ck_rpta3 -> pregunta.puntaje = "3"
                    R.id.ck_rpta4 -> pregunta.puntaje = "4"
                    R.id.ck_rpta5 -> pregunta.puntaje = "5"
                    else -> pregunta.puntaje = "0"
                }
                changeListener()
                itemView.sgRespuesta_encuestapreguntauno.setOnCheckedChangeListener(null)
            }
        }
    }

    class EncuestaPreguntaDosHolder (view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(pregunta: PreguntaEncuesta, changeListener: () -> Unit) {
            itemView.lblPregunta_encuestapreguntados.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblPregunta_encuestapreguntados.text = pregunta.pregunta

            itemView.txtRespuesta_encuestapreguntados.setText(pregunta.respuesta)
            itemView.txtRespuesta_encuestapreguntados.addTextChangedListener(object :TextWatcher {
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    println("cambia -> "+ p0)
                    pregunta.respuesta = if (p0 == null) "" else p0.toString()
                    changeListener()
                }
            })
        }
    }
}