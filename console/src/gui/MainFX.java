package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import programDisplay.ProgramDisplayImpl;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainScreen.fxml"));
        Parent root = fxmlLoader.load();
        MainScreenController controller = fxmlLoader.getController();

        ProgramDisplayImpl display = new ProgramDisplayImpl(controller);
        controller.setProgramDisplay(display);

        primaryStage.setTitle("S-Emulator");

        Scene scene = new Scene(root, 600, 400); // גודל התחלתי נוח
        primaryStage.setScene(scene);



        primaryStage.setResizable(true); // לא חובה (ברירת מחדל true), אבל טוב לכתוב מפורשות
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
