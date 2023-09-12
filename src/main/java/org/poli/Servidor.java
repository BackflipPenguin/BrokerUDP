package org.poli;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

public class Servidor {

    private final ExecutorService executorService;
    private final InetSocketAddress localAddress;
    private final HashMap<String, Topico> topicos;

    public boolean checkFragment(String s, long hash){
        CRC32 crc32 = new CRC32();
        crc32.update(s.getBytes(StandardCharsets.UTF_8));
        return hash == crc32.getValue();
    }
    public void start() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(localAddress);
        topicos.put("SYS", new Topico("SYSTEM", "SYS", channel, channel.getLocalAddress(), new Usuario(localAddress, "SERVIDOR"), 1024, true, executorService));

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
            System.out.println(hash);
            System.out.println("RECEPCION DE: " + packet.getAddress() + " " + packet.getPort());
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
                topicoDestino = new Topico("", f.getCodigoTopico(), channel, channel.getLocalAddress(), new Usuario(localAddress, "SERVIDOR"), 1024, true, topicos.get("SYS"), executorService);
                topicos.put(topicoDestino.getCodigo(), topicoDestino);
            }

            topicoDestino.addFragment(f);
        }
    }

    public Servidor(InetSocketAddress localAddress){
        this.localAddress = localAddress;
        this.executorService = Executors.newFixedThreadPool(10);
        this.topicos = new HashMap<>();
    }

    public static void main(String[] args) throws IOException {
        var s = new Scanner(System.in);
        System.out.println("Ingrese el puerto a utilizar: ");
        var puerto = s.nextInt();
        Servidor server = new Servidor( new InetSocketAddress(puerto));
        server.start();
    }
}
