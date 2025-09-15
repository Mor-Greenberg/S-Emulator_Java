package handleExecution;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;
import logic.Variable.Variable;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.program.Program;

import java.util.*;

import static utils.Utils.showError;
import static utils.Utils.showInfo;

public class HandleExecution {
    Program program;
    Map<Variable, Long> inputs =  new HashMap<Variable, Long>();
    public HandleExecution(Program program)
    {
        this.program = program;
    }

    public Program getProgram()
    {
        return program;
    }
    public void setProgram(Program program)
    {
        this.program = program;
    }

//    public void collectInputFromUser(Program program, ExecutionContext context) {
//        if (program == null) {
//            System.out.println("No program loaded yet.");
//            return;
//        }
//
//        List<Variable> inputVars = program.getVars().stream()
//                .filter(v -> v.getType() == VariableType.INPUT)
//                .toList();
//
//        if (inputVars.isEmpty()) {
//            System.out.println("No input variables found.");
//            return;
//        }
//        Scanner sc = new Scanner(System.in);
//
//        System.out.println("The input variables of this program:");
//        for (int i = 0; i < inputVars.size(); i++) {
//            System.out.print(inputVars.get(i));
//            if (i < inputVars.size() - 1) {
//                System.out.print(", ");
//            }
//        }
//        System.out.println();
//
//        System.out.println("\nPlease insert values (comma-separated):");
//        String line = sc.nextLine();
//        String[] values = line.split(",");
//
//        for (int i = 0; i < inputVars.size(); i++) {
//            long value = 0;
//            if (i < values.length) {
//                try {
//                    value = Integer.parseInt(values[i].trim());
//                } catch (NumberFormatException e) {
//                    System.out.println("Invalid input at position " + (i + 1) + ", using 0.");
//                }
//            }
//            context.updateVariable(inputVars.get(i), value);
//            inputs.put(inputVars.get(i), value);
//        }
//
//        System.out.println("Input values updated successfully.");
//    }
public void collectInputFromUserFX(Program program, ExecutionContext context) {
    if (program == null) {
        System.out.println("No program loaded yet.");
        return;
    }

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

    dialog.showAndWait();  // ← זו השורה ש"חוסמת" עד לסיום ההזנה
}






    public Map<Variable,Long> getInputsMap(){
        return inputs;
    }


}
