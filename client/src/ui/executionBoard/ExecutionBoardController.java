package ui.executionBoard;

import com.google.gson.Gson;
import dto.ProgramStatsDTO;
import logic.execution.ExecutionRunner;
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
import logic.execution.ExecutionContextImpl;
import logic.instruction.Instruction;
import logic.program.Program;
import okhttp3.*;

import session.UserSession;
import ui.executionBoard.instructionTable.InstructionRow;
import ui.executionBoard.variablesTable.VariableRow;
import util.HttpClientUtil;
import utils.UiUtils;

import java.io.IOException;
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
    private ProgramStatsDTO selectedProgram;
    private int userCredits;
    private static ExecutionBoardController instance;

    public static ExecutionBoardController getInstance() {
        return instance;
    }
    public void setProgramStats(ProgramStatsDTO programStats) {
        this.selectedProgram = programStats;
        String name = programStats.getProgramName();
        Program program = ExecutionContextImpl.getGlobalProgramMap().get(name);

        if (program == null) {
            System.err.println("❌ Program not found in local map: " + name);
            System.err.println("Available programs: " + ExecutionContextImpl.getGlobalProgramMap().keySet());
            return;
        }

        printInstructions(program.getInstructions());
    }


    public void setUserCredits(int credits) {
        this.userCredits = credits;
        creditsLabel.setText("Available Credits:" + credits);
    }


    // =====================================================
    // Initialization
    // =====================================================
    @FXML
    public void initialize() {
        String username = UserSession.getUsername();
        userNameField.setText(username);

        instance=this;
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



    public void setProgramName(String programName) {
        this.selectedProgram.setProgramName(programName);
    }
    private void fetchProgramFromServer(String name) {
        Request request = new Request.Builder()
                .url("http://localhost:8080/S-Emulator/get-program?name=" + name)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> UiUtils.showError("Failed to fetch program: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Platform.runLater(() -> UiUtils.showError("Program not found on server."));
                    return;
                }
                String json = response.body().string();
                loadedProgram = new Gson().fromJson(json, Program.class);

                Platform.runLater(() -> {
                    printInstructions(loadedProgram.getInstructions());
                    summaryLabel.setText("Program loaded: " + name);
                });
            }
        });
    }

    public void setLoadedProgram(Program program) {
        this.loadedProgram = program;
        printInstructions(program.getInstructions());
    }

    public void setOriginalInstructions(List<Instruction> list) {
        this.originalInstructions = (list != null) ? list : java.util.Collections.emptyList();
    }

    public void clearInstructionTable() {
        instructionsTable.getItems().clear();
    }
    public void addInstructionRow(InstructionRow row) {
        instructionsTable.getItems().add(row);
    }


    public void updateSummaryView(int total, int basic, int synthetic, int cycles) {
        Platform.runLater(() -> {
            summaryLabel.setText(
                    String.format("SUMMARY: Total instructions: %d | Basic: %d | Synthetic: %d | cycles: %d",
                            total, basic, synthetic, cycles)
            );
        });
    }

    public TableView<InstructionRow> getInstructionTable() {
        return instructionsTable;
    }
}
