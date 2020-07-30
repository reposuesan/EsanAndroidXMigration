package pe.edu.esan.appostgrado.adapter

import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.itemview_pregrado_pr_lab.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.entidades.PrereservaHorario

class PregradoPrereservaHorarioAdapter(private val listHorario: ArrayList<PrereservaHorario>, private var hoursAllowed: Int, var listener: HorarioListener) : androidx.recyclerview.widget.RecyclerView.Adapter<PregradoPrereservaHorarioAdapter.HorarioViewHolder>() {

    private var count: Int = 0

    private var prevIndexAllowed: Int = -2
    private var sgteIndexAllowed: Int = -2

    private var horasDisponibles: Int = -1

    private val LOG = PregradoPrereservaHorarioAdapter::class.simpleName

    init {
        horasDisponibles = hoursAllowed
    }

    interface HorarioListener{
        fun itemClick(position: Int, itemSelected: Boolean, rangoHora: String)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.itemview_pregrado_pr_lab, parent, false)

        return HorarioViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listHorario.size
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {

        val horaInicio = listHorario.get(holder.adapterPosition).horarioItemHoraInicio
        val horaFinal = listHorario.get(holder.adapterPosition).horarioItemHoraFinal

        //Texto del item
        val rangoHoras = "$horaInicio - $horaFinal"

        //Identifica el item que fue seleccionado y la posición se envía al HashMap en la Actividad donde se define el PregradoPrereservaHorarioAdapter, esto con el fin de usar dicho valor.
        //En caso el item sea deseleccionado, este es borrado del HashMap.
        holder.buttonRangoHoraItem.text = rangoHoras


        //Item seleccionado
        if(listHorario[holder.adapterPosition].itemSeleccionado) {
            holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
            holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
        //Item no seleccionado
        } else {
            holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_no_seleccionado)
            holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_black_1000))
        }

        holder.buttonRangoHoraItem.setOnClickListener {

            //El item está seleccionado
            if(listHorario[holder.adapterPosition].itemSeleccionado){
                //En caso haya más de 1 item
                if(count > 1) {
                    if (holder.adapterPosition < sgteIndexAllowed - 1 && holder.adapterPosition > prevIndexAllowed + 1) {
                        val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.deben_seleccionarse_horas_continuas_mensaje),
                            Snackbar.LENGTH_SHORT)
                        snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                        snack.show()

                    } else {
                        listHorario[holder.adapterPosition].itemSeleccionado = false
                        holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_no_seleccionado)
                        holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_black_1000))
                        if(holder.adapterPosition - 1 == prevIndexAllowed){
                            prevIndexAllowed = holder.adapterPosition
                        } else if(holder.adapterPosition + 1 == sgteIndexAllowed) {
                            sgteIndexAllowed = holder.adapterPosition
                        }
                        count--
                        //TODO: ACTUALIZAR HASHMAP
                        Log.i(LOG,count.toString())
                        listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHoras)
                    }
                }
                //En caso haya sólo 1 item seleccionado
                else {
                    listHorario[holder.adapterPosition].itemSeleccionado = false
                    holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_no_seleccionado)
                    holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_black_1000))
                    prevIndexAllowed = -2
                    sgteIndexAllowed = -2
                    count--
                    //TODO: ACTUALIZAR HASHMAP
                    Log.i(LOG,count.toString())
                    listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHoras)
                }
            // El item no está seleccionado
            } else {
                if(count < horasDisponibles){
                    when(holder.adapterPosition){
                        0 -> //Primera posición del Adapter
                            if(count == 0){
                                sgteIndexAllowed = 1
                                prevIndexAllowed = -1
                                count++
                                listHorario[holder.adapterPosition].itemSeleccionado = true
                                holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                                holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                                //TODO: ACTUALIZAR HASHMAP
                                Log.i(LOG,count.toString())
                                listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHoras)
                            } else {
                                if(holder.adapterPosition == prevIndexAllowed || holder.adapterPosition == sgteIndexAllowed){
                                    if(holder.adapterPosition == sgteIndexAllowed) {
                                        sgteIndexAllowed++
                                    } else if(holder.adapterPosition == prevIndexAllowed){
                                        prevIndexAllowed--
                                    }
                                    count++
                                    listHorario[holder.adapterPosition].itemSeleccionado = true
                                    holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                                    holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                                    //TODO: ACTUALIZAR HASHMAP
                                    Log.i(LOG,count.toString())
                                    listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHoras)
                                } else {
                                    val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.deben_seleccionarse_horas_continuas_mensaje),
                                        Snackbar.LENGTH_SHORT)
                                    snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                                    snack.show()
                                }
                            }

                        listHorario.size - 1 -> //Última posición del Adapter
                            if(count == 0){
                                sgteIndexAllowed = listHorario.size
                                prevIndexAllowed = listHorario.size - 2
                                count++
                                listHorario[holder.adapterPosition].itemSeleccionado = true
                                holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                                holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                                //ACTUALIZAR HASHMAP
                                Log.i(LOG,count.toString())
                                listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHoras)
                            } else {
                                if(holder.adapterPosition == prevIndexAllowed || holder.adapterPosition == sgteIndexAllowed){
                                    if(holder.adapterPosition == sgteIndexAllowed) {
                                        sgteIndexAllowed++
                                    } else if(holder.adapterPosition == prevIndexAllowed){
                                        prevIndexAllowed--
                                    }
                                    count++
                                    listHorario[holder.adapterPosition].itemSeleccionado = true
                                    holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                                    holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                                    //ACTUALIZAR HASHMAP
                                    Log.i(LOG,count.toString())
                                    listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHoras)
                                } else {
                                    val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.deben_seleccionarse_horas_continuas_mensaje),
                                        Snackbar.LENGTH_SHORT)
                                    snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                                    snack.show()
                                }
                            }

                        else -> //Cualquier posición intermedia en el Adapter
                            if(holder.adapterPosition == prevIndexAllowed || holder.adapterPosition == sgteIndexAllowed){
                                if(holder.adapterPosition == sgteIndexAllowed) {
                                    sgteIndexAllowed++
                                } else if(holder.adapterPosition == prevIndexAllowed){
                                    prevIndexAllowed--
                                }
                                count++
                                listHorario[holder.adapterPosition].itemSeleccionado = true
                                holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                                holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                                //ACTUALIZAR HASHMAP
                                Log.i(LOG,count.toString())
                                listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHoras)
                            } else if (count == 0){
                                prevIndexAllowed = holder.adapterPosition - 1
                                sgteIndexAllowed = holder.adapterPosition + 1
                                count++
                                listHorario[holder.adapterPosition].itemSeleccionado = true
                                holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                                holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                                //ACTUALIZAR HASHMAP
                                Log.i(LOG,count.toString())
                                listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHoras)
                            } else {
                                val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.deben_seleccionarse_horas_continuas_mensaje),
                                    Snackbar.LENGTH_SHORT)
                                snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                                snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                                snack.show()
                            }
                    }
                    /*listHorario[holder.adapterPosition].itemSeleccionado = true
                    holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                    holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                    Log.i(LOG,count.toString())
                    listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHora)
                    if(holder.adapterPosition == 0){
                    } else if (holder.adapterPosition == listHorario.size - 1) {
                    } else {
                    }*/
                //Se alcanzó el máximo número de horas disponibles
                } else {
                    val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.usted_alcanzo_maximo_numero_horas_mensaje),
                        Snackbar.LENGTH_SHORT)
                    snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                    snack.show()
                }
            }










