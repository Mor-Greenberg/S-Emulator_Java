package printExpand.expansion;

import gui.instructionTable.InstructionRow;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import logic.Variable.Variable;

import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.instruction.AbstractInstruction;
import logic.program.Program;
import programDisplay.ProgramDisplayImpl;
import utils.Utils;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Expand {
//    public static void expandAction (Program loadedProgram, ProgramDisplayImpl display) {
//        int maxDegree = loadedProgram.calculateMaxDegree();
//        List<Integer> choices = IntStream.rangeClosed(0, maxDegree)
//                .boxed()
//                .collect(Collectors.toList());
//
//        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(0, choices);
//        dialog.setTitle("Choose Expansion Degree");
//        dialog.setHeaderText("Select the expansion degree (0–"+maxDegree +")");
//        dialog.setContentText("Degree:");
//
//        Optional<Integer> result = dialog.showAndWait();
//        if(result.isEmpty())return;
//
//        int chosenDegree = result.get();
//
//
//
//        Map<Variable, Long> variableState = loadedProgram.getVars().stream()
//                .collect(Collectors.toMap(v -> v, v -> 0L));
//
//        ExecutionContext context = new ExecutionContextImpl(variableState, loadedProgram.getFunctionMap());
//
//        loadedProgram.expandToDegree(chosenDegree,context);
//
//        showExpandedProgramPopup(loadedProgram);
//    }
public static void expandAction(Program loadedProgram, ProgramDisplayImpl display) {
    // קודם מכינים Context ריק עם פונקציות
    Map<Variable, Long> variableState = loadedProgram.getVars().stream()
            .collect(Collectors.toMap(v -> v, v -> 0L));
    ExecutionContext context = new ExecutionContextImpl(variableState, loadedProgram.getFunctionMap());

    // עכשיו מחשבים דרגה מקסימלית עם רקורסיה אמיתית
    int maxDegree = Utils.computeProgramDegree(loadedProgram, context);

    List<Integer> choices = IntStream.rangeClosed(0, maxDegree)
            .boxed()
            .collect(Collectors.toList());

    ChoiceDialog<Integer> dialog = new ChoiceDialog<>(0, choices);
    dialog.setTitle("Choose Expansion Degree");
    dialog.setHeaderText("Select the expansion degree (0–" + maxDegree + ")");
    dialog.setContentText("Degree:");

    Optional<Integer> result = dialog.showAndWait();
    if (result.isEmpty()) return;

    int chosenDegree = result.get();

    // הרחבה בפועל
    loadedProgram.expandToDegree(chosenDegree, context);

    showExpandedProgramPopup(loadedProgram);
}

    private static void showExpandedProgramPopup(Program program) {
        Stage popup = new Stage();
        popup.setTitle("Expanded Program");

        TableView<InstructionRow> table = new TableView<>();

        TableColumn<InstructionRow, Number> numberCol = new TableColumn<>("#");
        numberCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumber()));

        TableColumn<InstructionRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));

        TableColumn<InstructionRow, String> labelCol = new TableColumn<>("Label");
        labelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabel()));

        TableColumn<InstructionRow, String> commandCol = new TableColumn<>("Command");
        commandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCommand()));

        TableColumn<InstructionRow, Number> cyclesCol = new TableColumn<>("Cycles");
        cyclesCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCycles()));

        table.getColumns().addAll(numberCol, typeCol, labelCol, commandCol, cyclesCol);

        PrintExpansion pE = new PrintExpansion(program);
        List<AbstractInstruction> expanded = pE.getExpandedInstructions();

        ObservableList<InstructionRow> rows = FXCollections.observableArrayList();
        int counter = 1;
        for (AbstractInstruction instr : expanded) {
            rows.add(new InstructionRow(
                    counter++,
                    instr.getType().toString(),
                    instr.getLabel() != null ? instr.getLabel().toString() : "",
                    instr.commandDisplay(),
                    instr.getCycles()
            ));
        }
        table.setItems(rows);

        Scene scene = new Scene(new BorderPane(table), 600, 400);
        popup.setScene(scene);
        popup.show();
    }


}
