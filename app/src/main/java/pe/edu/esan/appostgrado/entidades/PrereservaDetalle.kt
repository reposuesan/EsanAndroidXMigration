package pe.edu.esan.appostgrado.entidades

class PrereservaDetalle(val nomLabCubiculo: String,
                        val creadorPrereserva: String,
                        val horarioPrereserva: String,
                        val fechaPrereserva: String,
                        val referenciaUbicacion: String,
                        var prereservaEstado: String,
                        val grupoId: Int,
                        var imagenMapa: String) {
}