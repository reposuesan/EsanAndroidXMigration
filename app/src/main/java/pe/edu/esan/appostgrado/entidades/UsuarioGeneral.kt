package pe.edu.esan.appostgrado.entidades


/**
 * Created by lventura on 17/04/18.
 */
class UsuarioGeneral (
        var tipoDocumento: String,
        var numeroDocumento: String,
        var nombre: String,
        var apellido: String,
        var nombreCompleto: String,
        var correo: String,
        var usuario: String,
        var clave: String,
        var esAlumnoPregrado: Boolean,
        var codigoAlumnoPre: String,
        var esAlumnoPostgrado: Boolean,
        var codigoAlumnoPost: String,
        var esDocentePregrado: Boolean,
        var codigoDocentePre: String,
        var esDocentePostgrado: Boolean,
        var codigoDocentePost: String,
        var programasPregrado: List<ProgramasPregrado>) {
    var cambioPerfil = false
    var validaPerfil = false
    var validarFacultad = false
}