package org.poli;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

public class Servidor {

    private final ExecutorService executorService;
    private final InetSocketAddress localAddress;
    private final HashMap<String, Topico> topicos;

    private final Usuario yo;
    private Cripto cripto;

    public boolean checkFragment(String s, long hash){
        CRC32 crc32 = new CRC32();
        crc32.update(s.getBytes(StandardCharsets.UTF_8));
        return hash == crc32.getValue();
    }
    public void start() throws IOException, SignatureException {
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(localAddress);
        topicos.put("SYS", new Topico("SYSTEM", "SYS", channel, localAddress, yo, 1024, true, cripto, executorService));

        System.out.println("UDP Broker Servidor iniciado en puerto: " + this.localAddress.getPort());
        var socket = channel.socket();

        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            var data = packet.getData();

            // recibir mensaje
            String mensaje = new String(data, 8, packet.getLength() -8, StandardCharsets.UTF_8);
            byte[] longbytes = new byte[8];
            System.arraycopy(data, 0, longbytes, 0, 8);
            long hash = Utils.bytesToLong(longbytes);
            System.out.println("[SERVIDOR] RECEPCION DE: " + packet.getAddress() + " " + packet.getPort());
            if (!checkFragment(mensaje, hash)){
               System.out.println("MENSAJE INCORRECTO");
                continue;
            }
            String[] partes = mensaje.split(":", 7);
            if (partes.length != 7){
                System.out.println("MENSAJE INCORRECTO");
                continue;
            }
            var creador = topicos.get("SYS").getSuscriptor(partes[0]);
            if (creador == null){
                creador = new Usuario(new InetSocketAddress(packet.getAddress(), packet.getPort()), partes[0], null);
            }
            var f = new Fragmento(partes, creador, cripto);
            var topicoDestino = topicos.get(f.getCodigoTopico());
            if (topicoDestino == null){
                System.out.println("MENSAJE ENVIADO A TOPICO INEXISTENTE: " + f.getCodigoTopico() + " CREANDOLO.");
                topicoDestino = new Topico("", f.getCodigoTopico(), channel, yo, yo, 1024, true, cripto, topicos.get("SYS"), executorService);
                topicos.put(topicoDestino.getCodigo(), topicoDestino);
            }

            try {
                topicoDestino.addFragment(f);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Servidor(InetSocketAddress localAddress) throws NoSuchAlgorithmException {
        this.localAddress = localAddress;
        this.executorService = Executors.newFixedThreadPool(10);
        this.cripto = new Cripto();
        this.topicos = new HashMap<>();
        yo = new Usuario(localAddress, "SERVIDOR", cripto.getPublicKey());
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, SignatureException {
        var s = new Scanner(System.in);
        System.out.println("Ingrese el puerto a utilizar: ");
        var puerto = s.nextInt();
        Servidor server = new Servidor( new InetSocketAddress(puerto));
        server.start();
    }
}
