package pe.edu.esan.appostgrado.entidades

import org.json.JSONObject

/**
 * Created by lventura on 15/05/18.
 */
class PRConfiguracion (val idConfiguracion: Int, val grupo: String, val diasAnticipa: Int, val horasReserva: Int, var horasUtil: Int) {
    var cantiMaxGrupo: Int? = null
    var idGrupoEstudio: Int? = null
}