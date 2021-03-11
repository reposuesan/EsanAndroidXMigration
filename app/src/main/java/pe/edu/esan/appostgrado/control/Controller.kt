package pe.edu.esan.appostgrado.control

import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import pe.edu.esan.appostgrado.architecture.room.AppRoomDatabase
import pe.edu.esan.appostgrado.entidades.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.ArrayList


/**
 * Created by lventura on 18/04/18.
 */
class ControlUsuario private constructor(){

    init {
        Log.w(ControlUsuario::class.simpleName,"This ($this) is a singleton")
    }

    private object Holder {
        val INSTANCE = ControlUsuario()
    }

    companion object {
        val instance: ControlUsuario by lazy {
            Holder.INSTANCE
        }
    }

    var currentUsuarioGeneral : UsuarioGeneral? = null
    var statusLogout = 1
    var currentUsuario = ArrayList<UserEsan>()
    var entroEncuesta = false
    var currentListHorario = CopyOnWriteArrayList<Horario>()
    var currentListHorarioSelect = ArrayList<Horario>()
    var copiarListHorario = ArrayList<Horario>()
    var currentHorario : Horario? = null
    var accesoCamara = false
    var accesoGPS = false
    var prconfiguracion : PRConfiguracion? = null
    var prPromocionConfig : TipoGrupo? = null
    var prreserva: PRReserva? = null
    var esReservaExitosa = false

    var creoMomificoGrupo = false
    var currentMiGrupo = ArrayList<Alumno>()
    var cambioPantalla = false
    var pantallaSuspendida = false
    var recargaHorarioProfesor = false
    var tomoAsistenciaMasica = false
    var indexActualiza = -1

    var currentCursoPost : CursosPost? = null
    var currentCursoPre : CursosPre? = null
    var currentSeccion : Seccion? = null
}
