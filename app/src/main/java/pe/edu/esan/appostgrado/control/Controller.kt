package pe.edu.esan.appostgrado.control

import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import pe.edu.esan.appostgrado.architecture.room.AppRoomDatabase
import pe.edu.esan.appostgrado.entidades.*
import java.util.*
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
    var currentListHorario = CustomArrayList<Horario>()
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

//TODO: This class was defined to solve ConvertersKotlin.kt problem line 55 reported in Crashlytics
class CustomArrayList<T>: ArrayList<T>() {

    var values = ArrayList<T>()
    var viewValues: List<T>  = Collections.unmodifiableList(values)

    override fun add(element: T): Boolean {
        values.add(element)
        return super.add(element)
    }

    override fun get(index: Int): T {
        if (index >= viewValues.size) throw IndexOutOfBoundsException(outOfBoundsMsg(index))

        return viewValues[index]

    }

    override fun clear() {
        values.clear()
        viewValues = Collections.unmodifiableList(values)
        super.clear()
    }

    private fun outOfBoundsMsg(index: Int): String? {
        return "Index: $index, Size: ${viewValues.size}"
    }
}