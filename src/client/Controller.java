package client;

import common.ServerAPI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
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
    private Smiles smiles;
    private DateFormat dateFormat;
    private ClientConnection clientConnection;
    private static final int SMILE_MAX_SIZE = 32;

    @FXML
    public TextFlow textFlow;
    public TextField textField;
    public ScrollPane scrollPane;
    public TextField loginField;
    public PasswordField passwordField;
    public VBox loginPassBox;
    public BorderPane sendMessageBox;
    public TilePane smilesPane;
    public TextFlow userList;
    public HBox nicknameBox;
    public TextField nicknameField;
    public Button loginButton;
    public Button registerButton;
    public Label registerLink;

    public Controller() {
        dateFormat = new SimpleDateFormat("hh:mm");
        smiles = new Smiles();
    }

    public void userMessage(String fromUser, String toUser, String message, boolean personal) {
        if (message.isEmpty()) return;

        Date date = new Date();
        Text messageHeaderStart = new Text("["+ dateFormat.format(date) + "] ");
        Text messageHeaderNickname = new Text(fromUser);
        Text messageHeaderEnd = new Text((personal?" > " + toUser:"") + ": ");

        messageHeaderStart.setId("messageHeader");
        messageHeaderEnd.setId("messageHeader");
        messageHeaderNickname.setId("userNickname");
        messageHeaderNickname.setOnMouseClicked(this::onNicknameClicked);
        textFlow.getChildren().addAll(messageHeaderStart, messageHeaderNickname, messageHeaderEnd);

        String[] parts = message.split("\\s");
        for (String s : parts) {
            if (smiles.getSmiles().containsKey(s)){
                ImageView imageView = new ImageView(smiles.getSmiles().get(s));
                imageView.setFitWidth(SMILE_MAX_SIZE);
                imageView.setFitHeight(SMILE_MAX_SIZE);
                textFlow.getChildren().addAll(imageView, new Text(" "));
            } else {
                Text messageText = new Text(s + " ");
                if (personal) messageText.setId("personalMessage");
                textFlow.getChildren().add(messageText);
            }
        }

        textFlow.getChildren().add(new Text("\n"));
        scrollToEnd();
    }

    public void updateUserList(String[] usersOnline) {
        userList.getChildren().clear();
        Arrays.sort(usersOnline);
        for (String user : usersOnline) {
            Text userNickname = new Text(user + "\n");
            userNickname.setId("userNickname");
            userNickname.setOnMouseClicked(this::onNicknameClicked);
            userList.getChildren().add(userNickname);
        }
    }

    private void onNicknameClicked(MouseEvent event) {
        textField.setText(ServerAPI.TO_USER + " " + ((Text)event.getTarget()).getText() + " ");
        textField.requestFocus();
        textField.positionCaret(textField.getText().length());
    }

    public void serviceMessage(String message){
        if (message.isEmpty()) return;

        Text serviceMessageText = new Text(message + "\n");
        serviceMessageText.setId("serviceMessage");
        textFlow.getChildren().add(serviceMessageText);
        scrollToEnd();
    }

    private void login() {
        if (loginField.getText() != null && passwordField.getText() != null){
            clientConnection.authorize(loginField.getText(), passwordField.getText());
        }
    }

    public void sendMessage(){
        if (clientConnection.isAuthorized()) {
            clientConnection.sendMessage(textField.getText());
            textField.clear();
            textField.requestFocus();
        } else login();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        textFlow.maxWidthProperty().bind(scrollPane.widthProperty().subtract(40));
        loginPassBox.managedProperty().bind(loginPassBox.visibleProperty());
        sendMessageBox.managedProperty().bind(sendMessageBox.visibleProperty());
        smilesPane.managedProperty().bind(smilesPane.visibleProperty());
        userList.managedProperty().bind(userList.visibleProperty());
        nicknameBox.managedProperty().bind(nicknameBox.visibleProperty());
        registerButton.managedProperty().bind(registerButton.visibleProperty());
        loginButton.managedProperty().bind(loginButton.visibleProperty());
        registerLink.managedProperty().bind(registerLink.visibleProperty());

        clientConnection = new ClientConnection(this);
        clientConnection.openConnection();

        smiles.getSmiles().forEach((key, value) -> {
            ImageView imageView = new ImageView(value);
            imageView.setFitWidth(SMILE_MAX_SIZE);
            imageView.setFitHeight(SMILE_MAX_SIZE);
            imageView.setOnMouseClicked(event -> onSmileClicked(event, key));
            smilesPane.getChildren().add(imageView);
        });
        updateState();
    }

    private void onSmileClicked(MouseEvent event, String key) {
        if (textField.getText().isEmpty()) {
            textField.setText(key);
        } else {
            textField.setText(textField.getText() + " " + key);
        }

        textField.positionCaret(textField.getText().length());
    }

    public void setupStageListeners(Stage stage){
        stage.setOnCloseRequest(e -> onClose());
    }

    private void onClose() {
        clientConnection.closeConnection();
    }

    public void updateState(){
        if (clientConnection.isAuthorized()){
            sendMessageBox.setVisible(true);
            userList.setVisible(true);
            loginPassBox.setVisible(false);
        } else {
            sendMessageBox.setVisible(false);
            smilesPane.setVisible(false);
            userList.setVisible(false);
            loginPassBox.setVisible(true);

            nicknameBox.setVisible(false);
            registerButton.setVisible(false);
            loginButton.setVisible(true);
            registerLink.setVisible(true);
        }
    }

    private void scrollToEnd(){
        new Thread(() -> {
            try {
                Thread.sleep(100);
                scrollPane.setVvalue(1.0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void showSmiles(ActionEvent actionEvent) {
        if (smilesPane.isVisible()){
            smilesPane.setVisible(false);
        } else {
            smilesPane.setVisible(true);
        }
    }

    public void showUserList(ActionEvent actionEvent) {
        if (userList.isVisible()){
            userList.setVisible(false);
        } else {
            userList.setVisible(true);
        }
    }

    public void registerUser(ActionEvent actionEvent) {
        if (nicknameField.getText() != null && loginField.getText() != null && passwordField.getText() != null){
            clientConnection.register(nicknameField.getText(), loginField.getText(), passwordField.getText());
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
