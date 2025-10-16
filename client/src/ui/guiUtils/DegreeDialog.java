package ui.guiUtils;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import logic.execution.ExecutionContext;
import logic.program.Program;
import utils.Utils;

import java.util.Optional;

public class DegreeDialog
{
    public static int askForDegree(ExecutionContext context, Program program) {
        int maxDegree = Utils.computeProgramDegree(program, context);

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Choose Expansion Degree");
        dialog.setHeaderText("Select a degree between 0 and " + maxDegree);

        // OK button
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Spinner for degree selection
        Spinner<Integer> degreeSpinner = new Spinner<>(0, maxDegree, 0);
        degreeSpinner.setEditable(true);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().add(degreeSpinner);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return degreeSpinner.getValue();
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();
        return result.orElse(-1);
    }
}
