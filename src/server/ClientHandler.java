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

        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startHandlerThread() {
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
                        if (message.equals(CLOSE_CONNECTION)) break;
                        if (handleAuthorizationRequest(message)) break;
                    }
                }

                while (true) {
                    if (nickname.equals(UNAUTHORIZED)) break;

                    try {
                        dataObject = in.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (dataObject instanceof String) {
                        String message = dataObject.toString();
                        if (message.equals(CLOSE_CONNECTION)) break;
                    }

                    if (dataObject instanceof FileInfo) {
                        FileInfo fileInfo = (FileInfo) dataObject;

                        switch (fileInfo.getOperation()) {
                            case PUT_FILE:
                                handlePutFileRequest(fileInfo);
                                break;
                            case GET_FILE:
                                handleGetFileRequest(fileInfo);
                                break;
                            case DELETE_FILE:
                                handleDeleteFileRequest(fileInfo);
                                break;
                            case RENAME_FILE:
                                handleRenameFileRequest(fileInfo);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Работа обработчика сообщений прервана");
            } finally {
                server.unsubscribeClient(this);
                if (!socket.isClosed()) sendMessage(CLOSE_CONNECTION);
                try {
                    if (!socket.isClosed()) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Клиент отключился");
            }
        }).start();
    }

    private void handlePutFileRequest(FileInfo fileInfo) throws IOException {
        File file = new File(String.format("%s\\%s", directory.getPath(), fileInfo.getFileName()));
        if (file.exists()) {
            sendServiceMessage(String.format("Файл с именем %s уже есть на сервере", fileInfo.getFileName()));
            return;
        }

        out.writeObject(fileInfo);
        out.flush();

        byte[] buffer = new byte[STREAM_BUFFER_SIZE];
        int bytesReadFromSource;
        int totalBytesCount = 0;
        FileOutputStream inFile = new FileOutputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream(), STREAM_BUFFER_SIZE);

        while ((bytesReadFromSource = bufferedInputStream.read(buffer, 0, STREAM_BUFFER_SIZE)) != -1) {
            inFile.write(buffer, 0, bytesReadFromSource);
            totalBytesCount += bytesReadFromSource;

            if (totalBytesCount == fileInfo.getFileSize()) {
                break;
            }
        }

        inFile.close();
        sendServiceMessage(String.format("Файл %s загружен на сервер", fileInfo.getFileName()));
        sendFileList();
    }

    private void handleGetFileRequest(FileInfo fileInfo) {
        File file = new File(String.format("%s\\%s", directory.getPath(), fileInfo.getFileName()));

        if (file.exists()) {
            try {
                out.writeObject(fileInfo);
                out.flush();

                byte[] buffer = new byte[STREAM_BUFFER_SIZE];
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream(), STREAM_BUFFER_SIZE);
                FileInputStream outFile = new FileInputStream(file);
                int bytesReadFromSource;
                while ((bytesReadFromSource = outFile.read(buffer, 0, STREAM_BUFFER_SIZE)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesReadFromSource);
                    bufferedOutputStream.flush();
                }

                outFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendServiceMessage(String.format("Файл %s не найден на сервере", fileInfo.getFileName()));
        }
    }

    private void handleDeleteFileRequest(FileInfo fileInfo) {
        File file = new File(String.format("%s\\%s", directory.getPath(), fileInfo.getFileName()));
        if (file.exists()) {
            if (file.delete()) {
                sendServiceMessage(String.format("Файл %s удалён", fileInfo.getFileName()));
                sendFileList();
            } else {
                sendServiceMessage(String.format("Файл %s удалить не удалось", fileInfo.getFileName()));
            }
        } else {
            sendServiceMessage(String.format("Файл %s не найден на сервере", fileInfo.getFileName()));
        }
    }

    private void handleRenameFileRequest(FileInfo fileInfo) {
        File oldFile = new File(String.format("%s\\%s", directory.getPath(), fileInfo.getFileName()));
        File newFile = new File(String.format("%s\\%s", directory.getPath(), fileInfo.getNewFileName()));
        if (oldFile.exists()) {

            if (newFile.exists()) {
                sendServiceMessage(String.format("Файл с именем %s уже есть на сервере", fileInfo.getNewFileName()));
                return;
            }

            if (oldFile.renameTo(newFile)) {
                sendServiceMessage(String.format("Файл %s переименован в файл %s", fileInfo.getFileName(), fileInfo.getNewFileName()));
                sendFileList();
            } else {
                sendServiceMessage(String.format("Файл %s переименовать не удалось", fileInfo.getFileName()));
            }
        } else {
            sendServiceMessage(String.format("Файл %s не найден на сервере", fileInfo.getFileName()));
        }
    }

    private boolean handleAuthorizationRequest(String message) {
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
                        return true;
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
        return false;
    }

    private void sendFileList() {
        try {
            File[] fileList = directory.listFiles();
            List<FileInfo> fileInfoList = new ArrayList<>();
            for (int i = 0; i < fileList.length; i++) {
                fileInfoList.add(new FileInfo(fileList[i].getName(), fileList[i].length(), FileInfo.Operation.LIST_FILES));
            }
            out.writeObject(fileInfoList);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            System.out.println(String.format("out.writeObject(%s);", message));
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
