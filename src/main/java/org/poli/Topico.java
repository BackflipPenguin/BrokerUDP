package org.poli;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.zip.CRC32;

public class Topico {
    private String nombre;
    private String codigo;
    private final CRC32 crc32;
    private HashMap<String, Mensaje> mensajes;
    private HashMap<String, CommandHandler> comandos;
    private HashMap<String, Usuario> suscriptores;
    private DatagramChannel canal;
    private boolean server;
    private final int tamanoDatagrama;
    private  InetSocketAddress serverAddr;
    private ExecutorService executorService;
    private Usuario usuario;
    private Cripto cripto;
    private Topico topicoSYS;
    private boolean subscripto = false;

    private void registrarComandos(){
        CommandHandler sub = (String[] sections, Mensaje mensaje, Topico caller) -> {
            caller.executorService.submit(() -> caller.suscribir(mensaje.getCreador()));
            return "SUB";
        };
        CommandHandler ack = (String[] sections, Mensaje mensaje, Topico caller) -> {
            if (sections.length != 4)
                caller.enviarSYS("\\ERR\\" + mensaje.getUuid() + "\\ARG", mensaje.getCreador().getDireccion());
            try {
                // busca el uuid en el hashmap de mensajes y marca el fragmento como acknowledged
                mensajes.get(sections[2]).ackFragment(Integer.parseInt(sections[3]));
                if (caller.server) {
                    System.out.println("[ACK HANDLER] RECIBIDO ACK DEL FRAGMENTO: " + sections[3] + " CORRESPONDIENTE AL MENSAJE CON UUID: " + sections[2]);
                }
            } catch (NullPointerException e){
                caller.enviarSYS("\\ERR\\" + mensaje.getUuid() + "\\NOMSG", mensaje.getCreador().getDireccion());
            }
            return "ACK";
        };

        this.comandos.put("SUB", sub);
        this.comandos.put("ACK", ack);
    }

    public Topico(String nombre, String codigo, DatagramChannel canal, InetSocketAddress serverAddr, Usuario usuario, int tamanoDatagrama, boolean server, Cripto cripto, ExecutorService executorService) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.mensajes = new HashMap<>();
        this.canal = canal;
        this.tamanoDatagrama = tamanoDatagrama;
        this.crc32 = new CRC32();
        this.usuario = usuario;
        this.executorService = executorService;
        this.server = server;
        this.suscriptores = new HashMap<>();
        this.serverAddr = serverAddr;
        this.cripto = cripto;
        topicoSYS = this;
        this.comandos = new HashMap<>();
        registrarComandos();

