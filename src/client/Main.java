package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static final int START_WIDTH = 830;
    public static final int START_HEIGHT = 600;
    public static final String WINDOW_TITLE = "Chat client";

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
        Parent root = loader.load();
        ((Controller)loader.getController()).setupStageListeners(primaryStage);

        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(new Scene(root, START_WIDTH, START_HEIGHT));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/client/assets/icon.png")));
        primaryStage.getScene().getStylesheets().add(new Styles().getRandomStyle());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
