package client;

import common.ConnectionSettings;
import common.ServerAPI;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection implements ConnectionSettings, ServerAPI {
    private Socket socket;
    private Controller controller;
    private boolean authorized = false;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientConnection(Controller controller) {
        this.controller = controller;
    }

    public void sendMessage(String message){
        if (socket == null || socket.isClosed()) return;
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAuthorized() {
        return authorized;
    }

    private void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public void openConnection() {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true){
                        String message = in.readUTF();
                        if (message.equals(CLOSE_CONNECTION)){
                            setAuthorized(false);
                            updateControllerState();
                            break;
                        }
                        if (message.startsWith(AUTH_SUCCESSFUL)) {
                            setAuthorized(true);
                            updateControllerState();
                            serviceMessage("Добро пожаловать в чат," + message.substring(AUTH_SUCCESSFUL.length()));
                            continue;
                        }
                        if (message.startsWith(SERVICE_MESSAGE)) {
                            serviceMessage(message.substring(SERVICE_MESSAGE.length()));
                            continue;
                        }
                        if (message.startsWith(PERSONAL_MESSAGE)) {
                            userMessage(message.substring(PERSONAL_MESSAGE.length()), true);
                            continue;
                        }
                        if (message.startsWith(USERLIST)) {
                            updateUserList(message.substring(USERLIST.length() + 1));
                            continue;
                        }
                        userMessage(message, false);
                    }
                } catch (IOException e) {
                    System.err.println("Соединение с сервером прервано");
                } finally {
                    if (!socket.isClosed()) try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            serviceMessage("Ошибка соединения с сервером");
        }
    }

    public void closeConnection() {
        sendMessage(CLOSE_CONNECTION);
    }

    public void authorize(String login, String pass) {
        if (socket == null || socket.isClosed()) openConnection();
        sendMessage(AUTH_REQUEST + " " + login + " " + pass);
    }

    private void updateControllerState(){
        Platform.runLater(() -> controller.updateState());
    }

    private void updateUserList(String userList) {
        Platform.runLater(() -> controller.updateUserList(userList.split("\\s")));
    }

    private void serviceMessage(String message){
        Platform.runLater(() -> controller.serviceMessage(message));
    }

    private void userMessage(String message, boolean personal){
        if (!message.startsWith(FROM_USER)) return;
        String[] parts = message.split("\\s");
        String fromUser = parts[1];
        String toUser = personal?parts[3]:"";
        String message_text = message.substring(FROM_USER.length() + 1 + fromUser.length() + 1 + (personal?TO_USER.length() + 1 + toUser.length() + 1:0));
        Platform.runLater(() -> controller.userMessage(fromUser, toUser, message_text, personal));
    }

    public void register(String nickname, String login, String pass) {
        if (socket == null || socket.isClosed()) openConnection();
        sendMessage(AUTH_REGISTER + " " + nickname + " " + login + " " + pass);
    }
}
