package gui.stats;

import gui.ExecutionRunner;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import logic.Variable.Variable;
import logic.history.RunHistoryEntry;

import java.util.Map;



public class ShowStats {

    public static void presentStatistics(){

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: whitesmoke;");

        Label title = new Label("Run History");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.DARKSLATEBLUE);

        root.getChildren().add(title);

        for (RunHistoryEntry entry : ExecutionRunner.getHistory()) {
            VBox runBox = new VBox(5);
            runBox.setPadding(new Insets(10));
            runBox.setStyle("-fx-background-color: lavender; -fx-background-radius: 10;");

            String mode = entry.isDebug() ? "Debug" : "Regular";
            Label runTitle = new Label("Run #" + entry.getRunNumber() + " (" + mode + ")");
            runTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

            Label degree = new Label("Expansion Degree: " + entry.getExpansionDegree());
            Label resultY = new Label("Result (y): " + entry.getResultY());
            Label cycles = new Label("Total Cycles: " + entry.getTotalCycles());

            VBox inputBox = new VBox();
            inputBox.getChildren().add(new Label("Inputs:"));
            for (Map.Entry<Variable, Long> input : entry.getInputs().entrySet()) {
                Label inputLabel = new Label(" " + input.getKey().getRepresentation() + " = " + input.getValue());
                inputBox.getChildren().add(inputLabel);
            }

            runBox.getChildren().addAll(runTitle, degree, inputBox, resultY, cycles);
            root.getChildren().add(runBox);
        }

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);

        Stage stage = new Stage();
        stage.setTitle("Run Statistics");
        stage.setScene(new Scene(scrollPane, 400, 500));
        stage.show();
    }

}
