package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Сервер запущен на порту " + port);
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastMessage("Клиент: " + clientHandler.getUsername() + " вошел в чат");
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Клиент: " + clientHandler.getUsername() + " вышел из чата");
    }

    public void sendUserMessage(String userName, String message) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUsername().equals(userName)) {
                clientHandler.sendMessage(message);
            }
        }
    }

    public boolean isFirstUser(){
        return clients.size() == 0;
    }

    public void kickUser(String userName) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage("User " + userName + " was been kicked");
            if (clientHandler.getUsername().equals(userName)) {
                clientHandler.kickUser();
            }
        }
    }
}
