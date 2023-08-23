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

        t.enviar(new Mensaje(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean dapibus scelerisque lacinia. Pellentesque purus metus, tempus eget varius eget, semper vitae ex. Cras leo dui, dignissim quis diam vel, tincidunt laoreet lacus. Vestibulum nec turpis quis mi scelerisque mollis in at dolor. Fusce ut tempus enim. Phasellus neque ligula, luctus sit amet enim quis, lobortis viverra diam. Suspendisse viverra tempus sagittis. Sed tincidunt orci at augue ultrices cursus. Sed sed ipsum velit.\n" +
                "\n" +
                "Praesent mattis tristique arcu, a pellentesque dui laoreet non. Vivamus laoreet quam orci, eget consequat ligula hendrerit non. Sed a pharetra enim. Proin vel convallis enim, at dictum magna. Quisque euismod, felis sed venenatis porta, quam justo malesuada ipsum, non aliquam justo tortor quis ipsum. Duis venenatis ligula mauris, quis ultrices justo sodales at. Cras dignissim pharetra eros, at condimentum erat dapibus in. Quisque eget nisl tincidunt nunc fringilla placerat vitae at urna. Curabitur ullamcorper, purus eget iaculis tempor, libero erat fermentum ex, non bibendum ipsum mi in turpis. Nam mollis ipsum in urna semper aliquet. Vestibulum purus leo, mattis in lectus ac, eleifend convallis nulla. Donec vitae tortor purus. Vestibulum sapien ipsum, bibendum at nibh quis, ultrices aliquam velit. Phasellus sed justo et magna sodales egestas nec ut lorem. Quisque nec suscipit est.\n" +
                "\n" +
                "Donec laoreet mollis lacus. Cras ut lorem facilisis, rutrum purus vitae, mattis velit. Aenean hendrerit nisl molestie neque condimentum dignissim. Nullam lectus urna, volutpat nec ultricies vel, ultricies eget metus. Cras vitae mollis augue. Nunc volutpat egestas consectetur. In sed ipsum a arcu molestie mollis ac vel erat. Nulla et leo ut tellus lobortis commodo. Aenean egestas eros eu mollis consectetur. Sed malesuada, mi eget facilisis dapibus, eros justo convallis dui, vel viverra leo ante a quam. In hac habitasse platea dictumst.\n" +
                "\n" +
                "Aenean purus eros, condimentum ac turpis non, venenatis cursus lectus. Etiam eu mi ac mi convallis ultricies. Vestibulum risus est, luctus id sem pharetra, aliquet dictum elit. Duis ac finibus nunc. Duis pharetra dictum diam. Cras non condimentum est, non consequat dolor. Praesent vel mollis metus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam finibus diam quis scelerisque suscipit. Etiam ligula neque, gravida in quam vel, porttitor tristique ipsum. Integer sit amet lacus vitae magna tempor vestibulum. Duis porta risus sit amet mauris egestas fermentum. Maecenas eget elementum est. Donec tincidunt ac risus ut condimentum.\n" +
                "\n" +
                "Nullam ullamcorper posuere bibendum. Pellentesque a maximus ipsum, sed lacinia nisi. Praesent ornare facilisis posuere. Aliquam elementum diam eget erat egestas sollicitudin. In dictum volutpat finibus. In hac habitasse platea dictumst. Morbi sagittis velit elit, at facilisis eros tincidunt et. Suspendisse potenti. Nulla in sodales dolor. Mauris eu magna tellus. Aliquam eget molestie lectus. Vestibulum ultricies interdum ex, non tincidunt dolor feugiat et. Quisque aliquam ligula vel diam ultrices imperdiet. Sed ullamcorper vehicula faucibus. Phasellus molestie a est ac imperdiet.\n" +
                "\n" +
                "Aenean rhoncus tincidunt nibh, eget finibus ipsum. Cras maximus malesuada dolor, ac laoreet enim iaculis sit amet. Phasellus sit amet tempus dui, eget auctor urna. In posuere luctus dolor. Nullam in lacus id dolor consequat consectetur et ut neque. Proin at sem quis justo ultrices convallis tristique in justo. Vestibulum odio orci, consequat eget viverra convallis, pulvinar sit amet lorem.\n" +
                "\n" +
                "Nullam ultricies in libero quis vestibulum. Praesent ac ullamcorper erat. Nunc sit amet dolor vitae ipsum finibus egestas. Sed id sapien elementum, tincidunt tellus ut, convallis nisi. Cras vitae tincidunt sapien. In nec magna eget elit varius pulvinar accumsan in tortor. Proin consequat, erat vitae pulvinar iaculis, turpis risus rutrum ante, a cursus tortor leo vitae elit. Suspendisse sodales a quam id sollicitudin. Duis quis faucibus libero, vitae ultrices augue. Curabitur vitae orci in magna mollis consectetur eget nec diam.\n" +
                "\n" +
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla sed molestie magna, quis tristique sapien. Phasellus nibh lectus, maximus in nisi vitae, auctor ullamcorper quam. Nunc accumsan ultrices est et faucibus. Suspendisse vel risus at arcu ullamcorper ultrices sit amet eget metus. Duis ut nisl ac lacus volutpat eleifend a non lacus. Ut egestas erat sit amet malesuada accumsan. Vivamus a porta dui. Duis nec quam eget lectus ultricies pharetra sit amet ac arcu. Morbi eu gravida diam. Donec placerat magna odio, ut tristique nibh viverra et. Duis in enim vitae dolor eleifend iaculis.\n" +
                "\n" +
                "Ut sed tincidunt ante. Mauris enim augue, egestas in vulputate sit amet, vehicula in diam. Nullam nec fermentum ipsum, nec fringilla erat. Vivamus semper purus urna, ac pretium purus euismod vel. Donec est nibh, imperdiet at viverra et, mattis eget odio. Ut vehicula risus sem, sed tincidunt metus hendrerit et. Suspendisse donec. ", yo));
    }

}
