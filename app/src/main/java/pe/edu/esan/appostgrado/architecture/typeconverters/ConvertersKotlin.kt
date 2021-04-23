package pe.edu.esan.appostgrado.architecture.typeconverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import pe.edu.esan.appostgrado.architecture.adapter.AbstractElementAdapter
import pe.edu.esan.appostgrado.entidades.*
import java.util.concurrent.CopyOnWriteArrayList

class ConvertersKotlin {

    //val gson = Gson()

    @TypeConverter
    fun fromStringToCurrentUsuario(stringInput: String): ArrayList<UserEsan> {
        val gsonHelper = GsonBuilder()
        gsonHelper.registerTypeAdapter(UserEsan::class.java,
            AbstractElementAdapter()
        )

        val type = object : TypeToken<ArrayList<UserEsan>>(){}.type
        return gsonHelper.create().fromJson(stringInput, type)
    }

    @TypeConverter
    fun fromCurrentUsuarioToString(currentUsuarioInput: ArrayList<UserEsan>): String {
        val type = object : TypeToken<ArrayList<UserEsan>>(){}.type
        val gsonHelper = GsonBuilder()
        gsonHelper.registerTypeAdapter(UserEsan::class.java,
            AbstractElementAdapter()
        )
        return gsonHelper.create().toJson(currentUsuarioInput, type)
    }

    @TypeConverter
    fun fromStringToCurrentUsuarioGeneral(stringInput: String): UsuarioGeneral? {
        val type = object : TypeToken<UsuarioGeneral>(){}.type
        return Gson().fromJson(stringInput, type)
    }

    @TypeConverter
    fun fromCurrentUsuarioGeneralToString(currentUsuarioGeneralInput: UsuarioGeneral?): String {
        val type = object : TypeToken<UsuarioGeneral>(){}.type
        return Gson().toJson(currentUsuarioGeneralInput, type)
    }

    @TypeConverter
    fun fromStringToArrayListHorario(stringInput: String): ArrayList<Horario> {
        val type = object : TypeToken<ArrayList<Horario>>(){}.type
        return Gson().fromJson(stringInput, type)
    }

    @TypeConverter
    fun fromArrayListHorarioToString(listHorario: ArrayList<Horario>): String {
        val type = object : TypeToken<ArrayList<Horario>>(){}.type
        return Gson().toJson(listHorario,type)
    }

    @TypeConverter
    fun fromStringToUnmodifiedArrayListHorario(stringInput: String): CopyOnWriteArrayList<Horario> {
        val type = object : TypeToken<CopyOnWriteArrayList<Horario>>(){}.type
        return Gson().fromJson(stringInput, type)
    }

    @TypeConverter
    fun fromUnmodifiedArrayListHorarioToString(listHorario: CopyOnWriteArrayList<Horario>): String {
        val type = object : TypeToken<CopyOnWriteArrayList<Horario>>(){}.type
        return Gson().toJson(listHorario,type)
    }

    @TypeConverter
    fun fromStringToCurrentHorario(stringInput: String): Horario? {
        val type = object : TypeToken<Horario>() {}.type
        return Gson().fromJson<Horario>(stringInput, type)

    }

    @TypeConverter
    fun fromCurrentHorarioToString(horario: Horario?): String {
        val type = object : TypeToken<Horario>() {}.type
        return Gson().toJson(horario, type)
    }

    @TypeConverter
    fun fromStringToPRConfiguracion(stringInput: String): PRConfiguracion? {
        val type = object : TypeToken<PRConfiguracion>() {}.type
        return Gson().fromJson<PRConfiguracion>(stringInput, type)

    }

    @TypeConverter
    fun fromPRConfiguracionToString(prConfiguracion: PRConfiguracion?): String {
        val type = object : TypeToken<PRConfiguracion>() {}.type
        return Gson().toJson(prConfiguracion, type)
    }

    @TypeConverter
    fun fromStringToTipoGrupo(stringInput: String): TipoGrupo? {

        val type = object : TypeToken<TipoGrupo>() {}.type

        return Gson().fromJson<TipoGrupo>(stringInput, type)

    }

    @TypeConverter
    fun fromTipoGrupoToString(tipoGrupo: TipoGrupo?): String {
        val type = object : TypeToken<TipoGrupo>() {}.type
        return Gson().toJson(tipoGrupo, type)
    }

    @TypeConverter
    fun fromStringToPRReserva(stringInput: String): PRReserva? {

        val type = object : TypeToken<PRReserva>() {}.type

        return Gson().fromJson<PRReserva>(stringInput, type)

    }

    @TypeConverter
    fun fromPRReservaToString(prReserva: PRReserva?): String {
        val type = object : TypeToken<PRReserva>() {}.type
        return Gson().toJson(prReserva, type)
    }


    @TypeConverter
    fun fromStringToCurrentMiGrupo(stringInput: String): java.util.ArrayList<Alumno>? {

        val type = object : TypeToken<java.util.ArrayList<Alumno>>() {}.type

        return Gson().fromJson<java.util.ArrayList<Alumno>>(stringInput, type)

    }

    @TypeConverter
    fun fromCurrentMiGrupoToString(currentMiGrupo: java.util.ArrayList<Alumno>): String {
        val type = object : TypeToken<java.util.ArrayList<Alumno>>() {}.type
        return Gson().toJson(currentMiGrupo, type)
    }

    @TypeConverter
    fun fromStringToCurrentCursoPost(stringInput: String): CursosPost? {

        val type = object : TypeToken<CursosPost>() {}.type

        return Gson().fromJson<CursosPost>(stringInput, type)

    }

    @TypeConverter
    fun fromCurrentCursoPostToString(currentCursoPost: CursosPost?): String {
        val type = object : TypeToken<CursosPost>() {}.type
        return Gson().toJson(currentCursoPost, type)
    }

    @TypeConverter
    fun fromStringToCurrentCursoPre(stringInput: String): CursosPre? {

        val type = object : TypeToken<CursosPre>() {}.type

        return Gson().fromJson<CursosPre>(stringInput, type)

    }

    @TypeConverter
    fun fromCurrentCursoPreToString(currentCursoPre: CursosPre?): String {
        val type = object : TypeToken<CursosPre>() {}.type
        return Gson().toJson(currentCursoPre, type)
    }

    @TypeConverter
    fun fromStringToCurrentSeccion(stringInput: String): Seccion? {

        val type = object : TypeToken<Seccion>() {}.type

        return Gson().fromJson<Seccion>(stringInput, type)

    }

    @TypeConverter
    fun fromCurrentSeccionToString(currentSeccion: Seccion?): String {
        val type = object : TypeToken<Seccion>() {}.type
        return Gson().toJson(currentSeccion, type)
    }
}