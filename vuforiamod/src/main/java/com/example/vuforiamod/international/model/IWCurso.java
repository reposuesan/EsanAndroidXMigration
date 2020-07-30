package com.example.vuforiamod.international.model;

/**
 * Created by lchang on 12/06/17.
 */

public class IWCurso {
    private int tipo;
    private String categoria;
    private String curso;
    private String descripcioncurso;
    private String profesor;
    private String descripcionprofesor;
    private String horario;

    public IWCurso(int tipo, String curso, String descripcioncurso, String profesor, String descripcionprofesor, String horario) {
        this.tipo = tipo;
        this.curso = curso;
        this.descripcioncurso = descripcioncurso;
        this.profesor = profesor;
        this.descripcionprofesor = descripcionprofesor;
        this.horario = horario;
    }

    public IWCurso(int tipo, String categoria) {
        this.tipo = tipo;
        this.categoria = categoria;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getDescripcioncurso() {
        return descripcioncurso;
    }

    public void setDescripcioncurso(String descripcioncurso) {
        this.descripcioncurso = descripcioncurso;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    public String getDescripcionprofesor() {
        return descripcionprofesor;
    }

    public void setDescripcionprofesor(String descripcionprofesor) {
        this.descripcionprofesor = descripcionprofesor;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
}
