package org.poli;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.zip.CRC32;

public class Cliente {
    private Cripto cripto;
    private DatagramChannel channel;
    private HashMap<String, Topico> topicos;
    private Usuario yo;
    private ExecutorService executorService;
    public void enviar(String mensaje, String codigoTopico) throws IOException {
        var t = topicos.get(codigoTopico);
        if (t == null) {
            t = new Topico("", codigoTopico,
                    channel,
                    topicos.get("SYS").getSuscriptor("SERVIDOR"), yo, Utils.FRAG_SIZE, false, cripto, topicos.get("SYS"), executorService);
            topicos.put(t.getCodigo(), t);
        }
        t.enviar(mensaje);

   }

   public void subscribirse(String codigoTopico) throws IOException {
       var t = topicos.get(codigoTopico);
       if (t == null) {
           t = new Topico("", codigoTopico,
                   channel,
                   topicos.get("SYS").getSuscriptor("SERVIDOR"), yo, Utils.FRAG_SIZE, false, cripto, topicos.get("SYS"), executorService);
           topicos.put(t.getCodigo(), t);
       }
       t.subscribirse();
   }
    public void recibir() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        var continuar = true;

        var socket = channel.socket();
        System.out.println("RECIBIENDO MENSAJES EN  " + channel.getLocalAddress());
        while (continuar){
            byte[] buffer = new byte[Utils.FRAG_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            var data = packet.getData();

            // recibir mensaje
            String mensaje = new String(data, 8, packet.getLength() -8, StandardCharsets.UTF_8);
            byte[] longbytes = new byte[8];
            System.arraycopy(data, 0, longbytes, 0, 8);
            long hash = Utils.bytesToLong(longbytes);
            if (!checkFragment(mensaje, hash)){
                /*
                String[] partes = mensaje.split(":", 7);
                var f = new Fragmento(partes, );
                System.out.println(Arrays.toString(f.getBytes()));

                 */
                System.out.println("MENSAJE INCORRECTO");
                continue;
            }
            String[] partes = mensaje.split(":", 8);
            if (partes.length != 8){
                System.out.println("MENSAJE INCORRECTO");
                continue;
            }
            var creador = topicos.get("SYS").getSuscriptor(partes[0]);
            if (creador == null){
                creador = new Usuario(new InetSocketAddress(packet.getAddress(), packet.getPort()), partes[0], null, null);
            }
            if (!creador.getNombre().equals("SERVIDOR"))
                creador.setSecretKey(topicos.get("SYS").getSuscriptor("SERVIDOR").getSecretKey());
            Fragmento f = new Fragmento(partes, creador, cripto);
            var t = topicos.get(f.getCodigoTopico());
            try {
                t.addFragment(f);
            } catch (IOException e){
                e.printStackTrace();
            }
       }
    }

    public void enviar() throws IOException {
        var s = new Scanner(System.in);
        while (true){
            var input = s.nextLine();
            var partes = input.split(":", 2);
            if (partes.length < 2){
                System.out.println("Debe especificar el tópico");
            } else{
                if (partes[1].equals("\\subscribe")) {
                    subscribirse(partes[0]);
                } else {
                    if(partes[1].startsWith("\\ft")) {
                        var fn = partes[1].split(":", 2)[1];
                        try {
                            var msg = Files.readString(Path.of(fn));
                            enviar(msg, partes[0]);
                        } catch (NoSuchFileException e) {
                            System.out.println("Archivo no encontrado: " + fn);
                        }

                    } else if (partes[1].startsWith("\\f")){
                        var fn = partes[1].split(":", 2)[1];
                        try {

                            var file = new File(fn);
                            var msg = "\\FIL\\" + file.getName()  + "\\" + Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of(file.getAbsolutePath())));
                            enviar(msg, partes[0]);
                        } catch (NoSuchFileException e) {
                            System.out.println("Archivo no encontrado: " + fn);
                        }

                    } else {
                        enviar(partes[1], partes[0]);
                    }
                }
            }
        }
    }

    public Cliente(DatagramChannel canal, String nombreUsuario, InetSocketAddress serverAddr, ExecutorService executorService) throws NoSuchAlgorithmException, IOException {
        this.channel = canal;
        this.topicos = new HashMap<>();
        this.executorService = executorService;
        this.cripto = new Cripto();
        this.yo = new Usuario(new InetSocketAddress(String.valueOf(channel.socket().getLocalAddress()), channel.socket().getLocalPort()), nombreUsuario, cripto.getPublicKey(), cripto.generarSecreto());
        topicos.put("SYS", new Topico("SYSTEM", "SYS", channel, serverAddr, yo, Utils.FRAG_SIZE, false, cripto, executorService));
        topicos.get("SYS").registrarse();
    }

    public void start() throws IOException {
        executorService.submit( () -> {
            try {
                recibir();
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
                throw new RuntimeException(e);
            }
        });

        enviar();
    }




    public boolean checkFragment(String s, long hash){
        CRC32 crc32 = new CRC32();
        crc32.update(s.getBytes(StandardCharsets.UTF_8));
        return hash == crc32.getValue();
    }

}
