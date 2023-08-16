package org.poli;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class Fragmento {
    private Usuario creador;
    private int indice;
    private byte[] contenido;

    private int totalPaquetes;
    private long hash;
    private String codigoTopico;
    private byte[] envio;
    private CRC32 crc32;
    private String texto;

    public Fragmento(Usuario creador, int indice, byte[] contenido) {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
        this.texto = new String(contenido, StandardCharsets.UTF_8);
    }

    public Fragmento(Usuario creador, int indice, byte[] contenido, CRC32 crc32 ) {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
        this.crc32 = crc32;
        this.texto = new String(contenido, StandardCharsets.UTF_8);
    }
    public Fragmento(Usuario creador, int indice, int totalPaquetes, byte[] contenido, CRC32 crc32) {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
        this.crc32 = crc32;
        this.totalPaquetes = totalPaquetes;
        this.texto = new String(contenido, StandardCharsets.UTF_8);
    }

    public void setTotalPaquetes(int totalPaquetes) {
        this.totalPaquetes = totalPaquetes;
    }

    public long getHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }

    public byte[] getEnvio() {
        return envio;
    }

    public void setEnvio(byte[] envio) {
        this.envio = envio;
    }

    public CRC32 getCrc32() {
        return crc32;
    }

    public void setCrc32(CRC32 crc32) {
        this.crc32 = crc32;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getTotalPaquetes(){
        return this.totalPaquetes;
    }

    public Fragmento(String recibido, InetAddress addr){
        String[] partes = recibido.split(":", 6);
        if (partes.length != 6) {
            return;
        }
        hash = Long.parseLong(partes[0]);
        creador = new Usuario(addr, partes[1]);
        indice = Integer.parseInt(partes[2]);
        totalPaquetes = Integer.parseInt(partes[3]);
        this.codigoTopico = partes[4];
        this.texto  = partes[5];
        this.contenido = texto.getBytes(StandardCharsets.UTF_8);
    }
    public String getTexto(){
        return texto;
    }


    public Usuario getCreador() {
        return creador;
    }

    public void setCreador(Usuario creador) {
        this.creador = creador;
    }

    public int getIndice() {
        return indice;
    }

    public void setIndice(int indice) {
        this.indice = indice;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
    }

    //                           1      2     3          4           5     6
    // ESTRUCTURA FRAGMENTO    HASH:CREADOR:INDICE:TOTAL_PAQUETES:TOPICO:CONTENIDO
    public byte[] getBytes(){
        String header = creador + ":" + Integer.toString(indice) + ":" + Integer.toString(totalPaquetes) + ":";

         var envio = Utils.arrayConcat(header.getBytes(StandardCharsets.UTF_8), contenido);
         crc32.update(envio);
         hash = crc32.getValue();
         crc32.reset();
        System.out.println(new String(envio, StandardCharsets.UTF_8));
         return Utils.arrayConcat((Long.toString(hash) + ":").getBytes(StandardCharsets.UTF_8), envio);
    }



}
