package org.poli;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return direccion.equals(usuario.direccion) && nombre.equals(usuario.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(direccion, nombre);
    }
}
