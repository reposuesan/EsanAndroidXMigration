package pe.edu.esan.appostgrado.entidades

/**
 * Created by lventura on 7/05/18.
 */
class CursosPost (val escabecera : Boolean,
                  val modulo: Int,
                  val idCurso: Int,
                  val cursoNombre: String,
                  val promedioCondicion: String,
                  val promedioFinal: String,
                  val idSeccion: Int,
                  val seccionCodigo: String,
                  val cursoActual: Boolean,
                  var encuesta: Boolean = false,
                  var expandible: Boolean = false,
                  var cargando: Boolean = false,
                  var detalleNotas: ArrayList<NotasPost> = ArrayList(),
                  var listaSeccionEncuesta: ArrayList<SeccionEncuestaPos> = ArrayList()) {

    constructor(escabecera: Boolean, modulo: Int) : this(escabecera, modulo, 0, "", "", "", 0, "", false)

    constructor(modulo: Int,
                idCurso: Int,
                cursoNombre: String,
                promedioCondicion: String,
                promedioFinal: String,
                idSeccion: Int,
                seccionCodigo: String,
                cursoActual: Boolean) : this(false, modulo, idCurso, cursoNombre, promedioCondicion, promedioFinal, idSeccion, seccionCodigo, cursoActual)
}