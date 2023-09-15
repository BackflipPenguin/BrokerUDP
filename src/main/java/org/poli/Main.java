package org.poli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException, SignatureException, NoSuchAlgorithmException {
        try {
            switch (args[0].toLowerCase()){
                case ("server") -> {
                    Servidor server = new Servidor( new InetSocketAddress(Integer.parseInt(args[1])));
                    server.start();
                }
                case ("client") -> {
                    var channel = DatagramChannel.open();
                    var executorService = Executors.newFixedThreadPool(10);
                    var s = new Scanner(System.in);
                    channel.bind(null);

                    //channel.configureBlocking(true);

                    System.out.println("Ingrese la direccion de destino:");
                    var addr = s.nextLine();
                    System.out.println("Ingrese el puerto de destino:");
                    var puerto = s.nextLine();
                    System.out.println("Ingrese su nombre de usuario:");
                    var nombre = s.nextLine();
                    var c = new Cliente(channel, nombre, new InetSocketAddress(addr,Integer.parseInt(puerto)), executorService);
                    c.start();
                }
            }

        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Cantidad de argumentos incorrecta.");
        }
    }
}
