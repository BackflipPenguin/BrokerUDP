package org.poli;

import java.util.ArrayList;
import java.util.HashMap;

public class Mensaje {
    private String contenido;
    private Usuario creador;
    private String codigoTopico;

    public Mensaje(String contenido, Usuario creador) {
        this.contenido = contenido;
        this.creador = creador;
    }

    public Mensaje(HashMap<Integer, Fragmento> fragmentos, int cantFragmentos){
        ArrayList<String> contenidos = new ArrayList<>();
        this.creador = fragmentos.get(0).getCreador();
        for (int i = 0; i < cantFragmentos; i++) {
            contenidos.add(fragmentos.get(i).getTexto());
        }
        this.contenido = String.join("", contenidos);
    }

    public String getCodigoTopico() {
        return codigoTopico;
    }

    public void setCodigoTopico(String codigoTopico) {
        this.codigoTopico = codigoTopico;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Usuario getCreador() {
        return creador;
    }

    public void setCreador(Usuario creador) {
        this.creador = creador;
    }

    @Override
    public String toString() {
        return contenido;
    }
}
