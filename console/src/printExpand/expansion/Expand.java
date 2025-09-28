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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.Variable.Variable;

import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.program.Program;
import utils.Utils;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static logic.blaxBox.BlackBox.executeBlackBox;

public class Expand {
public static void expandAction(Program loadedProgram) {
    Map<Variable, Long> variableState = loadedProgram.getVars().stream()
            .collect(Collectors.toMap(v -> v, v -> 0L));
    ExecutionContext context = new ExecutionContextImpl(variableState, loadedProgram.getFunctionMap());

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

    if (chosenDegree == 0) {
        long res = executeBlackBox(context,loadedProgram);
        System.out.println("Black-box result for y = " + res);

        ObservableList<InstructionRow> rows = FXCollections.observableArrayList();
        int counter = 1;
        for (Instruction instr : loadedProgram.getInstructions()) {
            rows.add(new InstructionRow(
                    counter++,
                    instr.getType().toString(),
                    instr.getLabel() != null ? instr.getLabel().getLabelRepresentation() : "",
                    instr.commandDisplay(),
                    instr.getCycles()
            ));
        }

        showExpandedProgramPopup(rows);
        return;
    }
    loadedProgram.expandToDegree(chosenDegree, context);
    showExpandedProgramPopup(loadedProgram);
}



    private static void showExpandedProgramPopup(ObservableList<InstructionRow> rows) {
        TableView<InstructionRow> table = new TableView<>(rows);

        TableColumn<InstructionRow, Number> colNum = new TableColumn<>("#");
        colNum.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumber()));

        TableColumn<InstructionRow, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));

        TableColumn<InstructionRow, String> colLabel = new TableColumn<>("Label");
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLabel()));

        TableColumn<InstructionRow, String> colCmd = new TableColumn<>("Command");
        colCmd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCommand()));

        TableColumn<InstructionRow, Number> colCycles = new TableColumn<>("Cycles");
        colCycles.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCycles()));

        table.getColumns().addAll(colNum, colType, colLabel, colCmd, colCycles);

        Stage popup = new Stage();
        popup.setTitle("Expanded Program");
        popup.setScene(new Scene(new VBox(table), 600, 400));
        popup.show();
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
    public static List<AbstractInstruction> getExpandedInstructions(Program program) {
        ExecutionContext context = new ExecutionContextImpl(
                program.getVarsAsMapWithZeroes(),
                program.getFunctionMap()
        );

        int degree = program.askForDegree(context);
        if (degree < 0) {
            return Collections.emptyList();
        }

        program.expandToDegree(degree, context);

        return program.getExpandedInstructions();
    }


}
