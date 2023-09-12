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
    private Topico topicoSYS;
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
        topicoSYS = this;
    }
    public Topico(String nombre, String codigo, DatagramChannel canal, SocketAddress serverAddr, Usuario usuario, int tamanoDatagrama, boolean server, Topico topicoSYS, ExecutorService executorService) {
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
        this.topicoSYS = topicoSYS;
    }
    public String getNombre() {
        return nombre;
    }

    public CRC32 getCrc32() {
        return crc32;
    }

    public HashMap<String, Mensaje> getMensajes() {
        return mensajes;
    }

    public void setMensajes(HashMap<String, Mensaje> mensajes) {
        this.mensajes = mensajes;
    }

    public ArrayList<Usuario> getSuscriptores() {
        return suscriptores;
    }

    public void setSuscriptores(ArrayList<Usuario> suscriptores) {
        this.suscriptores = suscriptores;
    }

    public SocketAddress getServerAddr() {
        return serverAddr;
    }

    public boolean isServer() {
        return server;
    }

    public void setServer(boolean server) {
        this.server = server;
    }

    public int getTamanoDatagrama() {
        return tamanoDatagrama;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Topico getTopicoSYS() {
        return topicoSYS;
    }

    public void setTopicoSYS(Topico topicoSYS) {
        this.topicoSYS = topicoSYS;
    }

    public boolean isSubscripto() {
        return subscripto;
    }

    public void setSubscripto(boolean subscripto) {
        this.subscripto = subscripto;
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

    private String handleCommand(String[] sections, Mensaje mensaje, Fragmento fragmento) throws IOException {
        switch (sections[1]) {
            case ("SUB") -> {
                executorService.submit(() -> suscribir(mensaje.getCreador()));
                return "SUB";
            }
            case ("ACK") -> {
                if (sections.length != 4)
                    enviarSYS("\\ERR\\" + mensaje.getUuid() + "\\ARG", fragmento.getCreador().getDireccion());
                try {
                    mensajes.get(sections[2]).ackFragment(Integer.parseInt(sections[3]));
                } catch (NullPointerException e){
                    enviarSYS("\\ERR\\" + mensaje.getUuid() + "\\NOMSG", fragmento.getCreador().getDireccion());
                }
                return "ACK";
            }
        }
        return null;
    }

    public String addFragment(Fragmento fragmento) throws IOException {
        String cmd = "MSG";
        boolean ack = true;
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
            System.out.println("COMPLETADA LA RECEPCION DEL MENSAJE CON UUID: " + mensaje.getUuid() + " DE: " + fragmento.getCreador().getDireccion());
            System.out.println(mensaje.getContenido());
                if (mensaje.getContenido().startsWith("\\")) {
                    var sections = mensaje.getContenido().split("\\\\");
                    cmd = handleCommand(sections, mensaje, fragmento);
                    if (cmd.equals("ACK"))
                        ack = false;

                } else {
                    Mensaje finalMensaje1 = mensaje;
                    executorService.submit(() -> {
                        broadcast(finalMensaje1);
                    });
                }
        }

        if (ack){
            enviarACK(fragmento);
        }
        return cmd;
    }

    public void setCanal(DatagramChannel canal) {
        this.canal = canal;
    }

    public void subscribirse() throws IOException {
       enviar("\\SUB");
       subscripto = true;
    }
    public void suscribir(Usuario usuario){
        suscriptores.add(usuario);
        System.out.println("EL USUARIO " + usuario.getNombre() + " HA SIDO SUSCRIPTO AL TOPICO: " + codigo);
    }
    public void enviarACK(Fragmento f) throws IOException {
        System.out.println("ENVIANDO ACK DEL FRAGMENTO: " + f.getIndice() + " CORRESPONDIENTE AL MENSAJE CON UUID:" + f.getUuidMensaje() + " A: " + f.getCreador().getDireccion());
        enviar( "\\ACK\\" + f.getUuidMensaje() + "\\" + f.getIndice(), f.getCreador().getDireccion());
    }
    public void broadcast(Mensaje mensaje ) {
        System.out.println("INICIANDO BROADCAST DEL MENSAJE CON UUID: " + mensaje.getUuid());
        for (Usuario s:
             suscriptores) {
            executorService.submit(() -> {
                try {
                    // no enviar a donde recibimos
                    if (!(s.getNombre().equals(mensaje.getCreador().getNombre()))){

                        System.out.println("ENVIANDO MENSAJE " + mensaje.getUuid() + " A: " + s.getNombre());
                        // se debe recrear el mensaje para que tenga distinto uuid segun a quien se envia

                        enviar(new Mensaje(mensaje.getContenido(), mensaje.getCreador(), codigo, crc32, tamanoDatagrama), s.getDireccion());
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

    public void enviarSYS(String contenidoMensaje, SocketAddress destino) throws IOException {
        var mensaje = new Mensaje(contenidoMensaje, usuario, "SYS", crc32, tamanoDatagrama);
        topicoSYS.enviar(mensaje, destino);
    }

    public void enviar(String contenidoMensaje) throws IOException {
        enviar(contenidoMensaje, this.serverAddr);
    }
    public void enviar(String contenidoMensaje, SocketAddress destino) throws IOException {
        var mensaje = new Mensaje(contenidoMensaje, usuario, codigo, crc32, tamanoDatagrama);
        enviar(mensaje, destino);
    }
    public void enviar(Mensaje mensaje, SocketAddress destino) throws IOException {
        // agregar mensaje a los mensajes del canal
        mensajes.put(mensaje.getUuid(), mensaje);
        System.out.println("ENVIANDO MENSAJE " + mensaje.getUuid() + " A: " + destino);
        var fragmentos = mensaje.generarFragmentos();

        for (var f: fragmentos) {
            var bytes = f.getBytes();
            canal.send(ByteBuffer.wrap(bytes), destino);
        }
    }
}
