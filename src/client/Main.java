package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static final int START_WIDTH = 800;
    public static final int START_HEIGHT = 600;
    public static final String WINDOW_TITLE = "FileBox - simple network storage";

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
        Parent root = loader.load();
        ((Controller)loader.getController()).setupStage(primaryStage);

        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(new Scene(root, START_WIDTH, START_HEIGHT));
        primaryStage.getScene().getStylesheets().add((getClass().getResource("/client/css/style.css")).toExternalForm());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
