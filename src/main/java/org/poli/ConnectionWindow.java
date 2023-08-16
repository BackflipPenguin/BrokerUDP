package org.poli;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ConnectionWindow {
    private JButton conectarButton;
    private JPanel ConnectionPanel;
    private JTextField servidorTextField;

    private boolean placeholderBorrado;
    public ConnectionWindow() {
        servidorTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if(!placeholderBorrado){
                    servidorTextField.setText("");
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ConnectionWindow");
        frame.setContentPane(new ConnectionWindow().ConnectionPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
