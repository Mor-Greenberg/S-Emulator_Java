package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class MainTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        File fxmlFile = new File("client/src/ui/Dashboard/S-Emulator-Dashboard.fxml");
        loader.setLocation(fxmlFile.toURI().toURL());

        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("S-Emulator Dashboard");
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
