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

    private int tamanoHeader;

    private String header;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

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
    public Fragmento(Usuario creador, int indice, int totalPaquetes, byte[] contenido, String codigoTopico, CRC32 crc32) {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
        this.crc32 = crc32;
        this.totalPaquetes = totalPaquetes;
        this.texto = new String(contenido, StandardCharsets.UTF_8);
        this.codigoTopico = codigoTopico;
        generateHeader(creador, indice, totalPaquetes, codigoTopico);
    }

    private void generateHeader(Usuario creador, int indice, int totalPaquetes, String codigoTopico){
        this.header = creador + ":" + Integer.toString
                (indice) + ":" + Integer.toString(totalPaquetes) + ":" + codigoTopico + ":";
        this.tamanoHeader = this.header.length() * 2 + 8; // 8 bytes del CRC32
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
        if (this.crc32 != null)
            return crc32;
        else
            return new CRC32();
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

    public String getCodigoTopico() {
        return codigoTopico;
    }

    public void setCodigoTopico(String codigoTopico) {
        this.codigoTopico = codigoTopico;
    }

    public int getTamanoHeader() {
        return tamanoHeader;
    }

    public void setTamanoHeader(int tamanoHeader) {
        this.tamanoHeader = tamanoHeader;
    }

    public Fragmento(String recibido, InetAddress addr){
        String[] partes = recibido.split(":", 5);
        if (partes.length != 5) {
            return;
        }
        creador = new Usuario(addr, partes[0]);
        indice = Integer.parseInt(partes[1]);
        totalPaquetes = Integer.parseInt(partes[2]);
        this.codigoTopico = partes[3];
        this.texto  = partes[4];
        this.contenido = texto.getBytes(StandardCharsets.UTF_8);
        generateHeader(creador, indice, totalPaquetes, codigoTopico);
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
        var envio = Utils.arrayConcat(header.getBytes(StandardCharsets.UTF_8), contenido);
        CRC32 crc = this.getCrc32();
        crc.update(envio);
        hash = crc.getValue();
        crc.reset();
        System.out.println(hash);
        System.out.println(new String(envio, StandardCharsets.UTF_8));
        return Utils.arrayConcat(Utils.longToBytes(hash), envio);

    }



}
