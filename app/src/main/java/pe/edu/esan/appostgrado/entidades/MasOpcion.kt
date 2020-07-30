package pe.edu.esan.appostgrado.entidades

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * Created by lventura on 30/04/18.
 */
class MasOpcion(
        val id: Int,
        val intent: Intent?,
        val titulo: String,
        val detalle: String,
        val icono: Drawable?,
        var existeNotificacion: Boolean? = null,
        var cantidadNotificacion: Int? = null)