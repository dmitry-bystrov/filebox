package client;

import common.FileInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private ClientConnection clientConnection;
    private final FileChooser fileChooser;
    private Stage stage;

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
    public ProgressBar progressBar;
    public TableView<FileProperties> tableView;

    private final ObservableList<FileProperties> tableData = FXCollections.observableArrayList();

    public static class FileProperties {

        private final SimpleStringProperty fileName;
        private final SimpleStringProperty fileSize;

        public FileProperties(String fileName, long fileSize) {
            this.fileName = new SimpleStringProperty(fileName);
            this.fileSize = new SimpleStringProperty(String.valueOf(fileSize));
        }

        public String getFileName() {
            return fileName.get();
        }

        public SimpleStringProperty fileNameProperty() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName.set(fileName);
        }

        public long getFileSize() {
            return Long.valueOf(fileSize.get());
        }

        public SimpleStringProperty fileSizeProperty() {
            return fileSize;
        }

        public void setFileSize(String fileSize) {
            this.fileSize.set(fileSize);
        }

    }

    public Controller() {
        fileChooser = new FileChooser();
        clientConnection = new ClientConnection(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.setProgress(0);
        updateState();
    }

    public void uploadFile(MouseEvent mouseEvent) {
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;

        FileInfo fileInfo = new FileInfo(file.getName(), file.length(), FileInfo.Operation.PUT_FILE);
        fileInfo.setSelectedFile(file);
        clientConnection.sendFileInfo(fileInfo);
    }

    public void deleteFile(MouseEvent mouseEvent) {
        FileProperties fileProperties = tableView.getSelectionModel().getSelectedItem();
        if (fileProperties == null) return;

        FileInfo fileInfo = new FileInfo(fileProperties.getFileName(), fileProperties.getFileSize(), FileInfo.Operation.DELETE_FILE);
        clientConnection.sendFileInfo(fileInfo);
    }

    public void downloadFile(MouseEvent mouseEvent) {
        FileProperties fileProperties = tableView.getSelectionModel().getSelectedItem();
        if (fileProperties == null) return;

        fileChooser.setInitialFileName(fileProperties.getFileName());
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile == null) return;

        FileInfo fileInfo = new FileInfo(fileProperties.getFileName(), fileProperties.getFileSize(), FileInfo.Operation.GET_FILE);
        fileInfo.setSelectedFile(selectedFile);
        clientConnection.sendFileInfo(fileInfo);
    }

    public void disableFilesPane(boolean disable) {
        filesPane.setDisable(disable);
    }

    public void updateTable(List<FileInfo> fileInfoList) {
        tableData.clear();
        for (int i = 0; i < fileInfoList.size(); i++) {
            tableData.add(new FileProperties(fileInfoList.get(i).getFileName(), fileInfoList.get(i).getFileSize()));
        }
    }

    public void updateProgressBar(double progress, double total) {
        progressBar.setProgress(progress / total);
    }

    public void login() {
        if (loginField.getText() != null && passwordField.getText() != null) {
            clientConnection.authorize(loginField.getText(), passwordField.getText());
        }
    }

    public void setupStage(Stage stage) {
        this.stage = stage;
        setupTableView();
        bindProperties(stage);
        stage.setOnCloseRequest(e -> onClose());
    }

    private void setupTableView() {
        TableColumn<FileProperties, String> tableColumnFileName;
        TableColumn<FileProperties, String> tableColumnFileSize;

        tableColumnFileName = new TableColumn<>("Имя файла");
        tableColumnFileName.getStyleClass().add("tableColumnFileName");
        tableColumnFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        tableColumnFileName.setCellFactory(TextFieldTableCell.forTableColumn());
        tableColumnFileName.setOnEditCommit(event -> {
            FileProperties fileProperties = event.getTableView().getItems().get(event.getTablePosition().getRow());
            FileInfo fileInfo = new FileInfo(fileProperties.getFileName(), fileProperties.getFileSize(), FileInfo.Operation.RENAME_FILE);
            fileInfo.setNewFileName(event.getNewValue());
            clientConnection.sendFileInfo(fileInfo);
        });

        tableColumnFileSize = new TableColumn<>("Размер файла");
        tableColumnFileSize.getStyleClass().add("tableColumnFileSize");
        tableColumnFileSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));

        tableView.setItems(tableData);
        tableView.setEditable(true);
        tableView.getColumns().add(tableColumnFileName);
        tableView.getColumns().add(tableColumnFileSize);
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

    public void serviceMessage(String message) {
        if (message.isEmpty()) return;

        Text serviceMessageText = new Text(message + "\n");
        serviceMessageText.setId("serviceMessage");
        textFlow.getChildren().add(serviceMessageText);
        scrollToEnd();
    }

    private void bindProperties(Stage stage) {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        progressBar.prefWidthProperty().bind(stage.widthProperty());

        loginPassBox.managedProperty().bind(loginPassBox.visibleProperty());
        nicknameBox.managedProperty().bind(nicknameBox.visibleProperty());
        registerButton.managedProperty().bind(registerButton.visibleProperty());
        loginButton.managedProperty().bind(loginButton.visibleProperty());
        registerLink.managedProperty().bind(registerLink.visibleProperty());
        filesPane.managedProperty().bind(filesPane.visibleProperty());
    }
}
