package server;

import common.ConnectionSettings;
import common.ServerAPI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements ServerAPI, ConnectionSettings {

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.nickname = UNAUTHORIZED;
    }

    public void start(){

        new Thread(() -> {
            try {
                Thread.sleep(LOGIN_WAITING_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (nickname.equals(UNAUTHORIZED) && !socket.isClosed()) try {
                socket.close();
                System.out.println("Время ожидания авторизации истекло");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true){ // цикл авторизации
                        String message = in.readUTF();
                        if (message.equals(CLOSE_CONNECTION)) break;
                        if (message.startsWith(AUTH_REQUEST)){
                            String[] loginPass = message.split("\\s");
                            if (loginPass.length == 3){
                                String result = server.getAuthService().getNicknameByLoginPass(loginPass[1], loginPass[2]);
                                if (result != null)
                                {
                                    if (!server.isNicknameBusy(result)){
                                        nickname = result;
                                        sendMessage(AUTH_SUCCESSFUL + " " + nickname);
                                        server.broadcastServiceMessage(nickname + " зашел в чат");
                                        server.subscribeClient(this);
                                        break;
                                    } else {
                                        sendServiceMessage("Учетная запись уже используется");
                                    }
                                } else {
                                    sendServiceMessage("Неверный логин или пароль");
                                }
                            } else {
                                sendServiceMessage("Неверные параметры авторизации");
                            }
                        }
                        if (message.startsWith(AUTH_REGISTER)){
                            String[] loginPass = message.split("\\s");
                            if (loginPass.length == 4) {
                                if (server.getAuthService().setNicknameByLoginPass(loginPass[1], loginPass[2], loginPass[3])) {
                                    sendServiceMessage("Учетная запись успешно зарегистрирована");
                                } else {
                                    sendServiceMessage("Не удалось зарегистрировать учетную запись\nВозможно указанные имя пользователя или логин уже используются");
                                }
                            } else {
                                sendServiceMessage("Неверные параметры авторизации");
                            }
                        }
                    }

                    while (true){ // цикл получения сообщений
                        if (nickname.equals(UNAUTHORIZED)) break;
                        String message = in.readUTF();
                        if (message.equals(CLOSE_CONNECTION)) break;
                        String toUser = null;
                        if (message.startsWith(TO_USER)){
                            String[] parts = message.split("\\s");
                            toUser = parts[1];
                        }
                        server.broadcastMessage(this, toUser, message);
                    }
                } catch (IOException e) {
                    System.err.println("Работа обработчика сообщений прервана");
                } finally {
                    server.unsubscribeClient(this);
                    if (!socket.isClosed()) sendMessage(CLOSE_CONNECTION);
                    if (!nickname.equals(UNAUTHORIZED)) server.broadcastServiceMessage(nickname + " вышел из чата");
                    try {
                        if (!socket.isClosed()) socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Клиент отключился");
                }
            }).start();

        } catch (IOException e) {
            System.err.println("Ошибка при создании обработчика клиента");
            e.printStackTrace();
        }
    }

    public void sendMessage(String message){
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendServiceMessage(String message) {
        sendMessage(SERVICE_MESSAGE + message);
    }

    public String getNickname() {
        return nickname;
    }
}
