package org.poli;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Usuario {
    private InetAddress direccion;
    private String nombre;

    public Usuario(InetAddress direccion, String nombre) {
        this.direccion = direccion;
        this.nombre = nombre;
    }

    public InetAddress getDireccion() {
        return direccion;
    }

    public void setDireccion(InetAddress direccion) {
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
