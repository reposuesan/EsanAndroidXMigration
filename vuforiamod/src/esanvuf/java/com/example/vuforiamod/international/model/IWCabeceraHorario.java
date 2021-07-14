package com.example.vuforiamod.international.model;

import java.util.List;

/**
 * Created by lchang on 9/06/17.
 */

public class IWCabeceraHorario {

    private String titulo;
    private String descripcion;
    private boolean seleccion;
    private List<IWDetalleHorario> detalle;

    public IWCabeceraHorario(String titulo, String descripcion, List<IWDetalleHorario> detalle) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.seleccion = false;
    }


    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isSeleccion() {
        return seleccion;
    }

    public void setSeleccion(boolean seleccion) {
        this.seleccion = seleccion;
    }

    public List<IWDetalleHorario> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<IWDetalleHorario> detalle) {
        this.detalle = detalle;
    }
}
