package pe.edu.esan.appostgrado.architecture.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import pe.edu.esan.appostgrado.entidades.*
import java.util.concurrent.CopyOnWriteArrayList

@Entity(tableName = "control_room")
data class ControlRoomEntity (

    @PrimaryKey var id: Int,
    var currentUsuarioGeneral: UsuarioGeneral?,
    var statusLogout: Int = 1,
    var currentUsuario: ArrayList<UserEsan> = ArrayList<UserEsan>(),
    var entroEncuesta: Boolean = false,
    var currentListHorario: CopyOnWriteArrayList<Horario> = CopyOnWriteArrayList<Horario>(),
    var currentListHorarioSelect: ArrayList<Horario> = ArrayList<Horario>(),
    var copiarListHorario: ArrayList<Horario> = ArrayList<Horario>(),
    var currentHorario : Horario? = null,
    var accesoCamara: Boolean = false,
    var accesoGPS: Boolean = false,
    var prconfiguracion : PRConfiguracion? = null,
    var prPromocionConfig : TipoGrupo? = null,
    var prreserva: PRReserva? = null,
    var esReservaExitosa: Boolean = false,

    var creoMomificoGrupo: Boolean = false,
    var currentMiGrupo: ArrayList<Alumno> = ArrayList<Alumno>(),
    var cambioPantalla: Boolean = false,
    var pantallaSuspendida: Boolean = false,
    var recargaHorarioProfesor: Boolean = false,
    var tomoAsistenciaMasica:Boolean = false,
    var indexActualiza: Int = -1,

    var currentCursoPost : CursosPost? = null,
    var currentCursoPre : CursosPre? = null,
    var currentSeccion : Seccion? = null,

    var type: String = ""
)