package server;

import common.ConnectionSettings;
import common.ServerAPI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class Server implements ConnectionSettings, ServerAPI {
    private ServerSocket server;
    private AuthService authService;
    private Vector<ClientHandler> clients;

    public Server() {
        try {
            server = new ServerSocket(SERVER_PORT);
            authService = new BaseAuthService();
            clients = new Vector<>();
            authService.start();

            Socket socket = null;
            System.out.println("Сервер запущен и ожидает подключения");
            while (true)
            {
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket).start();
            }

        } catch (IOException e) {
            System.err.println("Ошибка при работе сервера");
            e.printStackTrace();
        }
        finally {
            if (server != null) try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (authService != null) authService.stop();
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isNicknameBusy(String nickname){
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(nickname)) return true;
        }
        return false;
    }

    public synchronized void broadcastMessage(ClientHandler fromUser, String toUser,String message){
        String messageFromUser = FROM_USER + " " + fromUser.getNickname() + " " + message;
        boolean personalMessageWasSent = false;
        for (ClientHandler client : clients) {
            if (toUser == null){
                client.sendMessage(messageFromUser);
            } else {
                if (client.getNickname().equals(toUser)) {
                    client.sendMessage(PERSONAL_MESSAGE + messageFromUser);
                    if (!fromUser.getNickname().equals(toUser)) fromUser.sendMessage(PERSONAL_MESSAGE + messageFromUser);
                    personalMessageWasSent = true;
                    break;
                }
            }
        }
        if (toUser != null && !personalMessageWasSent){
            fromUser.sendServiceMessage("Пользователя с ником " + toUser + " нет в чате.");
        }
    }

    public synchronized void broadcastServiceMessage(String message){
        for (ClientHandler client : clients) {
            client.sendServiceMessage(message);
        }
    }

    public synchronized void subscribeClient(ClientHandler client){
        clients.add(client);
        broadcastClientList();
    }

    public synchronized void unsubscribeClient(ClientHandler client){
        clients.remove(client);
        broadcastClientList();
    }

    private synchronized void broadcastClientList() {
        StringBuffer sb = new StringBuffer(USERLIST);
        for (ClientHandler client : clients) {
            sb.append(" ");
            sb.append(client.getNickname());
        }
        for (ClientHandler client : clients) {
            client.sendMessage(sb.toString());
        }
    }
}
