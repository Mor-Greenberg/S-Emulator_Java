package ui.executionBoard;

import handleExecution.ExecutionRunner;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import logic.Variable.Variable;
import logic.instruction.Instruction;
import logic.program.Program;
import okhttp3.OkHttpClient;

import session.UserSession;
import ui.executionBoard.instructionTable.InstructionRow;
import ui.executionBoard.variablesTable.VariableRow;
import util.HttpClientUtil;
import utils.UiUtils;

import java.util.*;

public class ExecutionBoardController {

    @FXML private Label userNameField;
    @FXML private Label creditsLabel;

    // --- Instruction Table ---
    @FXML private TableView<InstructionRow> instructionsTable;
    @FXML private TableColumn<InstructionRow, Number> colIndex;
    @FXML private TableColumn<InstructionRow, String> colBS;
    @FXML private TableColumn<InstructionRow, String> colLabel;
    @FXML private TableColumn<InstructionRow, String> colInstruction;
    @FXML private TableColumn<InstructionRow, Number> colCycles;
    @FXML private TableColumn<InstructionRow, String> colArch;

    @FXML private Label summaryLabel;

    // --- Variables Table ---
    @FXML private TableView<VariableRow> variablesTable;
    @FXML private TableColumn<VariableRow, String> variableNameCol;
    @FXML private TableColumn<VariableRow, Number> variableValueCol;

    @FXML private Label debugCycles;
    @FXML private VBox vboxDebuggerArea;

    private Program loadedProgram;

    private final OkHttpClient client = HttpClientUtil.getClient();
    private final ObservableList<InstructionRow> instructionData = FXCollections.observableArrayList();

    private List<Instruction> originalInstructions = Collections.emptyList();

    // =====================================================
    // Initialization
    // =====================================================
    @FXML
    public void initialize() {
        String username = UserSession.getUsername();
        userNameField.setText(username);
        // --- Instructions Table setup ---
        colIndex.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumber()));
        colBS.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colLabel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabel()));
        colInstruction.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCommand()));
        colCycles.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCycles()));
        colArch.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArchitecture()));
        instructionsTable.setItems(instructionData);

        // --- Variables Table setup ---
        variableNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        variableValueCol.setCellValueFactory(
                data -> data.getValue().nameProperty().length());
    }

    // =====================================================
    // Execution Actions
    // =====================================================

    @FXML
    private void startExecution(ActionEvent event) {
        if (loadedProgram == null) {
            UiUtils.showError("No program loaded.");
            return;
        }
        ExecutionRunner.runProgram(loadedProgram);
        updateVariablesView();
    }

    @FXML
    private void stepOverExecution(ActionEvent e) {
        if (loadedProgram == null) {
            UiUtils.showError("No program loaded.");
            return;
        }
        ExecutionRunner.stepOver();
        updateVariablesView();
        highlightCurrentInstruction(ExecutionRunner.getCurrentIndex());
    }

    @FXML
    private void stopExecution(ActionEvent e) {
        if (loadedProgram == null) {
            UiUtils.showError("No program loaded.");
            return;
        }
        ExecutionRunner.stop();
        UiUtils.showAlert("Program stopped.");
    }

    @FXML
    private void startDebug(ActionEvent e) {
        if (loadedProgram == null) {
            UiUtils.showError("No program loaded.");
            return;
        }
        ExecutionRunner.startDebug(loadedProgram);
        updateVariablesView();
    }

    @FXML
    private void resumeExecution(ActionEvent e) {
        if (loadedProgram == null) {
            UiUtils.showError("No program loaded.");
            return;
        }
        ExecutionRunner.resume();
        updateVariablesView();
    }

    @FXML
    private void onReRunClicked(ActionEvent e) {
        if (loadedProgram == null) {
            UiUtils.showError("No program loaded.");
            return;
        }

        ExecutionRunner.setPrefilledInputs(ExecutionRunner.getExecutionContextMap());
        ExecutionRunner.setPrefilledDegree(ExecutionRunner.getCurrentDegree());

        ExecutionRunner.runProgram(loadedProgram);

        updateVariablesView();
    }


    // =====================================================
    // UI & Data Updates
    // =====================================================

    public void updateVariablesView() {
        Map<Variable, Long> vars;

        if (ExecutionRunner.isDebugMode()) {
            vars = ExecutionRunner.getDebugContext().getVariableState();
        } else {
            vars = ExecutionRunner.getExecutionContextMap();
        }

        ObservableList<VariableRow> rows = FXCollections.observableArrayList();
        vars.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getRepresentation()))
                .forEach(entry ->
                        rows.add(new VariableRow(entry.getKey().getRepresentation(), entry.getValue()))
                );

        Platform.runLater(() -> variablesTable.setItems(rows));
    }

    public void updateCyclesView(int cycles) {
        Platform.runLater(() -> debugCycles.setText("Cycles: " + cycles));
    }

    public void highlightCurrentInstruction(int index) {
        if (index < 0 || index >= instructionsTable.getItems().size()) return;
        Platform.runLater(() -> {
            instructionsTable.getSelectionModel().clearAndSelect(index);
            instructionsTable.scrollTo(index);
        });
    }

    public void printInstructions(List<Instruction> instructions) {
        ObservableList<InstructionRow> rows = FXCollections.observableArrayList();
        int counter = 1;
        for (Instruction instr : instructions) {
            String bs = (instr.getType() != null) ? instr.getType().toString() : "";
            String label = (instr.getLabel() != null) ? instr.getLabel().toString() : "";
            String command = instr.commandDisplay();
            int cycles = instr.getCycles();
            rows.add(new InstructionRow(counter++, bs, label, command, cycles, "RISC-V"));
        }
        originalInstructions = instructions;
        Platform.runLater(() -> {
            instructionsTable.setItems(rows);
            summaryLabel.setText("Loaded " + rows.size() + " instructions");
        });
    }

    // =====================================================
    // External Loaders
    // =====================================================

    public void setLoadedProgram(Program program) {
        this.loadedProgram = program;
        printInstructions(program.getInstructions());
    }

}
