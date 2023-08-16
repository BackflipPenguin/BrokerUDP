package org.poli;

import java.util.Arrays;
import java.util.zip.CRC32;

public class Fragmento {
    private Usuario creador;
    private int indice;
    private byte[] contenido;
    private CRC32 crc32;
    long hash;

    public Fragmento(Usuario creador, int indice, byte[] contenido) {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
    }

    public Fragmento(Usuario creador, int indice, byte[] contenido, CRC32 crc32 ) {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
        this.crc32 = crc32;
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
    public byte[] getBytes(){

         var envio = Utils.arrayConcat(Utils.arrayConcat(creador.toString().getBytes(),
                new byte[]{(byte) indice}), contenido);
         crc32.update(envio);
         hash = crc32.getValue();
         crc32.reset();
         return Utils.arrayConcat(envio, new byte[]{(byte) hash});
    }



}
