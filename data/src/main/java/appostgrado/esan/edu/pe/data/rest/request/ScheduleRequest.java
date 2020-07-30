package appostgrado.esan.edu.pe.data.rest.request;

public class ScheduleRequest {


    private String Codigo;
    private String Tipo;
    private String Fecha;
    private String Facultad;

    public ScheduleRequest(String codigo, String tipo, String fecha, String facultad) {
        this.Codigo = codigo;
        this.Tipo = tipo;
        this.Fecha = fecha;
        this.Facultad = facultad;
    }

}
