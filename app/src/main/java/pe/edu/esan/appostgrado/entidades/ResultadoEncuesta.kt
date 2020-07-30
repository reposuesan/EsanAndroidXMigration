package pe.edu.esan.appostgrado.entidades

import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 18/06/18.
 */
class ResultadoEncuesta (
        val tipo: Utilitarios.TipoFila,
        val titulo: String,
        val totalEncuesta: Int,
        val siEvaluaron: Int,
        val noEvaluaron: Int,
        val AP: Double,
        val DP: Double,
        val AVG: Double,
        var preguntas : List<GrupoPregunta> = ArrayList()
)