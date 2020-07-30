package appostgrado.esan.edu.pe.domain.interactors;

import appostgrado.esan.edu.pe.domain.callbacks.ScheduleCallback;

public interface ScheduleInteractor {

    // void getMySchedule(ScheduleCallback interactor, String request);
    void getMySchedule(ScheduleCallback interactor, String codigo, String tipo, String fecha, String facultad);

}
