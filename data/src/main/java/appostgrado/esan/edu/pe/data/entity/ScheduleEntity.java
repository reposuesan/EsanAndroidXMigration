package appostgrado.esan.edu.pe.data.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ScheduleEntity {

    @SerializedName("Ambiente")
    @Expose
    private String ambiente;
    @SerializedName("CodigoSeccion")
    @Expose
    private String codigoSeccion;
    @SerializedName("Curso")
    @Expose
    private String curso;
    @SerializedName("EsPregrado")
    @Expose
    private int esPregrado;
    @SerializedName("Fecha")
    @Expose
    private String fecha;
    @SerializedName("FechaHoraActual")
    @Expose
    private String fechaHoraActual;
    @SerializedName("Fin")
    @Expose
    private String fin;
    @SerializedName("IdAmbiente")
    @Expose
    private int idAmbiente;
    @SerializedName("IdCurso")
    @Expose
    private int idCurso;
    @SerializedName("IdHorario")
    @Expose
    private int idHorario;
    @SerializedName("IdSeccion")
    @Expose
    private int idSeccion;
    @SerializedName("IdSesion")
    @Expose
    private int idSesion;
    @SerializedName("IdactorReemplazo")
    @Expose
    private int idactorReemplazo;
    @SerializedName("Inicio")
    @Expose
    private String inicio;
    @SerializedName("Profesor")
    @Expose
    private String profesor;
    @SerializedName("ProfesorReemplazo")
    @Expose
    private String profesorReemplazo;
    @SerializedName("Promocion")
    @Expose
    private String promocion;
    @SerializedName("SeccionCodigo")
    @Expose
    private String seccionCodigo;
    @SerializedName("TomoAsistencia")
    @Expose
    private int tomoAsistencia;
    @SerializedName("coordinador")
    @Expose
    private String coordinador;

    public String getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public String getCodigoSeccion() {
        return codigoSeccion;
    }

    public void setCodigoSeccion(String codigoSeccion) {
        this.codigoSeccion = codigoSeccion;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public int getEsPregrado() {
        return esPregrado;
    }

    public void setEsPregrado(int esPregrado) {
        this.esPregrado = esPregrado;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getFechaHoraActual() {
        return fechaHoraActual;
    }

    public void setFechaHoraActual(String fechaHoraActual) {
        this.fechaHoraActual = fechaHoraActual;
    }

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public int getIdAmbiente() {
        return idAmbiente;
    }

    public void setIdAmbiente(int idAmbiente) {
        this.idAmbiente = idAmbiente;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public int getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(int idHorario) {
        this.idHorario = idHorario;
    }

    public int getIdSeccion() {
        return idSeccion;
    }

    public void setIdSeccion(int idSeccion) {
        this.idSeccion = idSeccion;
    }

    public int getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(int idSesion) {
        this.idSesion = idSesion;
    }

    public int getIdactorReemplazo() {
        return idactorReemplazo;
    }

    public void setIdactorReemplazo(int idactorReemplazo) {
        this.idactorReemplazo = idactorReemplazo;
    }

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    public String getProfesorReemplazo() {
        return profesorReemplazo;
    }

    public void setProfesorReemplazo(String profesorReemplazo) {
        this.profesorReemplazo = profesorReemplazo;
    }

    public String getPromocion() {
        return promocion;
    }

    public void setPromocion(String promocion) {
        this.promocion = promocion;
    }

    public String getSeccionCodigo() {
        return seccionCodigo;
    }

    public void setSeccionCodigo(String seccionCodigo) {
        this.seccionCodigo = seccionCodigo;
    }

    public int getTomoAsistencia() {
        return tomoAsistencia;
    }

    public void setTomoAsistencia(int tomoAsistencia) {
        this.tomoAsistencia = tomoAsistencia;
    }

    public String getCoordinador() {
        return coordinador;
    }

    public void setCoordinador(String coordinador) {
        this.coordinador = coordinador;
    }



}
