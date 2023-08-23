package org.poli;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

public class Servidor {

    private static final int PORT = 12345;

    private Map<String, Map<InetAddress, Long>> topicSubscriptions;
    private ExecutorService executorService;

    public Servidor() {
        topicSubscriptions = new ConcurrentHashMap<>();
        executorService = Executors.newFixedThreadPool(10);
    }

    public boolean checkFragment(String s, long hash){
        CRC32 crc32 = new CRC32();
        crc32.update(s.getBytes(StandardCharsets.UTF_8));
        return hash == crc32.getValue();
    }
    public void start() throws IOException {
        DatagramSocket socket = new DatagramSocket(PORT);
        System.out.println("UDP broker server started on port " + PORT);

        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            var data = packet.getData();
            // recibir mensaje
            String message = new String(data, 8, packet.getLength() -8, StandardCharsets.UTF_8);
            byte[] longbytes = new byte[8];
            System.arraycopy(data, 0, longbytes, 0, 8);
            long hash = Utils.bytesToLong(longbytes);
            System.out.println(hash);
            if (!checkFragment(message, hash)){
                var f = new Fragmento(message, packet.getAddress());
                System.out.println(Arrays.toString(f.getBytes()));
                System.out.println("MENSAJE INCORRECTO");
                continue;
            }
            int fragmentosTotales = 0;
            HashMap<Integer, Fragmento> fragmentos = new HashMap<>();
            Fragmento f = new Fragmento(message, packet.getAddress());
            fragmentosTotales = f.getTotalPaquetes() + 1;
            fragmentos.put(f.getIndice(),f);
            for (int i = 1; i < fragmentosTotales; i++) {
                socket.receive(packet);
                data = packet.getData();
                message = new String(data, 8, packet.getLength() - 8);
                System.arraycopy(data,0, longbytes, 0, 8);

                if (!checkFragment(message, Utils.bytesToLong(longbytes))){
                    System.out.println("FRAGMENTO INCORRECTO: " + f.getIndice());
                }
                // System.out.println((new Date()).toString() + ": " +message);
                Fragmento nuevoFragmento = new Fragmento(message, );
                fragmentos.put(nuevoFragmento.getIndice(), nuevoFragmento);
            }
            Mensaje mensaje = new Mensaje(fragmentos, fragmentosTotales);
            System.out.println("MENSAJE: " + mensaje.getCreador().getNombre() + " : " + mensaje.getContenido());


            if (mensaje.getContenido().equals("\\subscribe")) {
                executorService.submit(() -> subscribe(mensaje.getCodigoTopico(), packet.getAddress()));
            } else {
                executorService.submit(() -> publish(mensaje.getCodigoTopico(), mensaje.getContenido(), packet.getAddress()));
            }
        }
    }

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

    public static void main(String[] args) throws IOException {
        Servidor server = new Servidor();
        server.start();
    }
}
