package pe.edu.esan.appostgrado.entidades

import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 25/05/18.
 */
class PreguntaEncuesta (val tipo: Utilitarios.TipoPreguntaEncuesta,
                        val cabecera: String,
                        val grupoOreden: Int,
                        val idEncuesta: Int,
                        val idPregunta: Int,
                        val pregunta: String,
                        val preguntaOrden: Int,
                        var puntaje: String = "0",
                        var respuesta : String = "")