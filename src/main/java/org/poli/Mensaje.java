package org.poli;

import java.security.PublicKey;
import java.security.SignatureException;
import java.util.*;
import java.util.zip.CRC32;

public class Mensaje {
    private String contenido;
    private Usuario creador;
    private String codigoTopico;
    private String uuid;
    private CRC32 crc32;
    private Estado estado;
    private int tamanoMaxDatagrama = 1024;
    private int totalFragmentos;
    private Cripto cripto;
    private HashMap<Integer, Fragmento> fragmentos;

    public Mensaje(String contenido, Usuario creador, String codigoTopico, CRC32 crc32, int tamanoMaxDatagrama, Cripto cripto) {
        this.contenido = contenido;
        this.creador = creador;
        this.crc32 = crc32;
        this.codigoTopico = codigoTopico;
        this.tamanoMaxDatagrama = tamanoMaxDatagrama;
        this.uuid = String.valueOf(java.util.UUID.randomUUID()).substring(0, 8);
        this.estado = Estado.CORRECTO;
        this.cripto = cripto;
    }

    public Mensaje(Fragmento fragmentoInicial){
        this.creador = fragmentoInicial.getCreador();
        this.uuid = fragmentoInicial.getUuidMensaje();
        this.codigoTopico = fragmentoInicial.getCodigoTopico();
        this.totalFragmentos = fragmentoInicial.getTotalPaquetes();
        this.estado = Estado.EN_PROGRESO;
        this.cripto = fragmentoInicial.getCripto();
        fragmentos = new HashMap<>();
        fragmentos.put(fragmentoInicial.getIndice(), fragmentoInicial);
        joinFragmentosIfComplete();
    }

    public Mensaje(HashMap<Integer, Fragmento> fragmentos, int cantFragmentos){
        this.fragmentos = fragmentos;
        ArrayList<String> contenidos = new ArrayList<>();

        this.creador = fragmentos.get(0).getCreador();
        this.cripto  = fragmentos.get(0).getCripto();
        for (int i = 0; i < cantFragmentos; i++) {
            contenidos.add(fragmentos.get(i).getTexto());
        }
        this.contenido = String.join("", contenidos);
        this.estado = Estado.CORRECTO;
    }

    private void joinFragmentosIfComplete(){
        if (this.fragmentos.size() == this.totalFragmentos){
            ArrayList<String> contenidos = new ArrayList<>();
            for (int i = 0; i < totalFragmentos; i++) {
                contenidos.add(fragmentos.get(i).getTexto());
            }
            this.contenido = String.join("", contenidos);
            this.estado = Estado.CORRECTO;
        }
    }

    public void addFragmento(Fragmento f){
        fragmentos.put(f.getIndice(), f);
        joinFragmentosIfComplete();
        /*
        if (fragmentos.size() == totalFragmentos){
            ArrayList<String> contenidos = new ArrayList<>();
            for (int i = 0; i < totalFragmentos; i++) {
                contenidos.add(fragmentos.get(i).getTexto());
            }
            this.contenido = String.join("", contenidos);
            this.estado = EstadoMensaje.CORRECTO;
        }
        */


    }

    public Collection<Fragmento> generarFragmentos(PublicKey destino) throws SignatureException {
        int tamanoDatagrama = this.tamanoMaxDatagrama;
        String texto = contenido;
        // bytes reservados para el header de los fragmentos
        int tamanoHeader = new Fragmento(creador, "12345678", 1, 1,
                new byte[0], this.codigoTopico, this.crc32, cripto, destino).getTamanoHeader();

        tamanoDatagrama -= tamanoHeader;
        int cantFragmentos = Math.max( (int) Math.ceil((double) texto.getBytes().length / tamanoDatagrama), 1);
        int digitosExtra = String.valueOf(cantFragmentos).length() - 1 ;
        tamanoDatagrama -= digitosExtra * 2;

        this.fragmentos = new HashMap<>();
        for (int i = 0; i < cantFragmentos; i++) {
            // no se me ocurrió una solución mejor :(
            if (i == 10) {
                tamanoDatagrama -= 2;
            } else if (i == 100) {
                tamanoDatagrama -= 2;
            } else if (i == 1000) {
                tamanoDatagrama -= 2;
            } else if (i == 10000) {
                tamanoDatagrama -= 2;
            }

            var cant = tamanoDatagrama;
            int indexfin = tamanoDatagrama * (i + 1)  + cant;
            if (texto.length() < indexfin) {
                cant = indexfin - texto.length();
            }
            fragmentos.put(i, new Fragmento(creador, uuid, i, cantFragmentos,
                    Utils.trimByBytes(texto, i * tamanoDatagrama, tamanoDatagrama).getBytes(), this.codigoTopico, crc32, cripto, destino));
        }

        return fragmentos.values();
    }

    public void ackFragment(int indice){
        fragmentos.get(indice).setAcknowledged(true);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
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
