package pe.edu.esan.appostgrado.entidades

/**
 * Created by lchang on 24/04/18.
 */
class Horario (
        var tipoHorario: Int,
        var idHorario: Int,
        var horaInicio: String,
        var horaFin: String,
        var profesor: String,
        var dia: String,
        var idAmbiente: Int,
        var curso: String,
        var idSeccion: Int,
        var seccionCodigo: String,
        var idSesion: Int,
        var section: String,
        var aula: String,
        var takeAssist: Int,
        var esPregrado: Int,
        var horaConsulta: String,
        var numSesionReal: String,
        var idProfesorReem: Int,
        var profesorReem: String) {


        //Secondary Constructor
        constructor(tipoHorario: Int, idHorario: Int, horaInicio: String, horaFin: String,
                    profesor: String, dia: String, idAmbiente: Int, curso: String,
                    idSeccion: Int, seccionCodigo: String, idSesion: Int, section: String,
                    aula: String, takeAssist: Int, esPregrado: Int, horaConsulta: String,
                    numSesionReal: String, idProfesorReem: Int, profesorReem: String, horario: Horario)
                : this (tipoHorario, idHorario, horaInicio, horaFin,
                profesor,dia,idAmbiente,curso, idSeccion, seccionCodigo,
                idSesion, section, aula, takeAssist, esPregrado, horaConsulta,
                numSesionReal, idProfesorReem, profesorReem) {

                this.tipoHorario = horario.tipoHorario
                this.idHorario = horario.idHorario
                this.horaInicio = horario.horaInicio
                this.horaFin = horario.horaFin
                this.profesor = horario.profesor
                this.dia = horario.dia
                this.idAmbiente = horario.idAmbiente
                this.curso = horario.curso
                this.idSeccion = horario.idSeccion
                this.seccionCodigo = horario.seccionCodigo
                this.idSesion = horario.idSesion
                this.section = horario.section
                this.aula = horario.aula
                this.takeAssist = horario.takeAssist
                this.esPregrado = horario.esPregrado
                this.horaConsulta = horario.horaConsulta
                this.numSesionReal = horario.numSesionReal
                this.idProfesorReem = horario.idProfesorReem
                this.profesorReem = horario.profesorReem
        }
}