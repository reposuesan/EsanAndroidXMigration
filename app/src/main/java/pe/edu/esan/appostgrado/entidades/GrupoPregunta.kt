package pe.edu.esan.appostgrado.entidades

import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 18/06/18.
 */
class GrupoPregunta(
        val tipo: Utilitarios.TipoFila,
        val grupo: String,
        val pregunta: String,
        val AP: Double,
        val DP: Double,
        val AVG: Double){
    constructor(tipo: Utilitarios.TipoFila, grupo: String) : this(tipo, grupo,"", 0.0, 0.0, 0.0)
    constructor(tipo: Utilitarios.TipoFila, pregunta: String, AP: Double, DP: Double, AVG: Double) : this(tipo, "", pregunta, AP, DP, AVG)
}
