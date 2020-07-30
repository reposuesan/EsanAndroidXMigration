package pe.edu.esan.appostgrado.entidades

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ProgramaDescripcionItem(var programaNombre: String,var programaId: String,var programaVersion: String,var itemSeleccionado: Boolean,var laboratorioId: Int): Parcelable {

    constructor(programaNombre: String, programaId: String, programaVersion: String, itemSeleccionado: Boolean, laboratorioId: Int, progDescripcionItem: ProgramaDescripcionItem)
            : this(programaNombre, programaId, programaVersion, itemSeleccionado, laboratorioId) {
        this.programaNombre = progDescripcionItem.programaNombre
        this.programaId = progDescripcionItem.programaId
        this.programaVersion = progDescripcionItem.programaVersion
        this.itemSeleccionado = progDescripcionItem.itemSeleccionado
        this.laboratorioId = progDescripcionItem.laboratorioId
    }
}