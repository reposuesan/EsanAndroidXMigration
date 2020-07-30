package pe.edu.esan.appostgrado.entidades

import java.util.*

/**
 * Created by lventura on 2/08/18.
 */
class AlmuerzoComedor(val tipopcion: Int,
                      val cabecera: String,
                      val descripcion: String? = null,
                      val dia: Int? = null,
                      val entrada: String? = null,
                      val fecha: Date? = null,
                      val guarnicion: String? = null,
                      val postre: String? = null,
                      val precio: String? = null,
                      val refresco: String? = null,
                      val segundo: String? = null,
                      val sopa: String? = null,
                      val idcomedor: String? = null,
                      val nombrecomedor: String? = null)