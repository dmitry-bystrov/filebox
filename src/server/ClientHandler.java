package server;

import common.ConnectionSettings;
import common.FileInfo;
import common.ServerAPI;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements ServerAPI, ConnectionSettings {

    private Server server;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nickname;
    private File directory;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.nickname = UNAUTHORIZED;
    }

    public void start() {

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
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

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

                        if (message.equals(CLOSE_CONNECTION)) break;
                        if (message.startsWith(AUTH_REQUEST)) {
                            String[] loginPass = message.split("\\s");
                            if (loginPass.length == 3) {
                                String result = server.getAuthService().getNicknameByLoginPass(loginPass[1], loginPass[2]);
                                if (result != null) {
                                    if (!server.isNicknameBusy(result)) {
                                        nickname = result;

                                        directory = new File(nickname);
                                        if (!directory.exists()) {
                                            if (!directory.mkdir()) {
                                                System.err.println("Не удалось создать директорию для пользователя " + nickname);
                                            }
                                        }

                                        sendMessage(AUTH_SUCCESSFUL + " " + nickname);
                                        server.subscribeClient(this);
                                        sendFileList();
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
                        if (message.startsWith(AUTH_REGISTER)) {
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

                    while (true) { // цикл получения сообщений
                        if (nickname.equals(UNAUTHORIZED)) break;
                        try {
                            dataObject = in.readObject();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        String message = "";
                        if (dataObject instanceof String) {
                            message = dataObject.toString();
                        }

                        if (message.equals(CLOSE_CONNECTION)) break;
                        String toUser = null;
                        if (message.startsWith(TO_USER)) {
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

    private void sendFileList() {
        try {
            File[] fileList = directory.listFiles();
            List<FileInfo> fileInfoList = new ArrayList<>();
            for (int i = 0; i < fileList.length; i++) {
                System.out.println(String.format("%s - %d", fileList[i].getName(), (int) fileList[i].length()));
                fileInfoList.add(new FileInfo(fileList[i].getName(), fileList[i].length()));
            }
            out.writeObject(fileInfoList);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeObject(message);
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
