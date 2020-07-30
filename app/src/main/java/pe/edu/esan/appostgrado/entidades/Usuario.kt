package pe.edu.esan.appostgrado.entidades

/**
 * Created by lventura on 17/04/18.
 */
open class UserEsan(
        val idActor : Int,
        val codigo : String,
        val nombreCompleto : String,
        val nombres : String,
        val apellidos : String,
        val tDocumento : String,
        val nDocumento: String,
        val usuario: String = "",
        val clave: String = "",
        val carrera: String = "") {

    constructor(idActor: Int, nombreCompleto: String) : this(idActor, "", nombreCompleto, "", "", "", "")
    constructor(idActor: Int, codigo: String, nombreCompleto: String) : this(idActor, codigo, nombreCompleto, "", "", "", "")
    constructor(codigo : String, nombreCompleto : String, nombres : String, apellidos : String, tDocumento : String, nDocumento: String, usuario: String, clave: String) : this(0, codigo, nombreCompleto, nombres, apellidos, tDocumento, nDocumento, usuario, clave)
    constructor(idActor: Int, codigo : String, nombreCompleto : String, nombres : String, apellidos : String, nDocumento: String, usuario: String, clave: String) : this(idActor, codigo, nombreCompleto, nombres, apellidos, "", nDocumento, usuario, clave)
    constructor(codigo : String, nombreCompleto : String, nombres : String, apellidos : String, tDocumento : String, nDocumento: String, usuario: String, clave: String, carrera: String) : this(0, codigo, nombreCompleto, nombres, apellidos, tDocumento, nDocumento, usuario, clave, carrera)
}

class Alumno : UserEsan {
    var tipoAlumno : String = ""
    var idPeriodo : Int = 0
    var correo : String = ""
    var asistencias : Int = 0
    var tardanzas : Int = 0
    var faltas : Int = 0
    var totalsesiones : Int = 0
    var seleccionado = false
    var estadoAsistencia = ""
    var actualizoEstadoAsistencia = false
    var porcFaltas : Float = 0f
    var porcInha : Int = 0
    var estado : String = ""
    var esCreador: Boolean = false
    var activarEliminar = false

    constructor(codigo: String, nombreCompleto: String, nombres: String, apellidos: String, tDocumento: String, nDocumento: String, tipoAlumno: String, idPeriodo: Int, usuario: String, clave: String) :
            super(codigo, nombreCompleto, nombres, apellidos, tDocumento, nDocumento, usuario, clave) {
        this.tipoAlumno = tipoAlumno
        this.idPeriodo = idPeriodo
    }

    constructor(codigo: String, nombreCompleto: String, nombres: String, apellidos: String, tDocumento: String, nDocumento: String, tipoAlumno: String, idPeriodo: Int, usuario: String, clave: String, carrera: String) :
            super(codigo, nombreCompleto, nombres, apellidos, tDocumento, nDocumento, usuario, clave, carrera) {
        this.tipoAlumno = tipoAlumno
        this.idPeriodo = idPeriodo
    }

    constructor(codigo: String, idActor: Int, nombreCompleto: String, correo: String) :
            super(idActor, codigo, nombreCompleto) {
        this.correo = correo
    }

    constructor(codigo: String, idActor: Int, nombreCompleto: String, esCreador: Boolean) :
            super(idActor, codigo, nombreCompleto) {
        this.esCreador = esCreador
    }

    constructor(codigo: String, idActor: Int, nombreCompleto: String, correo: String, estado: String, procFaltas: Float, porcInha: Int) :
            super(idActor, codigo, nombreCompleto) {
        this.correo = correo
        this.estado = estado
        this.porcFaltas = procFaltas
        this.porcInha = porcInha
    }

    constructor(codigo: String, idActor: Int, nombreCompleto: String, asistencias: Int, tardanzas: Int, faltas: Int, totalsesiones: Int) :
            super(idActor, codigo, nombreCompleto) {
        this.asistencias = asistencias
        this.tardanzas = tardanzas
        this.faltas = faltas
        this.totalsesiones = totalsesiones
    }

    override fun equals(other: Any?): Boolean {
        val otroAlumno = other as Alumno?
        if (otroAlumno != null) {
            return (tipoAlumno == otroAlumno.tipoAlumno &&
                    codigo == otroAlumno.codigo &&
                    nDocumento == otroAlumno.nDocumento &&
                    tDocumento == otroAlumno.tDocumento)
        } else
            return false

    }
}

class Profesor : UserEsan {
    var email : String = ""

    var encuesta: String = ""
    var isResponsable: Boolean = false
    var esDocentePre = false
    var esDocentePost = false

    var idEncuesta: Int? = null
    var idProgramacion: Int? = null
    var seleccionado = false
    var estadoAlumno: String = ""

    /*constructor(codigo: String, nombreCompleto: String, nombres: String, apellidoPaterno: String, apellidoMaterno: String, numeroDocumento: String, idActor: Int, email: String) :
            super(idActor, codigo, nombreCompleto, nombres, apellidoPaterno + " " + apellidoMaterno, numeroDocumento) {
        this.email = email
    }*/

    constructor(idActor: Int, codigo: String, nombreCompleto: String, nombres: String, apellidos: String, tipoDocumento: String, numeroDocumento: String, email: String, usuario: String, clave: String, esDocentePre: Boolean, esDocentePost: Boolean) :
            super(idActor, codigo, nombreCompleto, nombres, apellidos, tipoDocumento, numeroDocumento, usuario, clave) {
        this.email = email
        this.esDocentePre = esDocentePre
        this.esDocentePost = esDocentePost
    }

    constructor(idActor: Int, nombre: String, encuesta: String, isResponsable: Boolean, idEncuesta: Int?, idProgramacion: Int?) :
            super(idActor, nombre) {
        this.encuesta = encuesta
        this.isResponsable = isResponsable
        this.idEncuesta = idEncuesta
        this.idProgramacion = idProgramacion
    }

    constructor(idActor: Int, nombre: String, encuesta: String, isResponsable: Boolean, idEncuesta: Int?, idProgramacion: Int?, estadoAlumno: String) :
            super(idActor, nombre) {
        this.encuesta = encuesta
        this.isResponsable = isResponsable
        this.idEncuesta = idEncuesta
        this.idProgramacion = idProgramacion
        this.estadoAlumno = estadoAlumno
    }


    override fun equals(other: Any?): Boolean {
        val otroProfesor = other as Profesor?
        if (otroProfesor != null) {
            return (codigo == otroProfesor.codigo &&
                    nDocumento == otroProfesor.nDocumento &&
                    tDocumento == otroProfesor.tDocumento)
        } else
            return false

    }
}