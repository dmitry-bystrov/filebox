package client;

import common.ConnectionSettings;
import common.FileInfo;
import common.ServerAPI;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection implements ConnectionSettings, ServerAPI {

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

                        if (dataObject instanceof String) {
                            String message = dataObject.toString();
                            if (message.equals(CLOSE_CONNECTION)) {
                                notifyConnectionClosed();
                                break;
                            }

                            if (message.startsWith(AUTH_SUCCESSFUL)) {
                                setAuthorized(true);
                                updateControllerState();
                                serviceMessage("Добро пожаловать," + message.substring(AUTH_SUCCESSFUL.length()));
                            }

                            if (message.startsWith(SERVICE_MESSAGE)) {
                                serviceMessage(message.substring(SERVICE_MESSAGE.length()));
                            }
                        }

                        if (dataObject instanceof ArrayList) {
                            controller.updateTable((ArrayList<FileInfo>) dataObject);
                        }

                        if (dataObject instanceof FileInfo) {
                            FileInfo fileInfo = (FileInfo) dataObject;
                            controller.disableFilesPane(true);

                            switch (fileInfo.getOperation()) {
                                case PUT_FILE:
                                    handlePutFileRequest(fileInfo);
                                    break;
                                case GET_FILE:
                                    handleGetFileRequest(fileInfo);
                                    break;
                            }

                            controller.disableFilesPane(false);
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

    public void closeConnection() {
        sendMessage(CLOSE_CONNECTION);
    }

    private void notifyConnectionClosed() {
        setAuthorized(false);
        updateControllerState();
        serviceMessage("Соединение с сервером прервано");
    }

    public void authorize(String login, String pass) {
        if (socket == null || socket.isClosed()) openConnection();
        sendMessage(AUTH_REQUEST + " " + login + " " + pass);
    }

    public void register(String login, String pass) {
        if (socket == null || socket.isClosed()) openConnection();
        sendMessage(AUTH_REGISTER + " " + login + " " + login + " " + pass);
    }

    private void updateControllerState() {
        Platform.runLater(() -> controller.updateState());
    }

    private void serviceMessage(String message) {
        Platform.runLater(() -> controller.serviceMessage(message));
    }

    public void handleGetFileRequest(FileInfo fileInfo) {
        try {
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int bytesReadFromSource;
            int totalBytesCount = 0;
            FileOutputStream inFile = new FileOutputStream(fileInfo.getSelectedFile());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream(), STREAM_BUFFER_SIZE);

            while ((bytesReadFromSource = bufferedInputStream.read(buffer, 0, STREAM_BUFFER_SIZE)) != -1) {
                inFile.write(buffer, 0, bytesReadFromSource);
                totalBytesCount += bytesReadFromSource;
                controller.updateProgressBar(totalBytesCount, fileInfo.getFileSize());

                if (totalBytesCount == fileInfo.getFileSize()) {
                    controller.updateProgressBar(0, 0);
                    break;
                }
            }

            serviceMessage(String.format("Файл %s получен с сервера", fileInfo.getFileName()));
            inFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handlePutFileRequest(FileInfo fileInfo) {
        try {
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream(), STREAM_BUFFER_SIZE);
            FileInputStream outFile = new FileInputStream(fileInfo.getSelectedFile());
            int bytesReadFromSource;
            int totalBytesCount = 0;

            while ((bytesReadFromSource = outFile.read(buffer, 0, STREAM_BUFFER_SIZE)) != -1) {
                totalBytesCount += bytesReadFromSource;
                bufferedOutputStream.write(buffer, 0, bytesReadFromSource);
                bufferedOutputStream.flush();

                controller.updateProgressBar(totalBytesCount, fileInfo.getSelectedFile().length());
            }

            controller.updateProgressBar(0, 0);
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFileInfo(FileInfo fileInfo) {
        if (socket == null || socket.isClosed()) return;

        try {
            out.writeObject(fileInfo);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
