package client;

import common.ConnectionSettings;
import common.FileInfo;
import common.ServerAPI;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientConnection implements ConnectionSettings, ServerAPI {
    public static final int STREAM_BUFFER_SIZE = 1024;
    private Socket socket;
    private Controller controller;
    private boolean authorized = false;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientConnection(Controller controller) {
        this.controller = controller;
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

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());

            new Thread(() -> {
                try {
                    Object dataObject = new Object();
                    while (true) {
                        try {
                            dataObject = in.readObject();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        String message = "";
                        if (dataObject instanceof String) {
                            message = dataObject.toString();
                        }

                        if (dataObject instanceof ArrayList) {
                            controller.updateTable((ArrayList<FileInfo>) dataObject);
                        }

                        if (message.equals(CLOSE_CONNECTION)) {
                            notifyConnectionClosed();
                            break;
                        }
                        if (message.startsWith(AUTH_SUCCESSFUL)) {
                            setAuthorized(true);
                            updateControllerState();
                            serviceMessage("Добро пожаловать," + message.substring(AUTH_SUCCESSFUL.length()));
                            continue;
                        }
                        if (message.startsWith(SERVICE_MESSAGE)) {
                            serviceMessage(message.substring(SERVICE_MESSAGE.length()));
                            continue;
                        }
                    }
                } catch (Exception e) {
                    notifyConnectionClosed();
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

    private void notifyConnectionClosed() {
        setAuthorized(false);
        updateControllerState();
        serviceMessage("Соединение с сервером прервано");
    }

    public void closeConnection() {
        sendMessage(CLOSE_CONNECTION);
    }

    public void authorize(String login, String pass) {
        if (socket == null || socket.isClosed()) openConnection();
        sendMessage(AUTH_REQUEST + " " + login + " " + pass);
    }

    private void updateControllerState() {
        Platform.runLater(() -> controller.updateState());
    }

    private void serviceMessage(String message) {
        Platform.runLater(() -> controller.serviceMessage(message));
    }


    public void register(String login, String pass) {
        if (socket == null || socket.isClosed()) openConnection();
        sendMessage(AUTH_REGISTER + " " + login + " " + login + " " + pass);
    }

    public void sendMessage(String message) {
        if (socket == null || socket.isClosed()) return;
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(File file) {
        if (socket == null || socket.isClosed()) return;

        try {
            out.writeObject(new FileInfo(file.getName(), file.length(), FileInfo.Operation.PUT_FILE));
            out.flush();

            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream(), STREAM_BUFFER_SIZE);
            FileInputStream outFile = new FileInputStream(file);
            int bytesReadFromSource;
            int totalBytesCount = 0;

            while ((bytesReadFromSource = outFile.read(buffer, 0, STREAM_BUFFER_SIZE)) != -1) {
                totalBytesCount += bytesReadFromSource;
                bufferedOutputStream.write(buffer, 0, bytesReadFromSource);
                bufferedOutputStream.flush();
            }

            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
