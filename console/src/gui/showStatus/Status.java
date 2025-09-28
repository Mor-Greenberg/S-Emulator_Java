package gui.showStatus;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import logic.Variable.Variable;

import java.util.Comparator;
import java.util.Map;

public class Status {
    public static void animateStatusButton(Button showStatusButton, Map<Variable, Long> allVariables) {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), showStatusButton);
        fade.setFromValue(1.0);
        fade.setToValue(0.2);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.3), showStatusButton);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);

        ParallelTransition pt = new ParallelTransition(fade, scale);

        pt.setOnFinished(e -> {
            Platform.runLater(() -> showVariablesPopup(allVariables));
        });

        pt.play();
    }


    public static void showVariablesPopup(Map<Variable, Long> allVariables) {
        if (allVariables == null || allVariables.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Variables");
            alert.setHeaderText("No variables to display");
            alert.setContentText("Please run the program first.");
            alert.showAndWait();
            return;
        }

        StringBuilder content = new StringBuilder();

        allVariables.entrySet().stream()
                .sorted(Comparator.comparing(e -> {
                    String name = e.getKey().getRepresentation();
                    if (name.equals("y")) return "0";
                    if (name.startsWith("x")) return "1" + name.substring(1);
                    if (name.startsWith("z")) return "2" + name.substring(1);
                    return name;
                }))
                .forEach(entry -> {
                    String name = entry.getKey().getRepresentation();
                    Long value = entry.getValue();
                    content.append(name).append(" = ").append(value).append("\n");
                });

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("All Variables");
        alert.setHeaderText("Variables After Execution");
        alert.setContentText(content.toString());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}
