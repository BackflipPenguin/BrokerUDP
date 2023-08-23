package org.poli;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

public class Topico {
    private String nombre;
    private String codigo;
    private CRC32 crc32;
    private ArrayList<Mensaje> mensajes;
    private DatagramChannel canal;
    private SocketAddress servidor;
    private Pattern patron;
    private int tamanoDatagrama;
    Usuario usuario;

    public Topico(String nombre, String codigo, ArrayList<Mensaje> mensajes, DatagramChannel canal, Pattern patron, SocketAddress servidor, Usuario usuario, int tamanoDatagrama) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.mensajes = mensajes;
        this.canal = canal;
        this.patron = patron;
        this.tamanoDatagrama = tamanoDatagrama;
        this.crc32 = new CRC32();
        this.servidor = servidor;
        this.usuario = usuario;
    }

    private HashSet<Usuario> suscriptores;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public void suscribir(Usuario usuario){
        suscriptores.add(usuario);
    }

    public void broadcast(Mensaje mensaje){

    }

    public void enviar(Mensaje mensaje) throws IOException {
        int tamanoDatagrama = this.tamanoDatagrama;
        String texto = mensaje.toString();
        int tamanoHeader = new Fragmento(usuario, 1, 1,
                new byte[0], this.codigo, crc32).getTamanoHeader();
        tamanoDatagrama -= tamanoHeader;
        int cantFragmentos = texto.length() / tamanoDatagrama ;
        if (cantFragmentos > 9 && cantFragmentos < 100){
            tamanoDatagrama -= 2;
        } else if (cantFragmentos > 100) {
            tamanoDatagrama -= 4;
        }
/*
        if (cantFragmentos == 0){
            canal.send(ByteBuffer.wrap(texto.getBytes()), servidor);
        }*/
        ArrayList<Fragmento> fragmentos = new ArrayList<>();


        for (int i = 0; i <= cantFragmentos; i++){
            if (i == 10){
                tamanoDatagrama -= 2;
            } else if (i == 100) {
                tamanoDatagrama -= 2;
            }

            int indexfin = tamanoDatagrama * (i+1);
            if (texto.length() < indexfin){
                indexfin = texto.length();
            }
            fragmentos.add(new Fragmento(usuario, i, cantFragmentos,
                    texto.substring(i * tamanoDatagrama, indexfin).getBytes(), this.codigo,  crc32));
        }

        for (var f: fragmentos) {
            canal.send(ByteBuffer.wrap(f.getBytes()), servidor);
        }
    }

    public ArrayList<Mensaje> recibir(){

        return null;
    }

}