        CommandHandler reg = (String[] sections, Mensaje mensaje, Topico caller) -> {
            if (sections.length != 3)
                caller.enviarSYS("\\ERR\\" + mensaje.getUuid() + "\\ARG", mensaje.getCreador().getDireccion());

            var encodedPubKey = new X509EncodedKeySpec(Base64.getDecoder().decode(sections[2].getBytes()));
            var kf = KeyFactory.getInstance("RSA");
            var pubkey = kf.generatePublic(encodedPubKey);
            var user = new Usuario(mensaje.getCreador().getDireccion(), mensaje.getCreador().getNombre(), pubkey);
            getExecutorService().submit(() -> caller.suscribir(user));
            return "REG";
        };
        CommandHandler key = (String[] sections, Mensaje mensaje, Topico caller) -> {
            caller.enviarSYS("\\REG\\" + Base64.getEncoder().encodeToString(caller.getUsuario().getPubKey().getEncoded()), mensaje.getCreador().getDireccion());
            return "KEY";
        };
        CommandHandler err = (String[] sections, Mensaje mensaje, Topico caller) -> {
            System.out.println("[ERR HANDLER] RESPUESTA DE ERROR: " + sections[3] + " EN EL MENSAJE CON UUID: " + sections[2]);
            return "ERR";
        };
        this.comandos.put("REG", reg);
        this.comandos.put("KEY", key);
        this.comandos.put("ERR", err);
    }
    public Topico(String nombre, String codigo, DatagramChannel canal, Usuario usuarioServer, Usuario usuario, int tamanoDatagrama, boolean server, Cripto cripto, Topico topicoSYS, ExecutorService executorService) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.mensajes = new HashMap<>();
        this.canal = canal;
        this.tamanoDatagrama = tamanoDatagrama;
        this.crc32 = new CRC32();
        this.usuario = usuario;
        this.executorService = executorService;
        this.server = server;
        this.suscriptores = new HashMap<>();
        this.suscriptores.put("SERVIDOR", usuarioServer);
        this.topicoSYS = topicoSYS;
        this.comandos = new HashMap<>();
        this.cripto = cripto;
        registrarComandos();
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

    public HashMap<String, Usuario> getSuscriptores() {
        return suscriptores;
    }

    public Usuario getSuscriptor(String nombre) {
        return suscriptores.get(nombre);
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

    public void addFragment(Fragmento fragmento) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        boolean ack = true;
        var mensaje = mensajes.get(fragmento.getUuidMensaje());
        if (mensaje == null){
            if (server) {
                System.out.println("[TOPICO] RECIBIENDO NUEVO MENSAJE CON UUID: " + fragmento.getUuidMensaje());
            }
            mensaje = new Mensaje(fragmento);
            mensajes.put(mensaje.getUuid(), mensaje);
        }
        else {
            mensaje.addFragmento(fragmento);
        }

        if (mensaje.getEstado() == Estado.CORRECTO){
            if (server){
                System.out.println("[TOPICO] COMPLETADA LA RECEPCION DEL MENSAJE CON UUID: " + mensaje.getUuid() + " DE: " + fragmento.getCreador().getDireccion() + " CON CONTENIDO: ");
                System.out.println("[TOPICO] " + mensaje.getContenido());
            }
            if (mensaje.getContenido().startsWith("\\")) {
                var sections = mensaje.getContenido().split("\\\\");
                if (sections[1].equals("ACK"))
                    ack = false;
                try {
                    comandos.get(sections[1]).handler(sections, mensaje, this); // llamar al handler correspondiente
                } catch (NullPointerException e){
                    enviarSYS("\\ERR\\" + mensaje.getUuid() + "\\NOCMD", mensaje.getCreador().getDireccion());
                }
            } else {
                Mensaje finalMensaje1 = mensaje;
                if (!server){
                    System.out.println("[" + codigo + "] " + mensaje.getCreador() + ": " + mensaje.getContenido());
                }
                executorService.submit(() -> {
                    broadcast(finalMensaje1);
                });
            }
        }

        if (ack){
            enviarACK(fragmento);
        }
    }

    public void setCanal(DatagramChannel canal) {
        this.canal = canal;
    }

    // solo para topico SYS
    public void registrarse() throws IOException {
        enviarSYS("\\REG\\" + Base64.getEncoder().encodeToString(usuario.getPubKey().getEncoded()), serverAddr);
        enviarSYS("\\KEY", serverAddr);
        enviarSYS("\\SUB", serverAddr);
        subscripto = true;
    }

    public void subscribirse() throws IOException {
       enviar("\\SUB");
       subscripto = true;
    }
    public void suscribir(Usuario usuario){
        suscriptores.put(usuario.getNombre(), usuario);
        if (server) {
            System.out.println("[TOPICO] EL USUARIO " + usuario.getNombre() + " HA SIDO SUSCRIPTO AL TOPICO: " + codigo);
        }
    }
    public void enviarACK(Fragmento f) throws IOException {
        if (server) {
            System.out.println("[TOPICO] ENVIANDO ACK DEL FRAGMENTO: " + f.getIndice() + " CORRESPONDIENTE AL MENSAJE CON UUID:" + f.getUuidMensaje() + " A: " + f.getCreador().getDireccion());
        }
        var mensaje = new Mensaje("\\ACK\\" + f.getUuidMensaje() + "\\" + f.getIndice(), usuario, codigo, crc32, tamanoDatagrama, cripto);
        try {
            enviar(mensaje, f.getCreador());
        } catch (SignatureException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
    public void broadcast(Mensaje mensaje ) {
        if (server) {
            System.out.println("[TOPICO] INICIANDO BROADCAST DEL MENSAJE CON UUID: " + mensaje.getUuid());
        }
        for (Usuario s:
             suscriptores.values()) {
            executorService.submit(() -> {
                try {
                    // no enviar a donde recibimos
                    if (!s.getNombre().equals(mensaje.getCreador().getNombre()) && !s.getNombre().equals("SERVIDOR")){

                        if (server) {
                            System.out.println("[TOPICO] ENVIANDO MENSAJE " + mensaje.getUuid() + " A: " + s.getNombre());
                        }

                        // se debe recrear el mensaje para que tenga distinto uuid segun a quien se envia para poder
                        // recibir ACKs separados
                        enviar(new Mensaje(mensaje.getContenido(), mensaje.getCreador(), codigo, crc32, tamanoDatagrama, cripto), s);
                    }
                } catch (IOException e) {
                    System.out.println("[TOPICO] Error en broadcast enviando a usuario: " + s.getNombre() + "\n" + e );
                } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | SignatureException |
                         NoSuchAlgorithmException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void enviarSYS(String contenidoMensaje, SocketAddress destino) throws IOException {
        var mensaje = new Mensaje(contenidoMensaje, usuario, "SYS", crc32, tamanoDatagrama, cripto);
        try {
            topicoSYS.enviar(mensaje, new Usuario((InetSocketAddress) destino, null, null));
        } catch (SignatureException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void enviar(String contenidoMensaje) throws IOException {
        try {
            enviar(new Mensaje(contenidoMensaje, usuario, codigo, crc32, tamanoDatagrama, cripto), topicoSYS.getSuscriptor("SERVIDOR"));
        } catch (SignatureException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void enviar(Mensaje mensaje, Usuario destino) throws IOException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // agregar mensaje a los mensajes del canal
        mensajes.put(mensaje.getUuid(), mensaje);
        if (server) {
            System.out.println("[TOPICO] ENVIANDO MENSAJE " + mensaje.getUuid() + " CON CONTENIDO: " + mensaje.getContenido() + " A: " + destino);
        }
        var fragmentos = mensaje.generarFragmentos(destino.getPubKey());

        for (var f: fragmentos) {
            var bytes = f.getBytes();
            canal.send(ByteBuffer.wrap(bytes), destino.getDireccion());
        }
    }
}
