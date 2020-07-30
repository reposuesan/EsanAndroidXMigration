package appostgrado.esan.edu.pe.data.rest;

import appostgrado.esan.edu.pe.data.rest.request.ScheduleRequest;
import appostgrado.esan.edu.pe.data.rest.response.schedule.ScheduleResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Api {

    @Headers("Content-Type:application/json")
    @POST("/GestionAcademica/Horario/Horario.svc/HorarioFecha2")
    // @POST("/RESTLocal/GestionAcademica/Horario/Horario.svc/HorarioFecha2")
    // Call<ScheduleResponse> getMySchedule(@Body BaseRequest baseRequest);
    Call<ScheduleResponse> getMySchedule(@Body ScheduleRequest scheduleRequest);

/*
    @Headers("Content-Type:application/json")
    @POST("/api/users/register")
    Call<RegisterResponse> register(@Body RegisterRequest loginRequest);
*/

}
