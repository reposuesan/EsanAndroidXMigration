package pe.edu.esan.appostgrado.entidades

/**
 * Created by lventura on 17/05/18.
 */
class PRReserva (val codigo: String,
                 val nombre: String,
                 val fecha: String,
                 val time: Long,
                 val disponible: Boolean,
                 val listaHorario: List<PRHorarioReserva>,
                 val listaEmergencia: List<PRHorarioReserva>,
                 val listaNoSelect: List<PRHorarioReserva>?,
                 val listaAlternativo: List<List<PRHorarioReserva>> = ArrayList()) {
}