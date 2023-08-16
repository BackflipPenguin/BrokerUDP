package org.poli;

import java.net.InetAddress;

public class Mensaje {
    private String contenido;
    private InetAddress origen;
    private String creador;

    public Mensaje(String contenido, InetAddress origen, String creador) {
        this.contenido = contenido;
        this.origen = origen;
        this.creador = creador;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public InetAddress getOrigen() {
        return origen;
    }

    public void setOrigen(InetAddress origen) {
        this.origen = origen;
    }

    public String getCreador() {
        return creador;
    }

    public void setCreador(String creador) {
        this.creador = creador;
    }

    @Override
    public String toString() {
        return contenido;
    }
}
