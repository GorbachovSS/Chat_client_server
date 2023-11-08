package org.example;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Closeable {
    private boolean isAdmin;
    private boolean isAuthorized;
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private final DbService dbService;
    private final String url = "jdbc:postgresql://localhost/chat?user=postgres&password=sa";

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
        dbService = new DbService(url);
        dbService.connect();

        if (server.isFirstUser()) {
            this.isAdmin = true;
        }

        server.subscribe(this);
        new Thread(() -> {
            try {
                while (true) {
                    // /exit -> disconnect()
                    // /w user message -> user

                    String message = in.readUTF();
                    if (isAuthorized) {

                        if (message.startsWith("/")) {

                            if (message.contains("/reg")) {
                                String oldName = this.username;

                                if (dbService.updateLogin(oldName, message.replace("/reg", "").trim())) {
                                    this.username = message.replace("/reg", "").trim();
                                    server.broadcastMessage("Пользователь " + oldName + " сменил имя на " + this.username);
                                } else {
                                    server.broadcastMessage("Не удалось сменить имя!");
                                }
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
                                } else {
                                    server.sendUserMessage(username, "Ты не админ команда не доступна");
                                }
                            } else if (message.equals("/exit")) {
                                break;
                            }
                        } else {
                            server.broadcastMessage(message);
                        }
                    } else {
                        if (message.contains("/auth")) {
                            String[] msgArray = message.split(" ");
                            String userName = msgArray[1];
                            String passwd = msgArray[2];

                            isAuthorized = dbService.checkUserInDb(userName, passwd);

                            if (isAuthorized) {
                                this.username = userName;
                                server.sendUserMessage(username, "Вы успешно авторизованы в системе!");
                            } else {
                                server.sendUserMessage(username, "Неверный логин или пароль!");
                            }

                        } else {
                            server.sendUserMessage(username, "Авторизуйтесь в системе!");
                        }
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
