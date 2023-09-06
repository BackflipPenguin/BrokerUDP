package org.poli;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.zip.CRC32;

public class Topico {
    private String nombre;
    private String codigo;
    private final CRC32 crc32;
    private HashMap<String, Mensaje> mensajes;
    private ArrayList<Usuario> suscriptores;
    private DatagramChannel canal;
    private final SocketAddress serverAddr;
    private boolean server;
    private final int tamanoDatagrama;
    private ExecutorService executorService;
    Usuario usuario;

    private boolean subscripto = false;

    public Topico(String nombre, String codigo, DatagramChannel canal, SocketAddress serverAddr, Usuario usuario, int tamanoDatagrama, boolean server, ExecutorService executorService) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.mensajes = new HashMap<>();
        this.canal = canal;
        this.tamanoDatagrama = tamanoDatagrama;
        this.crc32 = new CRC32();
        this.serverAddr = serverAddr;
        this.usuario = usuario;
        this.executorService = executorService;
        this.server = server;
        this.suscriptores = new ArrayList<>();
    }

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

    public DatagramChannel getCanal() {
        return canal;
    }

    public void addFragmento(Fragmento fragmento){
        var mensaje = mensajes.get(fragmento.getUuidMensaje());
        if (mensaje == null){
            System.out.println("RECIBIENDO NUEVO MENSAJE CON UUID: " + fragmento.getUuidMensaje());
            mensaje = new Mensaje(fragmento);
            mensajes.put(mensaje.getUuid(), mensaje);
        }
        else {
            mensaje.addFragmento(fragmento);
        }
        if (mensaje.getEstado() == EstadoMensaje.CORRECTO){
            System.out.println("COMPLETADA LA RECEPCION DEL MENSAJE CON UUID: " + mensaje.getUuid());
            System.out.println(mensaje.getContenido());
            if (server){
                if (mensaje.getContenido().equals("\\subscribe")) {
                    Mensaje finalMensaje = mensaje;
                    executorService.submit(() -> suscribir(finalMensaje.getCreador()) );
                } else {
                    broadcast(mensaje);
                }
            }
        }
    }

    public void setCanal(DatagramChannel canal) {
        this.canal = canal;
    }


    public void subscribirse() throws IOException {
       enviar("\\subscribe");
       subscripto = true;
    }
    public void suscribir(Usuario usuario){
        suscriptores.add(usuario);
        System.out.println("EL USUARIO " + usuario.getNombre() + " HA SIDO SUSCRIPTO AL TOPICO: " + codigo);
    }

    public void broadcast(Mensaje mensaje ) {
        System.out.println("INICIANDO BROADCAST DEL MENSAJE CON UUID: " + mensaje.getUuid());
        for (Usuario s:
             suscriptores) {
            executorService.submit(() -> {
                try {
                    if (!(s.getNombre().equals(mensaje.getCreador().getNombre()))){

                        System.out.println("ENVIANDO MENSAJE " + mensaje.getUuid() + " A: " + s.getNombre());
                        enviar(mensaje, s.getDireccion());
                    }
                } catch (IOException e) {
                    System.out.println("Error en broadcast enviando a usuario: " + s.getNombre() + "\n" + e );
                }
            });
        }
    }

    public void enviar(Mensaje mensaje) throws IOException {
        enviar(mensaje, this.serverAddr);
    }
    public void enviar(String contenidoMensaje) throws IOException {
        var mensaje = new Mensaje(contenidoMensaje, usuario, codigo, crc32, tamanoDatagrama);
        enviar(mensaje, this.serverAddr);
    }
    public void enviar(Mensaje mensaje, SocketAddress destino) throws IOException {

        /*
        int tamanoDatagrama = this.tamanoDatagrama;
        String texto = mensaje.toString();
        int tamanoHeader = new Fragmento(usuario, 1, 1,
                new byte[0], this.codigo, crc32).getTamanoHeader();
        tamanoDatagrama -= tamanoHeader;
        int cantFragmentos = texto.length() / tamanoDatagrama ;
        int digitosExtra = String.valueOf(cantFragmentos).length() - 1;
        tamanoDatagrama -= digitosExtra;
       ArrayList<Fragmento> fragmentos = new ArrayList<>();


        for (int i = 0; i <= cantFragmentos; i++){
            // no se me ocurrió una solución mejor :(
            if (i == 10){
                tamanoDatagrama -= 2;
            } else if (i == 100) {
                tamanoDatagrama -= 2;
            } else if (i == 1000) {
                tamanoDatagrama -= 2;
            } else if (i == 10000) {
                tamanoDatagrama -= 2;
            }

            int indexfin = tamanoDatagrama * (i+1);
            if (texto.length() < indexfin){
                indexfin = texto.length();
            }
            fragmentos.add(new Fragmento(usuario, i, cantFragmentos,
                    texto.substring(i * tamanoDatagrama, indexfin).getBytes(), this.codigo,  crc32));
        } */

        System.out.println("ENVIANDO MENSAJE " + mensaje.getUuid() + " A: " + destino);
        var fragmentos = mensaje.generarFragmentos();

        for (var f: fragmentos) {
            var bytes = f.getBytes();
            canal.send(ByteBuffer.wrap(bytes), destino);
        }
    }
}
