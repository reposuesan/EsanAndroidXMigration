package pe.edu.esan.appostgrado.architecture.typeconverters;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import pe.edu.esan.appostgrado.entidades.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ConvertersJava {

    @TypeConverter
    public static ArrayList<Object> fromStringToCurrentUsuario(String stringInput){

        Type type = new TypeToken <ArrayList<Object>>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromCurrentUsuarioToString(ArrayList<Object> currentUsuarioInput){
        Gson gson = new Gson();

        return gson.toJson(currentUsuarioInput);
    }


    @TypeConverter
    public static UsuarioGeneral fromStringToCurrentUsuarioGeneral(String stringInput){

        Type type = new TypeToken <UsuarioGeneral>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromCurrentUsuarioGeneralToString(UsuarioGeneral currentUsuarioGeneralInput){
        Gson gson = new Gson();

        return gson.toJson(currentUsuarioGeneralInput);
    }

    @TypeConverter
    public static ArrayList<Horario> fromStringToArrayListHorario(String stringInput){

        Type type = new TypeToken <ArrayList<Horario>>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromArrayListHorarioToString(ArrayList<Horario> currentListHorario){
        Gson gson = new Gson();

        return gson.toJson(currentListHorario);
    }

    /*@TypeConverter
    public static ArrayList<Horario> fromStringToCurrentListHorarioSelect(String stringInput){

        Type type = new TypeToken <ArrayList<Horario>>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromCurrentListHorarioSelectToString(ArrayList<Horario> currentListHorarioSelect){
        Gson gson = new Gson();

        return gson.toJson(currentListHorarioSelect);
    }

    @TypeConverter
    public static ArrayList<Horario> fromStringToCopiarListHorario(String stringInput){

        Type type = new TypeToken <ArrayList<Horario>>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromCopiarListHorarioToString(ArrayList<Horario> copiarListHorario){
        Gson gson = new Gson();

        return gson.toJson(copiarListHorario);
    }*/

    @TypeConverter
    public static Horario fromStringToCurrentHorario(String stringInput){

        Type type = new TypeToken <Horario>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromCurrentHorarioToString(Horario horario){
        Gson gson = new Gson();

        return gson.toJson(horario);
    }

    @TypeConverter
    public static PRConfiguracion fromStringToPRConfiguracion(String stringInput){

        Type type = new TypeToken <PRConfiguracion>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromPRConfiguracionToString(PRConfiguracion prConfiguracion){
        Gson gson = new Gson();

        return gson.toJson(prConfiguracion);
    }

    @TypeConverter
    public static TipoGrupo fromStringToTipoGrupo(String stringInput){

        Type type = new TypeToken <TipoGrupo>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromTipoGrupoToString(TipoGrupo tipoGrupo){
        Gson gson = new Gson();

        return gson.toJson(tipoGrupo);
    }

    @TypeConverter
    public static PRReserva fromStringToPRReserva(String stringInput){

        Type type = new TypeToken <PRReserva>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromPRReservaToString(PRReserva prReserva){
        Gson gson = new Gson();

        return gson.toJson(prReserva);
    }


    @TypeConverter
    public static ArrayList<Alumno> fromStringToCurrentMiGrupo(String stringInput){

        Type type = new TypeToken <ArrayList<Alumno>>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromCurrentMiGrupoToString(ArrayList<Alumno> currentMiGrupo){
        Gson gson = new Gson();

        return gson.toJson(currentMiGrupo);
    }

    @TypeConverter
    public static CursosPost fromStringToCurrentCursoPost(String stringInput){

        Type type = new TypeToken <CursosPost>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromCurrentCursoPostToString(CursosPost currentCursoPost){
        Gson gson = new Gson();

        return gson.toJson(currentCursoPost);
    }

    @TypeConverter
    public static CursosPre fromStringToCurrentCursoPre(String stringInput){

        Type type = new TypeToken <CursosPre>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromCurrentCursoPreToString(CursosPre currentCursoPre){
        Gson gson = new Gson();

        return gson.toJson(currentCursoPre);
    }

    @TypeConverter
    public static Seccion fromStringToCurrentSeccion(String stringInput){

        Type type = new TypeToken <Seccion>(){}.getType();

        return new Gson().fromJson(stringInput, type);

    }

    @TypeConverter
    public static String fromCurrentSeccionToString(Seccion currentSeccion){
        Gson gson = new Gson();

        return gson.toJson(currentSeccion);
    }
}
