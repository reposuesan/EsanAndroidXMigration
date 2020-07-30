package pe.edu.esan.appostgrado.adapter

import android.content.Intent
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_horariodetalle_alumno_lista.view.*
import kotlinx.android.synthetic.main.item_horariodetalle_profesor_lista.view.*
import kotlinx.android.synthetic.main.item_horariodetalle_profesorestadoasistencia_lista.view.*
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.control.ControlUsuario
import pe.edu.esan.appostgrado.entidades.Horario
import pe.edu.esan.appostgrado.util.Utilitarios
import pe.edu.esan.appostgrado.view.mas.ra.PrincipalRAActivity

/**
 * Created by lventura on 10/05/18.
 */
class HorarioDetalleAdapter (val listaHorario: List<Horario>, val clickListener: (Horario, Int, Utilitarios.TipoClick) -> Unit): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return listaHorario[position].tipoHorario
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        if (viewType == HORARIOALUMNO) {
            return HorarioAlumnoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_horariodetalle_alumno_lista, parent, false))
        } else if (viewType == HORARIOPROFESOR) {
            return HorarioProfesorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_horariodetalle_profesor_lista, parent, false))
        } else { //HORARIOPROFESORASISTENCIA
            return HorarioProfesorAsistenciaViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_horariodetalle_profesorestadoasistencia_lista, parent, false))
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val horario = listaHorario[position]
        when (holder) {
            is HorarioAlumnoViewHolder -> {
                holder.setValores(horario)
            }
            is HorarioProfesorViewHolder -> {
                holder.setValores(horario)
            }
            is HorarioProfesorAsistenciaViewHolder -> {
                holder.setValores(horario, clickListener)
            }
        }
    }

    override fun getItemCount(): Int {
        return listaHorario.size
    }

    class HorarioAlumnoViewHolder (view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        fun setValores (horario: Horario) {
            itemView.lblHoraFin_ahorarioalumnodetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.BOLD)
            itemView.lblHoraInicio_ahorarioalumnodetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.BOLD)

            itemView.lblSAula_ahorarioalumnodetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblSCurso_ahorarioalumnodetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblSProfesor_ahorarioalumnodetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

            itemView.lblAula_ahorarioalumnodetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblCurso_ahorarioalumnodetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblProfesor_ahorarioalumnodetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

            itemView.lblHoraInicio_ahorarioalumnodetalle.text = horario.horaInicio
            itemView.lblHoraFin_ahorarioalumnodetalle.text = horario.horaFin
            itemView.lblAula_ahorarioalumnodetalle.text = horario.aula
            itemView.lblCurso_ahorarioalumnodetalle.text = horario.curso
            itemView.lblProfesor_ahorarioalumnodetalle.text = if (horario.idProfesorReem == -1) horario.profesor else horario.profesorReem

            itemView.imgBtnRA_ahorarioalumnodetalle.visibility = if (horario.idAmbiente == -1) View.GONE else View.VISIBLE
            itemView.imgBtnRA_ahorarioalumnodetalle.setImageResource(if (Utilitarios.comprobarSensor(itemView.context)) R.drawable.raglass else R.drawable.tab_ra_unselect)
            itemView.imgBtnRA_ahorarioalumnodetalle.setOnClickListener {
                println("CLICK IMAGEN RA")
                if (Utilitarios.comprobarSensor(itemView.context)) {
                    if (ControlUsuario.instance.accesoCamara) {
                        if (ControlUsuario.instance.accesoGPS) {
                            val intentAR = Intent(itemView.context, PrincipalRAActivity::class.java)
                            intentAR.putExtra("accion", "individual")
                            intentAR.putExtra("buscar", horario.idAmbiente.toString())
                            itemView.context.startActivity(intentAR)
                        } else {
                            //val snack = Snackbar.make(itemView.findViewById(android.R.id.content), itemView.resources.getString(R.string.error_permiso_gps), Snackbar.LENGTH_LONG)
                            val snack = Snackbar.make(itemView, itemView.resources.getString(R.string.error_permiso_gps), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(itemView.context, R.color.warning_text))
                            snack.show()
                        }
                    } else {
                        //val snack = Snackbar.make(itemView.findViewById(android.R.id.content), itemView.resources.getString(R.string.error_permiso_camara), Snackbar.LENGTH_LONG)
                        val snack = Snackbar.make(itemView, itemView.resources.getString(R.string.error_permiso_camara), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(itemView.context, R.color.warning_text))
                        snack.show()
                    }
                } else {
                    //val snack = Snackbar.make(itemView.findViewById(android.R.id.content), itemView.resources.getString(R.string.error_sensores_ra), Snackbar.LENGTH_LONG)
                    val snack = Snackbar.make(itemView, itemView.resources.getString(R.string.error_sensores_ra), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(itemView.context, R.color.danger_text))
                    snack.show()
                }
            }
        }
    }

    class HorarioProfesorViewHolder (view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun setValores (horario: Horario) {
            itemView.lblHoraFin_ahorarioprofesordetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.BOLD)
            itemView.lblHoraInicio_ahorarioprofesordetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.BOLD)

            itemView.lblSeccion_ahorarioprofesordetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR_ITALIC)
            itemView.lblSesion_ahorarioprofesordetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR_ITALIC)

            itemView.lblSAula_ahorarioprofesordetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblSCurso_ahorarioprofesordetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

            itemView.lblAula_ahorarioprofesordetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblCurso_ahorarioprofesordetalle.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

            itemView.lblHoraInicio_ahorarioprofesordetalle.text = horario.horaInicio
            itemView.lblHoraFin_ahorarioprofesordetalle.text = horario.horaFin
            itemView.lblAula_ahorarioprofesordetalle.text = horario.aula
            itemView.lblCurso_ahorarioprofesordetalle.text = horario.curso

            itemView.lblSeccion_ahorarioprofesordetalle.text = String.format(itemView.context.resources.getString(R.string.seccion_, horario.seccionCodigo))
            itemView.lblSesion_ahorarioprofesordetalle.text = String.format(itemView.context.resources.getString(R.string.sesion_n_, horario.numSesionReal))
            itemView.imgBtnRA_ahorarioprofesordetalle.visibility = if (horario.idAmbiente == -1) View.GONE else View.VISIBLE
            itemView.imgBtnRA_ahorarioprofesordetalle.setImageResource(if (Utilitarios.comprobarSensor(itemView.context)) R.drawable.raglass else R.drawable.tab_ra_unselect)
            itemView.imgBtnRA_ahorarioprofesordetalle.setOnClickListener {
                println("CLICK IMAGEN RA")
                if (Utilitarios.comprobarSensor(itemView.context)) {
                    if (ControlUsuario.instance.accesoCamara) {
                        if (ControlUsuario.instance.accesoGPS) {
                            val intentAR = Intent(itemView.context, PrincipalRAActivity::class.java)
                            intentAR.putExtra("accion", "individual")
                            intentAR.putExtra("buscar", horario.idAmbiente.toString())
                            itemView.context.startActivity(intentAR)
                        } else {
                            //val snack = Snackbar.make(itemView.findViewById(android.R.id.content), itemView.resources.getString(R.string.error_permiso_gps), Snackbar.LENGTH_LONG)
                            val snack = Snackbar.make(itemView, itemView.resources.getString(R.string.error_permiso_gps), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(itemView.context, R.color.warning_text))
                            snack.show()
                        }
                    } else {
                        //val snack = Snackbar.make(itemView.findViewById(android.R.id.content), itemView.resources.getString(R.string.error_permiso_camara), Snackbar.LENGTH_LONG)
                        val snack = Snackbar.make(itemView, itemView.resources.getString(R.string.error_permiso_camara), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(itemView.context, R.color.warning_text))
                        snack.show()
                    }
                } else {
                    //val snack = Snackbar.make(itemView.findViewById(android.R.id.content), itemView.resources.getString(R.string.error_sensores_ra), Snackbar.LENGTH_LONG)
                    val snack = Snackbar.make(itemView, itemView.resources.getString(R.string.error_sensores_ra), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(itemView.context, R.color.danger_text))
                    snack.show()
                }
            }
        }
    }

    class HorarioProfesorAsistenciaViewHolder (view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {


        fun setValores (horario: Horario, clickListener: (Horario, Int, Utilitarios.TipoClick) -> Unit?) {
            itemView.lblEstadoAsis_ahorarioprofesorasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.THIN_ITALIC)

            itemView.lblHoraFin_ahorarioprofesorasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.BOLD)
            itemView.lblHoraInicio_ahorarioprofesorasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.BOLD)

            itemView.lblSeccion_ahorarioprofesorasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR_ITALIC)
            itemView.lblSesion_ahorarioprofesorasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR_ITALIC)

            itemView.lblSAula_ahorarioprofesorasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
            itemView.lblSCurso_ahorarioprofesorasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)

            itemView.lblAula_ahorarioprofesorasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)
            itemView.lblCurso_ahorarioprofesorasistencia.typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.LIGHT)

            itemView.lblSeccion_ahorarioprofesorasistencia.text = String.format(itemView.context.resources.getString(R.string.seccion_, horario.seccionCodigo))
            itemView.lblSesion_ahorarioprofesorasistencia.text = String.format(itemView.context.resources.getString(R.string.sesion_n_, horario.numSesionReal))

            itemView.lblHoraInicio_ahorarioprofesorasistencia.text = horario.horaInicio
            itemView.lblHoraFin_ahorarioprofesorasistencia.text = horario.horaFin
            itemView.lblAula_ahorarioprofesorasistencia.text = horario.aula
            itemView.lblCurso_ahorarioprofesorasistencia.text = horario.curso

            if (horario.tipoHorario == 3) {

                if (horario.takeAssist == 1) {
                    itemView.lblEstadoAsis_ahorarioprofesorasistencia.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                    itemView.lblEstadoAsis_ahorarioprofesorasistencia.text = itemView.resources.getString(R.string.asistencia_modificar)
                    itemView.imgTomarAsis_horarioprofesorestasis.setImageResource(R.drawable.ico_tomoasistencia)
                } else {
                    itemView.lblEstadoAsis_ahorarioprofesorasistencia.setTextColor(ContextCompat.getColor(itemView.context, R.color.s3))
                    itemView.lblEstadoAsis_ahorarioprofesorasistencia.text = itemView.resources.getString(R.string.asistencia_tomar)
                    itemView.imgTomarAsis_horarioprofesorestasis.setImageResource(R.drawable.ico_tomarasistencia)
                }

                itemView.viewTomarAsis_ahorarioprofesorasistencia.setOnClickListener {
                    println("CLICK TOMAR")
                    clickListener (horario, layoutPosition, Utilitarios.TipoClick.OnCLick)
                }

            } else {
                if (horario.takeAssist == 1) {
                    itemView.lblEstadoAsis_ahorarioprofesorasistencia.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                    itemView.lblEstadoAsis_ahorarioprofesorasistencia.text = itemView.resources.getString(R.string.asistencia_tomada)
                    itemView.imgTomarAsis_horarioprofesorestasis.setImageResource(R.drawable.ico_success)
                    itemView.viewTomarAsis_ahorarioprofesorasistencia.setOnLongClickListener {
                        println("COPIAR ASISTENCIA")
                        println(layoutPosition)
                        clickListener (horario, layoutPosition, Utilitarios.TipoClick.OnLongClick)
                        true
                    }
                } else {
                    itemView.lblEstadoAsis_ahorarioprofesorasistencia.setTextColor(ContextCompat.getColor(itemView.context, R.color.s3))
                    itemView.lblEstadoAsis_ahorarioprofesorasistencia.text = itemView.resources.getString(R.string.asistencia_notomada)
                    itemView.imgTomarAsis_horarioprofesorestasis.setImageResource(R.drawable.ico_error)
                    itemView.viewTomarAsis_ahorarioprofesorasistencia.setOnLongClickListener { false }
                }
            }

            itemView.imgBtnRA_ahorarioprofesorasistencia.visibility = if (horario.idAmbiente == -1) View.GONE else View.VISIBLE
            itemView.imgBtnRA_ahorarioprofesorasistencia.setImageResource(if (Utilitarios.comprobarSensor(itemView.context)) R.drawable.raglass else R.drawable.tab_ra_unselect)
            itemView.imgBtnRA_ahorarioprofesorasistencia.setOnClickListener {
                println("CLICK IMAGEN RA")
                if (Utilitarios.comprobarSensor(itemView.context)) {
                    if (ControlUsuario.instance.accesoCamara) {
                        if (ControlUsuario.instance.accesoGPS) {
                            val intentAR = Intent(itemView.context, PrincipalRAActivity::class.java)
                            intentAR.putExtra("accion", "individual")
                            intentAR.putExtra("buscar", horario.idAmbiente.toString())
                            itemView.context.startActivity(intentAR)
                        } else {
                            //val snack = Snackbar.make(itemView.findViewById(android.R.id.content), itemView.resources.getString(R.string.error_permiso_gps), Snackbar.LENGTH_LONG)
                            val snack = Snackbar.make(itemView, itemView.resources.getString(R.string.error_permiso_gps), Snackbar.LENGTH_LONG)
                            snack.view.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.warning))
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
                            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(itemView.context, R.color.warning_text))
                            snack.show()
                        }
                    } else {
                        //val snack = Snackbar.make(itemView.findViewById(android.R.id.content), itemView.resources.getString(R.string.error_permiso_camara), Snackbar.LENGTH_LONG)
                        val snack = Snackbar.make(itemView, itemView.resources.getString(R.string.error_permiso_camara), Snackbar.LENGTH_LONG)
                        snack.view.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.warning))
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
                        snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(itemView.context, R.color.warning_text))
                        snack.show()
                    }
                } else {
                    //val snack = Snackbar.make(itemView.findViewById(android.R.id.content), itemView.resources.getString(R.string.error_sensores_ra), Snackbar.LENGTH_LONG)
                    val snack = Snackbar.make(itemView, itemView.resources.getString(R.string.error_sensores_ra), Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.danger))
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).typeface = Utilitarios.getFontRoboto(itemView.context, Utilitarios.TypeFont.REGULAR)
                    snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(ContextCompat.getColor(itemView.context, R.color.danger_text))
                    snack.show()
                }
            }
        }
    }

    private val HORARIOALUMNO = 1
    private val HORARIOPROFESOR = 2
    private val HORARIOPROFESORASISTENCIA = 3
}