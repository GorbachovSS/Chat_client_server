package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import static java.time.LocalDateTime.now;


public class MainClient extends JFrame implements Runnable {
    protected JTextArea outTextArea;
    protected JPanel southPanel;
    protected JTextField inTextField;
    protected JButton inTextSendButton;
    protected boolean isOn;
    private final static String TEMPLATE_OUTPUT = "%s: %s \n";
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    Network network;

    public MainClient(String title, Network network) throws HeadlessException, InterruptedException {
        super(title);
        southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(2, 1, 10, 10));
        southPanel.add(inTextField = new JTextField());
        inTextField.setEditable(true);
        southPanel.add(inTextSendButton = new JButton("Send message"));
        inTextSendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    network.sendMeassage(inTextField.getText());
                    inTextField.setText("");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(BorderLayout.CENTER, outTextArea = new JTextArea());
        outTextArea.setEditable(false);
        cp.add(BorderLayout.SOUTH, southPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setVisible(true);
        inTextField.requestFocus();
        (new Thread(this)).start();
        this.network = network;

        this.network.setCallback(new Callback() {
            @Override
            public void call(Object... args) {
                outTextArea.append(String.format(TEMPLATE_OUTPUT, now().format(DATE_TIME_FORMATTER), args[0]));
            }
        });
    }

    public static void main(String[] args) throws Exception {
        try (Network network = new Network()) {
            network.connect(888);
            new MainClient("chat", network);
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String msg = scanner.nextLine();
                network.sendMeassage(msg);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

    }
}
