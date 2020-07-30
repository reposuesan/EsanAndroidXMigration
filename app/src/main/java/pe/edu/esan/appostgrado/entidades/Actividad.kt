package pe.edu.esan.appostgrado.entidades

/**
 * Created by lventura on 28/05/18.
 */
class Actividad (
        val actividad: String,
        val sesiones: Double,
        val visibledetalle: Boolean,
        val detalleActividad: List<ActividadDetalle>
)