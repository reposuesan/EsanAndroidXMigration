package appostgrado.esan.edu.pe.data.rest.response.schedule;

import appostgrado.esan.edu.pe.data.entity.ScheduleEntity;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScheduleResponse {

    @SerializedName("ListarHorarioAlumnoProfesorxFecha2Result")
    @Expose
    private List<ScheduleEntity> listarHorarioAlumnoProfesorxFecha2Result = null;

    public List<ScheduleEntity> getListarHorarioAlumnoProfesorxFecha2Result() {
        return listarHorarioAlumnoProfesorxFecha2Result;
    }

    public void setListarHorarioAlumnoProfesorxFecha2Result(List<ScheduleEntity> listarHorarioAlumnoProfesorxFecha2Result) {
        this.listarHorarioAlumnoProfesorxFecha2Result = listarHorarioAlumnoProfesorxFecha2Result;
    }

}
