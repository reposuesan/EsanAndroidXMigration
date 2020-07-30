package pe.edu.esan.appostgrado.entidades

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GrupoAlumnosPrereserva(var horasDisp: String,
                             var mensajeVerificacion: String,
                             var indicador: String,
                             var horaIniRes: String,
                             var horaFinRes: String,
                             var idConfig:String,
                             var horaActual: String): Parcelable {
}