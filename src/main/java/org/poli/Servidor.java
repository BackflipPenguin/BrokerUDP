package org.poli;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

public class Servidor {

    private static final int PORT = 12345;

    private Map<String, Map<InetAddress, Long>> topicSubscriptions;
    private ExecutorService executorService;

    private InetSocketAddress localAddress;
    private HashMap<String, Topico> topicos;

    public boolean checkFragment(String s, long hash){
        CRC32 crc32 = new CRC32();
        crc32.update(s.getBytes(StandardCharsets.UTF_8));
        return hash == crc32.getValue();
    }
    public void start() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(localAddress);

        System.out.println("UDP broker server started on port " + PORT);
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
            System.out.println(hash);
            if (!checkFragment(mensaje, hash)){
                var f = new Fragmento(mensaje, new InetSocketAddress(packet.getAddress(), packet.getPort()));
                System.out.println(Arrays.toString(f.getBytes()));
                System.out.println("MENSAJE INCORRECTO");
                continue;
            }
            Fragmento f = new Fragmento(mensaje, new InetSocketAddress(packet.getAddress(), packet.getPort()));
            var topicoDestino = topicos.get(f.getCodigoTopico());
            if (topicoDestino == null){
                System.out.println("MENSAJE ENVIADO A TOPICO INEXISTENTE: " + f.getCodigoTopico() + " CREANDOLO.");
                topicoDestino = new Topico("", f.getCodigoTopico(), channel, channel.getLocalAddress(), new Usuario(localAddress, "SERVIDOR"), 1024, executorService);
                topicos.put(topicoDestino.getCodigo(), topicoDestino);
            }

            topicoDestino.addFragmento(f);
        }
    }

    /*
    private void subscribe(String topic, InetAddress address) {
        Map<InetAddress, Long> subscriptions = topicSubscriptions.get(topic);
        if (subscriptions == null) {
            subscriptions = new ConcurrentHashMap<>();
            topicSubscriptions.put(topic, subscriptions);
        }

        subscriptions.put(address, System.currentTimeMillis());
    }

    private void publish(String topic, String message, InetAddress address) {
        Map<InetAddress, Long> subscriptions = topicSubscriptions.get(topic);
        if (subscriptions != null) {
            for (InetAddress subscriberAddress : subscriptions.keySet()) {
                if (subscriberAddress != address) {
                    try {
                        sendMessage(message, subscriberAddress);
                    } catch (IOException e){
                        System.out.println("Error enviando mensaje: " + e.toString());
                    }
                }
            }
        }
    }

    private void sendMessage(String message, InetAddress address) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
        DatagramChannel socket = DatagramChannel.open();
        socket.send(ByteBuffer.wrap(buffer), new InetSocketAddress(address, PORT));
    }

     */

    public Servidor(InetSocketAddress localAddress){
        this.localAddress = localAddress;
        this.executorService = Executors.newFixedThreadPool(10);
        this.topicos = new HashMap<>();
    }

    public static void main(String[] args) throws IOException {
        Servidor server = new Servidor( new InetSocketAddress(InetAddress.getLocalHost(), PORT));
        server.start();
    }
}