/*            if (count == 0) {
                if (count < horasDisponibles) {
                    if (listHorario[holder.adapterPosition].itemSeleccionado) {
                        holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_no_seleccionado)
                        holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_black_1000))
                        listHorario[holder.adapterPosition].itemSeleccionado = false
                    } else {
                        holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                        holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                        listHorario[holder.adapterPosition].itemSeleccionado = true
                        if (holder.adapterPosition == 0) {
                            sgteIndexAllowed = holder.adapterPosition + 1
                        } else if (holder.adapterPosition == listHorario.size - 1) {
                            prevIndexAllowed = holder.adapterPosition - 1
                        } else {
                            prevIndexAllowed = holder.adapterPosition - 1
                            sgteIndexAllowed = holder.adapterPosition + 1
                        }
                        count++
                        listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHora)
                    }
                } else {
                    val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.usted_alcanzo_maximo_numero_horas_mensaje),Snackbar.LENGTH_SHORT)
                    snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                    snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                    snack.show()
                }
            } else if (count == 1) {
                if (count <= horasDisponibles) {
                    if (listHorario[holder.adapterPosition].itemSeleccionado) {
                        holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_no_seleccionado)
                        holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_black_1000))
                        listHorario[holder.adapterPosition].itemSeleccionado = false
                        count--
                        prevIndexAllowed = -1
                        sgteIndexAllowed = -1
                        listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHora)
                    } else {
                        if(count == horasDisponibles){
                            val snack = Snackbar.make(holder.view, holder.view.context.getString(R.string.usted_alcanzo_maximo_numero_horas_mensaje),Snackbar.LENGTH_SHORT)
                            snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                            snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                            snack.show()
                        } else {
                            if (holder.adapterPosition == prevIndexAllowed || holder.adapterPosition == sgteIndexAllowed) {
                                holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                                holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                                listHorario[holder.adapterPosition].itemSeleccionado = true
                                count++
                                listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHora)
                                if (holder.adapterPosition == prevIndexAllowed) {
                                    prevIndexAllowed--
                                } else {
                                    sgteIndexAllowed++
                                }
                            } else {
                                val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.deben_seleccionarse_horas_continuas_mensaje),Snackbar.LENGTH_SHORT)
                                snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                                snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                                snack.show()
                            }
                        }

                    }
                } else {
                    val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.usted_alcanzo_maximo_numero_horas_mensaje),Snackbar.LENGTH_SHORT)
                    snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                    snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                    snack.show()
                }
            } else if (count == 2) {
                if (count <= horasDisponibles) {
                    if (listHorario[holder.adapterPosition].itemSeleccionado) {
                        holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_no_seleccionado)
                        holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_black_1000))
                        listHorario[holder.adapterPosition].itemSeleccionado = false
                        count--
                        listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHora)
                        if(holder.adapterPosition + 1 == sgteIndexAllowed){
                            sgteIndexAllowed = holder.adapterPosition
                        } else if(holder.adapterPosition - 1 == prevIndexAllowed){
                            prevIndexAllowed = holder.adapterPosition
                        }
                    } else {
                        if(count == horasDisponibles){
                            val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.usted_alcanzo_maximo_numero_horas_mensaje),Snackbar.LENGTH_SHORT)
                            snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                            snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                            snack.show()
                        } else {
                            if (holder.adapterPosition == prevIndexAllowed || holder.adapterPosition == sgteIndexAllowed) {
                                holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                                holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                                listHorario[holder.adapterPosition].itemSeleccionado = true
                                count++
                                listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHora)
                                if (holder.adapterPosition == prevIndexAllowed) {
                                    prevIndexAllowed--
                                } else {
                                    sgteIndexAllowed++
                                }
                            } else {
                                val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.deben_seleccionarse_horas_continuas_mensaje),Snackbar.LENGTH_SHORT)
                                snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                                snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                                snack.show()
                            }
                        }

                    }
                } else {
                    val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.usted_alcanzo_maximo_numero_horas_mensaje),Snackbar.LENGTH_SHORT)
                    snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                    snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                    snack.show()
                }
            } else if (count == 3) {
                if (count <= horasDisponibles) {
                    if (listHorario[holder.adapterPosition].itemSeleccionado) {
                        if(holder.adapterPosition < sgteIndexAllowed - 1 && holder.adapterPosition > prevIndexAllowed + 1) {
                            val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.deben_seleccionarse_horas_continuas_mensaje),Snackbar.LENGTH_SHORT)
                            snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                            snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                            snack.show()
                        } else {
                            holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_no_seleccionado)
                            holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_black_1000))
                            listHorario[holder.adapterPosition].itemSeleccionado = false
                            count--
                            listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHora)
                            if (holder.adapterPosition + 1 == sgteIndexAllowed) {
                                sgteIndexAllowed = holder.adapterPosition
                            } else if (holder.adapterPosition - 1 == prevIndexAllowed) {
                                prevIndexAllowed = holder.adapterPosition
                            }
                        }
                    } else {
                        if(count == horasDisponibles){
                            val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.usted_alcanzo_maximo_numero_horas_mensaje),Snackbar.LENGTH_SHORT)
                            snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                            snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                            snack.show()
                        } else {
                            if (holder.adapterPosition == prevIndexAllowed || holder.adapterPosition == sgteIndexAllowed) {
                                holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_seleccionado_rango_hora)
                                holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_white_1000))
                                listHorario[holder.adapterPosition].itemSeleccionado = true
                                count++
                                listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHora)
                                if (holder.adapterPosition == prevIndexAllowed) {
                                    prevIndexAllowed--
                                } else {
                                    sgteIndexAllowed++
                                }
                            } else {
                                val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.deben_seleccionarse_horas_continuas_mensaje),Snackbar.LENGTH_SHORT)
                                snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                                snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                                snack.show()
                            }
                        }

                    }
                } else {
                    val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.usted_alcanzo_maximo_numero_horas_mensaje),Snackbar.LENGTH_SHORT)
                    snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                    snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                    snack.show()
                }
            } else if (count == 4) {
                //Si el item ha sido seleccionado ingresa al bloque if
                if (listHorario[holder.adapterPosition].itemSeleccionado) {
                    //En caso se seleccione items entre el 1er y 4to item
                    if(holder.adapterPosition < sgteIndexAllowed - 1 && holder.adapterPosition > prevIndexAllowed + 1) {
                        val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.deben_seleccionarse_horas_continuas_mensaje),Snackbar.LENGTH_SHORT)
                        snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                        snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                        snack.show()
                    } else {
                        //En caso se seleccione items del 1er y 4to item
                        holder.buttonRangoHoraItem.setBackgroundResource(R.drawable.shape_item_no_seleccionado)
                        holder.buttonRangoHoraItem.setTextColor(ContextCompat.getColor(holder.view.context, R.color.md_black_1000))
                        listHorario[holder.adapterPosition].itemSeleccionado = false
                        count--
                        listener.itemClick(holder.adapterPosition, listHorario[holder.adapterPosition].itemSeleccionado, rangoHora)
                        if (holder.adapterPosition + 1 == sgteIndexAllowed) {
                            sgteIndexAllowed = holder.adapterPosition
                        } else if (holder.adapterPosition - 1 == prevIndexAllowed) {
                            prevIndexAllowed = holder.adapterPosition
                        }
                    }

                } else {
                    //Se alcanzó máximo número de horas
                    val snack = Snackbar.make(holder.view,holder.view.context.getString(R.string.usted_alcanzo_maximo_numero_horas_mensaje),Snackbar.LENGTH_SHORT)
                    snack.view.setBackgroundColor(ContextCompat.getColor(holder.view.context, R.color.warning))
                    snack.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(holder.view.context, R.color.warning_text))
                    snack.show()
                }

            }*/


        }
    }

    class HorarioViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val buttonRangoHoraItem: Button = view.button_item
    }

}