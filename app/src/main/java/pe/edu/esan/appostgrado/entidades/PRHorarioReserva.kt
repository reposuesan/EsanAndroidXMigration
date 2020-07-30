package pe.edu.esan.appostgrado.entidades

/**
 * Created by lventura on 15/05/18.
 */
class PRHorarioReserva (
        val id: Int,
        val horainicio: String,
        val horafin: String,
        val inicio: Int,
        val fin: Int,
        var estado: String = "",
        var seleccionado: Boolean = false,
        var emergencia: Boolean = false,
        var posicion: Int = 0)