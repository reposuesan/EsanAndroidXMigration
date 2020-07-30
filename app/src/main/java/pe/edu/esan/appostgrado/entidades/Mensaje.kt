package pe.edu.esan.appostgrado.entidades

/**
 * Created by lventura on 1/08/18.
 */
class Mensaje (
        val key: String,
        val titulo: String,
        val mensaje: String,
        val fecha: String,
        var noLeido: Boolean,
        val url: String
)