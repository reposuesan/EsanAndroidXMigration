package appostgrado.esan.edu.pe.data.rest;

import appostgrado.esan.edu.pe.data.rest.request.ScheduleRequest;
import appostgrado.esan.edu.pe.data.rest.response.schedule.ScheduleResponse;
import appostgrado.esan.edu.pe.domain.callbacks.ScheduleCallback;
import appostgrado.esan.edu.pe.domain.interactors.ScheduleInteractor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleRestInteractor implements ScheduleInteractor {

    @Override
    public void getMySchedule(final ScheduleCallback callback, String codigo, String tipo, String fecha, String facultad) {

        RestClient.getMyApiClient().getMySchedule(new ScheduleRequest(codigo, tipo, fecha, facultad)).enqueue(new Callback<ScheduleResponse>() {
            @Override
            public void onResponse(Call<ScheduleResponse> call, Response<ScheduleResponse> response) {
                if( response.code() == 200){
                    try{
                        ScheduleResponse caseDetailResponse = response.body();
                        callback.onServicesSuccess(caseDetailResponse.getListarHorarioAlumnoProfesorxFecha2Result());
                    }catch ( Exception e){
                        callback.onServicesError("Acción Cancelada.");
                    }
                } else if (response.code() == 401 ) {
                    callback.onAccessErrorNotAuthorized();
                } else {
                    callback.onServicesError("Respuesta desconocida del Servidor.");
                }
            }

            @Override
            public void onFailure(Call<ScheduleResponse> call, Throwable t) {
                callback.onErrorConection();
            }
        });
    }

}
