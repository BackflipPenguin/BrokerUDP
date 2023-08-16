package org.poli;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Cliente {
    private DatagramSocket socket;

    private ArrayList<Topico> canales;

    public Cliente(DatagramSocket socket, ArrayList<Topico> canales) {
        this.socket = socket;
        this.canales = canales;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public ArrayList<Topico> getCanales() {
        return canales;
    }

    public void setCanales(ArrayList<Topico> canales) {
        this.canales = canales;
    }

    public static void main(String[] args) throws IOException {
        var channel = DatagramChannel.open();
        var yo = new Usuario(InetAddress.getLocalHost(), "USUARIO");
        channel.bind(null);
        channel.configureBlocking(false);
        var t = new Topico("Test","t",
                new ArrayList<>(),
                channel, Pattern.compile(""),
                new InetSocketAddress(InetAddress.getLocalHost(), 12345), yo, 1024);

        t.enviar(new Mensaje("ashe", yo));
    }

}
