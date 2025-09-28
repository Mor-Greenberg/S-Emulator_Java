package handleExecution;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;
import logic.Variable.Variable;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.program.Program;

import java.util.*;

public class HandleExecution {
    Program program;
    Map<Variable, Long> inputs =  new HashMap<Variable, Long>();
    public HandleExecution(Program program)
    {
        this.program = program;
    }



public void collectInputFromUserFX(Program program, ExecutionContext context) {


    List<Variable> inputVars = program.getVars().stream()
            .filter(v -> v.getType() == VariableType.INPUT)
            .toList();

    if (inputVars.isEmpty()) {
        System.out.println("No input variables found.");
        return;
    }

    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Input Variables");
    dialog.setHeaderText("Insert values for the input variables:");
    dialog.initModality(Modality.APPLICATION_MODAL);

    ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

    VBox vbox = new VBox(10);
    vbox.setPadding(new Insets(10));
    Map<Variable, TextField> fieldMap = new HashMap<>();
    Label errorLabel = new Label();
    errorLabel.setStyle("-fx-text-fill: red;");

    for (Variable var : inputVars) {
        HBox hbox = new HBox(10);
        Label label = new Label(var.getRepresentation() + ":");
        TextField inputField = new TextField();
        inputField.setPromptText("0");
        Label helpLabel = new Label("* Only positive integers are allowed");
        helpLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        VBox fieldBox = new VBox(2, hbox, helpLabel);
        vbox.getChildren().add(fieldBox);

        hbox.getChildren().addAll(label, inputField);
        fieldMap.put(var, inputField);
    }

    vbox.getChildren().add(errorLabel);
    dialog.getDialogPane().setContent(vbox);

    Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
    okButton.addEventFilter(ActionEvent.ACTION, event -> {
        boolean hasError = false;
        StringBuilder errorMessages = new StringBuilder();

        for (Map.Entry<Variable, TextField> entry : fieldMap.entrySet()) {
            Variable var = entry.getKey();
            String text = entry.getValue().getText().trim();

            try {
                long value = Long.parseLong(text);
                if (value < 0)
                    throw new NumberFormatException();
                context.updateVariable(var, value);
                inputs.put(var, value);
            } catch (NumberFormatException e) {
                hasError = true;
                context.updateVariable(var, 0);
                inputs.put(var, 0L);
                errorMessages.append("Invalid input for ")
                        .append(var.getRepresentation())
                        .append(", ")
                        .append(var.getRepresentation())
                        .append("=0\n");
            }
        }

        if (hasError) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText(null);
            alert.setContentText(errorMessages.toString().trim());

            alert.showAndWait();


        }

        else {

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Input values received successfully");
            successAlert.show();

            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(e -> successAlert.close());
            delay.play();

        }
    });

    dialog.showAndWait();
}


    public Map<Variable,Long> getInputsMap(){
        return inputs;
    }



}
