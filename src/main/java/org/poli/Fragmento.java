package org.poli;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Objects;
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
    private String uuidMensaje;
    private String texto;
    private boolean acknowledged;
    private int tamanoHeader;
    private String header;
    private Cripto cripto;
    private Estado estado;
    private PublicKey destino;
    public String getHeader() {
        return header;
    }
    public void setHeader(String header) {
        this.header = header;
    }

    public Fragmento(Usuario creador, int indice, byte[] contenido, Cripto cripto) {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
        this.texto = new String(contenido, StandardCharsets.UTF_8);
        this.cripto = cripto;
    }

    public Fragmento(Usuario creador, int indice, byte[] contenido, CRC32 crc32, Cripto cripto ) {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
        this.crc32 = crc32;
        this.texto = new String(contenido, StandardCharsets.UTF_8);
        this.cripto = cripto;
    }
    public Fragmento(Usuario creador, int indice, int totalPaquetes, byte[] contenido, CRC32 crc32, Cripto cripto) {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
        this.crc32 = crc32;
        this.totalPaquetes = totalPaquetes;
        this.texto = new String(contenido, StandardCharsets.UTF_8);
        this.cripto = cripto;
    }
    public Fragmento(Usuario creador, String uuidMensaje, int indice, int totalPaquetes, byte[] contenido, String codigoTopico, CRC32 crc32, Cripto cripto) throws SignatureException {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
        this.uuidMensaje = uuidMensaje;
        this.crc32 = crc32;
        this.totalPaquetes = totalPaquetes;
        this.texto = new String(contenido, StandardCharsets.UTF_8);
        this.codigoTopico = codigoTopico;
        this.cripto = cripto;
        this.destino = null;
        generateHeader(creador, uuidMensaje,  indice, totalPaquetes, codigoTopico);
    }
    public Fragmento(Usuario creador, String uuidMensaje, int indice, int totalPaquetes, byte[] contenido, String codigoTopico, CRC32 crc32, Cripto cripto, PublicKey destino) throws SignatureException {
        this.creador = creador;
        this.indice = indice;
        this.contenido = contenido;
        this.uuidMensaje = uuidMensaje;
        this.crc32 = crc32;
        this.totalPaquetes = totalPaquetes;
        this.texto = new String(contenido, StandardCharsets.UTF_8);
        this.codigoTopico = codigoTopico;
        this.cripto = cripto;
        this.destino = destino;
        generateHeader(creador, uuidMensaje,  indice, totalPaquetes, codigoTopico);
    }
    private void generateHeader(Usuario creador, String uuidMensaje, int indice, int totalPaquetes, String codigoTopico) throws SignatureException {
        String firma = "";
        if (this.destino != null){
           firma = cripto.generarFirma(contenido);
        }
        this.header = creador.getNombre() + ":" + uuidMensaje + ":" + indice + ":" + totalPaquetes + ":" + codigoTopico + ":" + firma + ":";
        this.tamanoHeader = (this.header.length() * 2) + 8; // 8 bytes del CRC32, char = 2 bytes
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

    public Cripto getCripto() {
        return cripto;
    }

    public Estado getEstado() {
        return estado;
    }

    public PublicKey getDestino() {
        return destino;
    }

    public byte[] getEnvio() {
        return envio;
    }

    public void setEnvio(byte[] envio) {
        this.envio = envio;
    }

    public CRC32 getCrc32() {
        return Objects.requireNonNullElseGet(this.crc32, CRC32::new);
    }

    public void setCrc32(CRC32 crc32) {
        this.crc32 = crc32;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getUuidMensaje() {
        return uuidMensaje;
    }

    public void setUuidMensaje(String uuidMensaje) {
        this.uuidMensaje = uuidMensaje;
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

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public void setTamanoHeader(int tamanoHeader) {
        this.tamanoHeader = tamanoHeader;
    }

    public Fragmento(String[] partes, Usuario autor, Cripto cripto) throws SignatureException {
        if (partes.length != 7) {
            throw new RuntimeException();
        }
        this.cripto = cripto;
        this.creador = autor;
        this.uuidMensaje = partes[1];
        this.indice = Integer.parseInt(partes[2]);
        this.totalPaquetes = Integer.parseInt(partes[3]);
        this.codigoTopico = partes[4];
        if (!partes[5].isEmpty()){
            try {
                this.texto = cripto.desencriptar(partes[6].getBytes());
            } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeyException e) {
                System.out.println("[FRAGMENTO] ERROR DESENCRIPTANDO FRAGMENTO " + this.indice + " DEL MENSAJE CON UUID: " + this.uuidMensaje);
            }
            try {
                if (cripto.verificarFirma(this.texto, partes[5], this.creador.getPubKey())) {
                    this.estado = Estado.CORRECTO;
                } else {
                    this.estado = Estado.FALLA_FIRMA;
                }
            } catch (SignatureException | InvalidKeyException e) {
                this.estado = Estado.FALLA_FIRMA;
            }
        } else {
            this.estado = Estado.CORRECTO;
            this.texto = partes[6];
        }
        contenido = this.texto.getBytes(StandardCharsets.UTF_8);
        generateHeader(creador, uuidMensaje,  indice, totalPaquetes, codigoTopico);
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

    //                           1      2     3          4           5      6        7
    // ESTRUCTURA FRAGMENTO    HASH:CREADOR:INDICE:TOTAL_PAQUETES:TOPICO:CONTENIDO:FIRMA
    public byte[] getBytes() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        var contenido = this.contenido;
        if (destino != null){
           contenido = cripto.encriptar(contenido,destino) ;
        }
        var envio = Utils.arrayConcat(header.getBytes(StandardCharsets.UTF_8), contenido);
        CRC32 crc = getCrc32();
        crc.update(envio);
        hash = crc.getValue();
        crc.reset();
        return Utils.arrayConcat(Utils.longToBytes(hash), envio);
    }



}
