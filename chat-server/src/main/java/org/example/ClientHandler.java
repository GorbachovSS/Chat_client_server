package org.example;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Closeable {
    private boolean isAdmin;
    private Socket socket;

    private Server server;
    private DataInputStream in;
    private DataOutputStream out;

    private String username;

    private static int userCount = 0;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        username = "User" + userCount++;

        if (server.isFirstUser()){
            this.isAdmin = true;
        }

        server.subscribe(this);
        new Thread(() -> {
            try {
                while (true) {
                    // /exit -> disconnect()
                    // /w user message -> user

                    String message = in.readUTF();


                    if (message.startsWith("/")) {

                        if (message.contains("/reg")) {
                            String oldName = this.username;
                            this.username = message.replace("/reg", "").trim();
                            server.broadcastMessage("Пользователь " + oldName + " сменил имя на " + this.username);
                        } else if (message.contains("/w")) {
                            String[] msgArray = message.split(" ");
                            String nameUser = msgArray[1];
                            String messageUser = message.replace("/w", "").replace(nameUser, "").trim();

                            server.sendUserMessage(nameUser, messageUser);
                        } else if (message.contains("/kick")) {
                            if (isAdmin) {
                                String oldName = this.username;
                                String[] msgArray = message.split(" ");
                                String nameUser = msgArray[1];
                                server.kickUser(nameUser);
                            }else {
                               server.sendUserMessage(username, "Ты не админ команда не доступна");
                            }
                        }else if (message.equals("/exit")) {
                            break;
                        }
                    } else {
                        server.broadcastMessage(message);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }
        }).start();
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }

    @Override
    public void close() throws IOException {

    }

    public void kickUser() {
        try {
            socket.close();
        } catch (Exception exception) {
            System.out.println(exception.getCause());
        }
    }
}
