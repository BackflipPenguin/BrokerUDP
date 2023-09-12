package org.poli;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;

public class TopicoSYS extends Topico{
    private String handleCommand(String[] sections, Mensaje mensaje, Fragmento fragmento) throws IOException {
        switch (sections[1]) {
            case ("SUB") -> {
                getExecutorService().submit(() -> suscribir(mensaje.getCreador()));
                return "SUB";
            }
            case ("ACK") -> {
                if (sections.length != 4)
                    enviarSYS("\\ERR\\" + mensaje.getUuid() + "\\ARG", fragmento.getCreador().getDireccion());
                try {
                    getMensajes().get(sections[2]).ackFragment(Integer.parseInt(sections[3]));
                } catch (NullPointerException e){
                    enviarSYS("\\ERR\\" + mensaje.getUuid() + "\\NOMSG", fragmento.getCreador().getDireccion());
                }
                return "ACK";
            }
            case ("REG") -> {

            }
        }
        return null;
    }
    public TopicoSYS(String nombre, String codigo, DatagramChannel canal, SocketAddress serverAddr, Usuario usuario, int tamanoDatagrama, boolean server, ExecutorService executorService) {
        super(nombre, codigo, canal, serverAddr, usuario, tamanoDatagrama, server, executorService);
    }

    public TopicoSYS(String nombre, String codigo, DatagramChannel canal, SocketAddress serverAddr, Usuario usuario, int tamanoDatagrama, boolean server, Topico topicoSYS, ExecutorService executorService) {
        super(nombre, codigo, canal, serverAddr, usuario, tamanoDatagrama, server, topicoSYS, executorService);
    }
}
