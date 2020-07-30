package pe.edu.esan.appostgrado.entidades

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AlumnoPrereservaLab(var horasDispLab: String,
                          var mensajeLab: String,
                          var indicadorLab: String,
                          var horaIniResLab: String,
                          var horaFinResLab: String,
                          var idConfiguracionLab: String,
                          var promocionLab: String,
                          var tipoMatriculaLab: String,
                          var horasUsadasLab: String,
                          var maxCantidadProgPermitidos: String,
                          var horaActualLab: String) : Parcelable {
}