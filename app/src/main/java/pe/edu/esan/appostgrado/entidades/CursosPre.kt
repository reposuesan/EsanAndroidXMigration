package pe.edu.esan.appostgrado.entidades

import android.content.Context
import pe.edu.esan.appostgrado.R

/**
 * Created by lventura on 26/04/18.
 */
class CursosPre (
        val idSeccion: Int,
        val seccionCodigo: String,
        val idProcesoMatricula: Int,
        val nombreCurso: String,
        var promedio: String = "-",
        var listNotasPre: ArrayList<NotasPre> = ArrayList(),
        var listProfesores: ArrayList<Profesor> = ArrayList()) {

    constructor() : this (0,"",0,"")
    constructor(nombreCurso: String, promedio: String): this (0, "", 0, nombreCurso, promedio)
    constructor(nombreCurso: String, promedio: String, estado: String, proceso: String, esCabecera: Boolean, esIngles: Boolean, credito: String): this(0,"", 0, nombreCurso, promedio) {
        this.estado = estado
        this.proceso = proceso
        this.esCabecera = esCabecera
        this.esIngles = esIngles
        this.credito = credito
    }
    constructor(nombreCurso: String, promedio: String, creditos: Int, veces: Int, periodo: String, estado: String, tipoElectivo: String, esIngles: Boolean, listaCursoRequisito: ArrayList<CursoRequisito>): this(nombreCurso, promedio){
        this.creditos = creditos
        this.veces = veces
        this.periodo = periodo
        this.estado = estado
        this.esIngles = esIngles
        this.tipoElectivo = tipoElectivo
        this.listaCursoRequisito = listaCursoRequisito
    }

    var retirado = false
    var proceso = ""
    var esCabecera = false
    var esIngles = false
    var credito: String = ""
    var creditos: Int = 0
    var veces: Int = 0
    var periodo: String = ""
    var estado: String = ""
    var tipoElectivo: String = ""
    var listaCursoRequisito: ArrayList<CursoRequisito> = ArrayList()

    var asistencias = 0
    var tardanzas = 0
    var faltas = 0
    var totalsesiones = 0
    var errorasistencias = false

    fun fillCursoDefault(context: Context) {
        val tipoNotas = context.resources.getStringArray(R.array.tipo_notas_mostrar)
        val codigoNotas = context.resources.getStringArray(R.array.codigo_notas)

        for (i in 0 .. 2) {
            val notas = NotasPre(tipoNotas[i], codigoNotas[i], "-", "-")
            listNotasPre.add(notas)
        }
    }

    fun cambiaPromedio(promedio: String){
        this.promedio = promedio
    }

}