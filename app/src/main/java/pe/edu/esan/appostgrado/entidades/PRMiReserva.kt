package pe.edu.esan.appostgrado.entidades

/**
 * Created by lventura on 12/09/18.
 */
class PRMiReserva(
        var tipo: Int,
        var fecha: String
) {
    constructor(tipo: Int, fecha: String, cubiculo: String, horaInicio: String, horaFin: String, descripcion: String, estado: String): this(tipo, fecha) {
        this.cubiculo = cubiculo
        this.horaInicio = horaInicio
        this.horaFin = horaFin
        this.descripcion = descripcion
        this.estado = estado
    }

    var cubiculo: String = ""
    var horaInicio: String = ""
    var horaFin : String = ""
    var descripcion: String = ""
    var estado: String = ""
}