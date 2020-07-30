package pe.edu.esan.appostgrado.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_almuerzo_cabecera.view.*
import kotlinx.android.synthetic.main.item_almuerzo_detalle.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.AlmuerzoComedor

/**
 * Created by lventura on 2/08/18.
 */
class AlmuerzoAdapter(val listaAlmuerzo: ArrayList<AlmuerzoComedor>): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return listaAlmuerzo[position].tipopcion
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                AlmuerzoCabeceraHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_almuerzo_cabecera, parent, false))
            }
            else -> {
                AlmuerzoDetalleHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_almuerzo_detalle, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AlmuerzoCabeceraHolder -> {
                holder.setValores(listaAlmuerzo[position])
            }
            is AlmuerzoDetalleHolder -> {
                holder.setValores(listaAlmuerzo[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return listaAlmuerzo.size
    }

    class AlmuerzoCabeceraHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(cabecera: AlmuerzoComedor) {
            itemView.lblCabecera_ialmuerzo.text = cabecera.cabecera
        }
    }

    class AlmuerzoDetalleHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores(almuerzo: AlmuerzoComedor) {
            itemView.lblTitulo_ialmuerzo.text = almuerzo.descripcion
            itemView.lblEntrada_ialmuerzo.text = almuerzo.entrada
            itemView.lblSopa_ialmuerzo.text = almuerzo.sopa

            val segundo = almuerzo.segundo?.split("-")
            try {
                if (segundo?.size == 1) {
                    itemView.lblFondoUno_ialmuerzo.text = "- ${segundo[0].trim()}"
                    itemView.lblFondoDos_ialmuerzo.text = ""
                } else {
                    if (segundo?.size == 2) {
                        itemView.lblFondoUno_ialmuerzo.text = "- ${segundo[0].trim()}"
                        itemView.lblFondoDos_ialmuerzo.text = "- ${segundo[1].trim()}"
                    } else {
                        itemView.lblFondoUno_ialmuerzo.text = almuerzo.segundo
                        itemView.lblFondoDos_ialmuerzo.text = ""
                    }
                }
            } catch (ex: Exception) {
                itemView.lblFondoUno_ialmuerzo.text = almuerzo.segundo
                itemView.lblFondoDos_ialmuerzo.text = ""
            }

            itemView.lblPostre_ialmuerzo.text = almuerzo.postre
            itemView.lblRefresco_ialmuerzo.text = almuerzo.refresco
            itemView.lblPrecio_ialmuerzo.text = "S/ ${almuerzo.precio}"
        }
    }
}