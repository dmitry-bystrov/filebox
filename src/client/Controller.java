package client;

import common.ServerAPI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private DateFormat dateFormat;
    private ClientConnection clientConnection;
    private static final int SMILE_MAX_SIZE = 32;

    @FXML
    public TextFlow textFlow;
    public ScrollPane scrollPane;
    public TextField loginField;
    public PasswordField passwordField;
    public VBox loginPassBox;
    public HBox nicknameBox;
    public TextField password2Field;
    public Button loginButton;
    public Button registerButton;
    public Label registerLink;
    public BorderPane filesPane;

    public Controller() {
        dateFormat = new SimpleDateFormat("hh:mm");
    }

    public void userMessage(String fromUser, String toUser, String message, boolean personal) {
        if (message.isEmpty()) return;

        Date date = new Date();
        Text messageHeaderStart = new Text("[" + dateFormat.format(date) + "] ");
        Text messageHeaderNickname = new Text(fromUser);
        Text messageHeaderEnd = new Text((personal ? " > " + toUser : "") + ": ");

        messageHeaderStart.setId("messageHeader");
        messageHeaderEnd.setId("messageHeader");
        messageHeaderNickname.setId("userNickname");
        textFlow.getChildren().addAll(messageHeaderStart, messageHeaderNickname, messageHeaderEnd);

        String[] parts = message.split("\\s");

        textFlow.getChildren().add(new Text("\n"));
        scrollToEnd();
    }

    public void serviceMessage(String message) {
        if (message.isEmpty()) return;

        Text serviceMessageText = new Text(message + "\n");
        serviceMessageText.setId("serviceMessage");
        textFlow.getChildren().add(serviceMessageText);
        scrollToEnd();
    }

    private void login() {
        if (loginField.getText() != null && passwordField.getText() != null) {
            clientConnection.authorize(loginField.getText(), passwordField.getText());
        }
    }

    public void sendMessage(){
        if (clientConnection.isAuthorized()) {
//            clientConnection.sendMessage(textField.getText());
//            textField.clear();
//            textField.requestFocus();
        } else login();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        textFlow.maxWidthProperty().bind(scrollPane.widthProperty().subtract(40));
        loginPassBox.managedProperty().bind(loginPassBox.visibleProperty());
        nicknameBox.managedProperty().bind(nicknameBox.visibleProperty());
        registerButton.managedProperty().bind(registerButton.visibleProperty());
        loginButton.managedProperty().bind(loginButton.visibleProperty());
        registerLink.managedProperty().bind(registerLink.visibleProperty());
        filesPane.managedProperty().bind(filesPane.visibleProperty());
        clientConnection = new ClientConnection(this);
        clientConnection.openConnection();

        updateState();
    }

    public void setupStageListeners(Stage stage) {
        stage.setOnCloseRequest(e -> onClose());
    }

    private void onClose() {
        clientConnection.closeConnection();
    }

    public void updateState() {
        if (clientConnection.isAuthorized()) {
            loginPassBox.setVisible(false);
            filesPane.setVisible(true);
        } else {
            loginPassBox.setVisible(true);
            filesPane.setVisible(false);
            nicknameBox.setVisible(false);
            registerButton.setVisible(false);
            loginButton.setVisible(true);
            registerLink.setVisible(true);
        }
    }

    private void scrollToEnd() {
        new Thread(() -> {
            try {
                Thread.sleep(100);
                scrollPane.setVvalue(1.0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void registerUser(ActionEvent actionEvent) {
        if (password2Field.getText() != null && loginField.getText() != null && passwordField.getText() != null) {
            if (password2Field.getText().equals(passwordField.getText())) {
                clientConnection.register(loginField.getText(), passwordField.getText());
            } else {
                serviceMessage("Пароли не совпадают!");
            }
        }

        nicknameBox.setVisible(false);
        registerButton.setVisible(false);
        loginButton.setVisible(true);
        registerLink.setVisible(true);
    }

    public void registerSwitch(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) return;

        nicknameBox.setVisible(true);
        registerButton.setVisible(true);
        loginButton.setVisible(false);
        registerLink.setVisible(false);
    }
}
