package org.poli;

import java.net.DatagramSocket;
import java.util.ArrayList;

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

}
