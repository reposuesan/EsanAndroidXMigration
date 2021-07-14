package com.example.vuforiamod.international.model;

/**
 * Created by lchang on 9/06/17.
 */

public class IWDetalleHorario {
    private String curso;
    private String profesor;
    private String idioma;
    private String aula;

    public IWDetalleHorario(String curso, String profesor, String idioma, String aula) {
        this.curso = curso;
        this.profesor = profesor;
        this.idioma = idioma;
        this.aula = aula;
    }


    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }
}
