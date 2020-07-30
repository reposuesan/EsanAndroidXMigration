package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_cargacademica_actividad.view.*
import kotlinx.android.synthetic.main.item_cargacademica_cabecera.view.*
import kotlinx.android.synthetic.main.item_cargacademica_tipo.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.Actividad
import pe.edu.esan.appostgrado.entidades.CargaAcademica
import pe.edu.esan.appostgrado.entidades.TipoCargaAcademica
import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 28/05/18.
 */
class CargaAcademicaAdapter (val cargaAcademica: CargaAcademica): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
             CABECERA
        else
            getTipoCelda(position-1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        when (viewType) {
            CABECERA -> return CabeceraHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_cargacademica_cabecera, parent, false))
            TIPOCARGA -> return TipoHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_cargacademica_tipo, parent, false))
            else -> return ActividadHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_cargacademica_actividad, parent, false))
        }
    }


    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CabeceraHolder -> {
                holder.setValores(cargaAcademica.mes, cargaAcademica.agno, cargaAcademica.acumulado, cargaAcademica.total)
            }
            is TipoHolder -> {
                holder.setValores(getPosicionReal(position - 1))
            }
            is ActividadHolder -> {
                holder.setValores(getPosicionReal(position - 1))
            }
        }
    }

    override fun getItemCount(): Int {
        var cantidad = 0
        for (i in 0 until cargaAcademica.listaCargas.size) {
            cantidad += cargaAcademica.listaCargas[i].actividades.size
        }

        return cantidad + cargaAcademica.listaCargas.size + 1
    }

    private fun getPosicionReal(position: Int): Any? {
        var contador = 0
        for (i in 0 until cargaAcademica.listaCargas.size) {
            if (contador == position) return cargaAcademica.listaCargas[i]
            for (z in 0 until cargaAcademica.listaCargas[i].actividades.size) {
                contador++
                if (contador == position) {
                    if (cargaAcademica.listaCargas[i].actividades[z] == null){
                        var contDet = 0
                        do {
                            contDet++
                        } while (cargaAcademica.listaCargas[i].actividades[z-contDet] == null)
                    } else return cargaAcademica.listaCargas[i].actividades[z]
                }
            }
            contador++
        }
        return null
    }

    private fun getTipoCelda(position: Int): Int {
        var contador = 0
        for (i in 0 until cargaAcademica.listaCargas.size) {
            if (contador == position) return TIPOCARGA
            for (z in 0 until cargaAcademica.listaCargas[i].actividades.size) {
                contador++
                if (contador == position) {
                    if (cargaAcademica.listaCargas[i].actividades[z] == null) return ACTIVIDADDETALLE
                    else return ACTIVIDAD
                }
            }
            contador++
        }
        return 4
    }

    private class CabeceraHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(mes: String, agno: String, esacumuado: Boolean, total: Double) {
            itemView.lblFecha_cargacademicacabecera.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)
            itemView.lblTotal_cargacademicacabecera.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.MEDIUM)

            if (esacumuado) {
                itemView.lblFecha_cargacademicacabecera.text = String.format(itemView.resources.getString(R.string.carga_meses_uno), mes, agno)
            } else {
                itemView.lblFecha_cargacademicacabecera.text = String.format(itemView.resources.getString(R.string.carga_meses_dos), mes, agno)
            }
            itemView.lblTotal_cargacademicacabecera.text = String.format(itemView.resources.getString(R.string.sesiones_), total)
        }
    }

    private class TipoHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(tipoCarga: Any?) {
            when (tipoCarga) {
                is TipoCargaAcademica -> {
                    itemView.lblTipo_cargacademicatipo.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
                    itemView.lblTotal_cargacademicatipo.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

                    itemView.lblTipo_cargacademicatipo.text = tipoCarga.tipo
                    itemView.lblTotal_cargacademicatipo.text = String.format(itemView.resources.getString(R.string.decimal), tipoCarga.sesiones)
                }
            }
        }
    }

    private class ActividadHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(actividad: Any?) {
            when (actividad) {
                is Actividad -> {
                    itemView.lblSSesiones_cargacademicactividad.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN)
                    itemView.lblActividad_cargacademicactividad.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT_ITALIC)
                    itemView.lblSesiones_cargacademicactividad.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

                    itemView.lblActividad_cargacademicactividad.text = actividad.actividad
                    itemView.lblSesiones_cargacademicactividad.text = String.format(itemView.resources.getString(R.string.decimal), actividad.sesiones)
                }
            }
        }
    }

    private val CABECERA = 0
    private val TIPOCARGA = 1
    private val ACTIVIDAD = 2
    private val ACTIVIDADDETALLE = 3
}