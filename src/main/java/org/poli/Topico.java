package org.poli;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

public class Topico {
    private String nombre;
    private String prefijo;
    private CRC32 crc32;
    private ArrayList<Mensaje> mensajes;
    private DatagramChannel canal;
    private SocketAddress servidor;
    private Pattern patron;
    private int tamanoDatagrama;
    Usuario usuario;

    public Topico(String nombre, String prefijo, ArrayList<Mensaje> mensajes, DatagramChannel canal, Pattern patron, int tamanoDatagrama) {
        this.nombre = nombre;
        this.prefijo = prefijo;
        this.mensajes = mensajes;
        this.canal = canal;
        this.patron = patron;
        this.tamanoDatagrama = tamanoDatagrama;
        this.crc32 = new CRC32();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    public ArrayList<Mensaje> getMensajes() {
        return mensajes;
    }

    public void setMensajes(ArrayList<Mensaje> mensajes) {
        this.mensajes = mensajes;
    }

    public DatagramChannel getCanal() {
        return canal;
    }

    public void setCanal(DatagramChannel canal) {
        this.canal = canal;
    }

    public Pattern getPatron() {
        return patron;
    }

    public void setPatron(Pattern patron) {
        this.patron = patron;
    }

    public void enviar(Mensaje mensaje) throws IOException {
        String texto = prefijo + mensaje.toString();
        int cantFragmentos = texto.length() / tamanoDatagrama;
        ArrayList<Fragmento> fragmentos = new ArrayList<>();

        for (int i = 0; i < cantFragmentos; i++){
            fragmentos.add(new Fragmento(usuario, i,
                    texto.substring(i * tamanoDatagrama, tamanoDatagrama).getBytes(), crc32));
        }

        for (var f:
             fragmentos) {
            canal.send(ByteBuffer.wrap(f.getBytes()), servidor);
        }
        canal.send(ByteBuffer.wrap(texto.getBytes(StandardCharsets.UTF_8)), servidor);

    }

    public ArrayList<Mensaje> recibir(){

        return null;
    }

}
