package org.poli;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Usuario {
    private InetSocketAddress direccion;
    private String nombre;

    public Usuario(InetSocketAddress direccion, String nombre) {
        this.direccion = direccion;
        this.nombre = nombre;
    }

    public InetSocketAddress getDireccion() {
        return direccion;
    }

    public void setDireccion(InetSocketAddress direccion) {
        this.direccion = direccion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String toString(){
        return nombre;
    }
}
