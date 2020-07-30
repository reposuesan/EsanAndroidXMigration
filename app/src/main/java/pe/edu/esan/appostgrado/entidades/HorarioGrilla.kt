package pe.edu.esan.appostgrado.entidades

/**
 * Created by lventura on 20/08/18.
 */
class HorarioGrilla (var valorInferior: String,
                     var valorSuperior: String,
                     var tipo: Int,
                     var actual: Boolean,
                     var minuto: Int) {
    constructor(valorInferior: String, valorSuperior: String, tipo: Int) : this(valorInferior, valorSuperior, tipo, false, 0)
}