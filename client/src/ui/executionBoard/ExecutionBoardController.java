package ui.executionBoard;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.ProgramStatsDTO;
import gui.reRun.ReRunService;
import logic.architecture.ArchitectureData;
import logic.architecture.ArchitectureRules;
import logic.execution.ExecutionContext;
import logic.execution.HandleCredits;
import logic.instruction.AbstractInstruction;
import printExpand.expansion.Expand;
import ui.dashboard.DashboardController;
import ui.executionBoard.highlightSelectionPopup.HighlightAction;
import ui.executionBoard.highlightSelectionPopup.HighlightChoiceListener;
import ui.executionBoard.highlightSelectionPopup.HighlightSelectionController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
import ui.executionBoard.instructionTable.ExpandedTable;
import ui.executionBoard.instructionTable.InstructionRow;
import ui.executionBoard.variablesTable.VariableRow;
import util.HttpClientUtil;
import utils.UiUtils;
import utils.Utils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;



import static gui.showStatus.Status.showVariablesPopup;
import static printExpand.expansion.PrintExpansion.getInstructionHistoryChain;
import static session.UserSession.confirmReusePreviousArchitecture;
import static ui.executionBoard.instructionTable.InstructionRow.*;
import static utils.UiUtils.showError;


public class ExecutionBoardController {

    @FXML private Label userNameField;
    @FXML private Label creditsLabel;

    private final ObservableList<InstructionRow> instructionData = FXCollections.observableArrayList();

    // --- Instruction Table ---
    @FXML private TableView<InstructionRow> instructionsTable;
    @FXML private TableColumn<InstructionRow, Number> colIndex;
    @FXML private TableColumn<InstructionRow, String> colBS;
    @FXML private TableColumn<InstructionRow, String> colLabel;
    @FXML private TableColumn<InstructionRow, String> colInstruction;
    @FXML private TableColumn<InstructionRow, Number> colCycles;
    @FXML private TableColumn<InstructionRow, String> colArch;

    @FXML private Label summaryLabel;
    @FXML private Label architectureLabel;
    @FXML private Button highlightButton;

    @FXML
    private VBox historyContainer;
    // --- Variables Table ---
    @FXML private TableView<VariableRow> variablesTable;
    @FXML private TableColumn<VariableRow, String> variableNameCol;
    @FXML private TableColumn<VariableRow, Number> variableValueCol;


    @FXML private Label debugCycles;
    @FXML private VBox vboxDebuggerArea;
    @FXML private Button ReRunButton;
    private Program loadedProgram;
    @FXML private Button showStatusButton;

    private final OkHttpClient client = HttpClientUtil.getClient();

    private List<Instruction> originalInstructions = Collections.emptyList();
    private ProgramStatsDTO selectedProgram;
    private int userCredits;
    private static ExecutionBoardController instance;
    public ArchitectureData architecture = null;

    public static ExecutionBoardController getInstance() {
        return instance;
    }
    @FXML
    public void initialize() {
        String username = UserSession.getUsername();
        userNameField.setText(username);
        updateArchitectureLabel(null);

        instance = this;

        // --- Instructions Table setup ---
        colIndex.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumber()));
        colBS.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colLabel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabel()));
        colInstruction.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCommand()));
        colCycles.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCycles()));
        colArch.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArchitecture()));
        instructionsTable.setItems(instructionData);
        instructionsTable.setRowFactory(tv -> new TableRow<InstructionRow>() {
            @Override
            protected void updateItem(InstructionRow row, boolean empty) {
                super.updateItem(row, empty);

                if (empty || row == null) {
                    setStyle("");
                    return;
                }

                ArchitectureData selectedArch = ExecutionRunner.architecture;

                if (selectedArch != null && row.getArchitecture() != null && !row.getArchitecture().isEmpty()) {
                    try {
                        ArchitectureData instrArch = ArchitectureData.valueOf(row.getArchitecture());

                        if (instrArch.ordinal() > selectedArch.ordinal()) {
                            setStyle("-fx-background-color: #ffcccc;");
                        } else {
                            setStyle("");
                        }
                    } catch (IllegalArgumentException e) {
                        setStyle("");
                    }
                } else {
                    setStyle("");
                }
            }
        });

        // --- Instruction selection logic ---
        instructionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) -> {
            if (newRow == null) {
                historyContainer.getChildren().clear();
                return;
            }
            if (originalInstructions == null || originalInstructions.isEmpty()) {
                historyContainer.getChildren().clear();
                return;
            }

            Instruction matchedInstruction = findInstructionFromRow(newRow, originalInstructions);
            if (matchedInstruction instanceof AbstractInstruction absInstr) {
                List<AbstractInstruction> history = getInstructionHistoryChain(absInstr);
                TableView<AbstractInstruction> historyTable = expandedTable.buildInstructionTableFrom(history);
                historyContainer.getChildren().setAll(historyTable);
            } else {
                historyContainer.getChildren().clear();
            }
        });

        // --- Variables Table setup ---
        variableNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        variableValueCol.setCellValueFactory(data -> data.getValue().valueProperty());

        ExecutionRunner.setRunCompletionListener(dto -> {
            Platform.runLater(() -> {
                try {
                    DashboardController.refreshProgramsFromServer();
                } catch (Exception ignored) {}

                try {
                    ui.dashboard.UserHistory.refreshUserHistory();
                } catch (Exception ignored) {}

                try {
                    creditsLabel.setText("Available Credits: " + UserSession.getUserCredits());
                } catch (Exception ignored) {}
            });
        });
    }

    // =====================================================
    // Execution Actions
    // =====================================================
    private boolean firstrun=true;
    @FXML
    private void startExecution(ActionEvent event) {
        if (loadedProgram == null) {
            UiUtils.showError("No program loaded.");
            return;
        }

        // Architecture not selected yet
        if (architecture == null ) {
            UiUtils.showError("No architecture selected.\nPlease select an architecture before running the program.");
            return;
        }
        ArchitectureData last = UserSession.getInstance().getLastArchitecture();
        if (last != null&&!firstrun) {
            boolean ok = UserSession.confirmReusePreviousArchitecture(
                    ((Button) event.getSource()).getScene().getWindow(),
                    last.toString(),
                    "Regular Execution"
            );
            if (!ok) return;
        }

        updateArchitectureLabel(last);

        if (userCredits <= 0) {
            UiUtils.showError("You don't have enough credits to run this program.");
            return;
        }

        // continue normally
        ExecutionRunner.runProgram(loadedProgram);
        firstrun = false;
        updateVariablesView();

        int cost = Utils.computeProgramDegree(
                loadedProgram,
                new ExecutionContextImpl(
                        new HashMap<>(),
                        loadedProgram.getFunctionMap(),
                        new HashMap<>()
                )
        );

        updateCreditsAfterExecution(loadedProgram.getName(), cost);
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

        // Architecture not selected yet
        if (architecture == null ) {
            UiUtils.showError("No architecture selected.\nPlease select an architecture before starting debug mode.");
            return;
        }
        ArchitectureData last = UserSession.getInstance().getLastArchitecture();
        if (last != null&&!firstrun) {
            boolean ok = UserSession.confirmReusePreviousArchitecture(
                    ((Button) e.getSource()).getScene().getWindow(),
                    last.toString(),
                    "Debug Execution"
            );
            if (!ok) return;
        }

        updateArchitectureLabel(last);

        if (userCredits <= 0) {
            UiUtils.showError("You don't have enough credits to start debugging this program.");
            return;
        }

        // continue normally
        ExecutionRunner.startDebug(loadedProgram);
        firstrun = false;
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

    // =====================================================
    // UI & Data Updates
    // =====================================================

    private void updateArchitectureLabel(ArchitectureData arch) {
        if (architectureLabel == null) return;
        String text = (arch == null)
                ? "Architecture selected : none"
                : "Architecture selected : " + arch.name() + " (" + arch.getCreditsCost() + " credits)";
        Platform.runLater(() -> architectureLabel.setText(text));
    }

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
            String bs = instr.getType() != null ? instr.getType().toString() : "";
            String label = instr.getLabel() != null ? instr.getLabel().toString() : "";
            String command = instr.commandDisplay();
            int cycles = instr.getCycles();

            ArchitectureData minArch = ArchitectureRules.getMinArchitectureFor(instr.getData());
            String archName = (minArch != null) ? minArch.name() : "";

            rows.add(new InstructionRow(counter++, bs, label, command, cycles, archName,false));
        }

        originalInstructions = instructions;
        Platform.runLater(() -> instructionsTable.setItems(rows));
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



    public TableView<InstructionRow> getInstructionTable() {
        return instructionsTable;
    }
    @FXML
    void onHighlightSelectionClicked() throws IOException {
        if (loadedProgram == null) {
            showError("No program loaded.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/executionBoard/highlightSelectionPopup/highlight_selection_popup.fxml"));
        Parent root = loader.load();

        HighlightAction highlightAction = new HighlightAction(instructionsTable);
        List<InstructionRow> rows = instructionsTable.getItems();
        HighlightSelectionController controller = loader.getController();

        controller.setListener(new HighlightChoiceListener() {
            @Override
            public void onLabelChosen() {
                try {
                    List<String> labelOptions = getAllLabels(rows);
                    String selected = highlightAction.showChoicePopup(labelOptions, "Highlight by Label");
                    if (selected != null) {
                        highlightAction.highlightByLabel(selected);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onVariableChosen() {
                try {
                    List<String> variableOptions = getAllVariables(rows);
                    String selected = highlightAction.showChoicePopup(variableOptions, "Highlight by Variable");
                    if (selected != null) {
                        highlightAction.highlightByVariable(selected);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onClearHighlight() {
                highlightAction.clearHighlight();
            }
        });

        Stage stage = new Stage();
        stage.setTitle("Highlight Selection");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    @FXML
    void onShowStatusClicked() {
        if (loadedProgram == null) {
            showError("No program loaded.");
            return;
        }

        Map<Variable, Long> allVariables;

        if (ExecutionRunner.isDebugMode()) {
            allVariables = ExecutionRunner.getDebugContext().getVariableState();
        } else {
            allVariables = ExecutionRunner.getExecutionContextMap();
        }
        showVariablesPopup(allVariables);

    }
    public void loadProgramByName(String name) {
        fetchProgramFromServer(name);
    }
    public void notifyExecutionCompleted(String programName, int creditsUsed) {
        Request request = new Request.Builder()
                .url("http://localhost:8080/S-Emulator/execution-update?program=" + programName + "&creditsUsed=" + creditsUsed)
                .post(RequestBody.create(new byte[0], null))
                .build();

        HttpClientUtil.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("Failed to update execution stats: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.err.println("Server returned error for execution update: " + response.code());
                    response.close();
                    return;
                }

                String json = response.body().string();
                response.close();

                try {
                    com.google.gson.JsonObject obj =
                            com.google.gson.JsonParser.parseString(json).getAsJsonObject();

                    int remaining = obj.has("remaining") ? obj.get("remaining").getAsInt() : -1;
                    System.out.println("Execution stats updated for " + programName + " | Remaining credits: " + remaining);

                    if (remaining >= 0) {
                        UserSession.setUserCredits(remaining);
                        Platform.runLater(() ->
                                creditsLabel.setText("Available Credits: " + remaining));
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse execution-update response: " + e.getMessage());
                }
            }
        });
    }


    @FXML
    void onReRunClicked(ActionEvent event) {
        if (loadedProgram == null) {
            showError("No program loaded.");
            return;
        }

        ArchitectureData last = UserSession.getInstance().getLastArchitecture();
        if (last != null) {
            boolean ok = UserSession.confirmReusePreviousArchitecture(
                    ((Button) event.getSource()).getScene().getWindow(),
                    last.toString(),
                    "Re-Run Execution"
            );
            if (!ok) return;
            architecture = last;
            ExecutionRunner.architecture = last;
        }

        if (architecture == null) {
            UiUtils.showError("No architecture selected.\nPlease select an architecture before re-running the program.");
            return;
        }

        if (getUserCredits() <= 0) {
            UiUtils.showError("You don't have enough credits to re-run this program.");
            return;
        }

        ReRunService.prepareReRun(loadedProgram);

        List<String> options = Arrays.asList("Run", "Debug");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Run", options);
        dialog.setTitle("Re-Run Options");
        dialog.setHeaderText("Select how you want to re-run the program");
        dialog.setContentText("Mode:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String choice = result.get();
        if (choice.equals("Run")) {
            ExecutionRunner.runProgram(loadedProgram);
        } else if (choice.equals("Debug")) {
            ExecutionRunner.startDebug(loadedProgram);
        }
    }

    @FXML
    private void onDegreeCommandsAndInformationClicked() {
        if (loadedProgram == null) {
            UiUtils.showError("No program loaded.");
            return;
        }

        // Get current expansion degree (from ExecutionRunner)
        int currentDegree = ExecutionRunner.getCurrentDegree();
        boolean hasRun = currentDegree > 0;

        // Compute max expansion degree based on program definition
        Map<Variable, Long> variableState = new HashMap<>();
        loadedProgram.getVars().forEach(v -> variableState.put(v, 0L));

        ExecutionContext context = new ExecutionContextImpl(
                variableState,
                loadedProgram.getFunctionMap(),
                new HashMap<>()
        );

        int maxDegree = Utils.computeProgramDegree(loadedProgram, context);

        // Prepare message depending on execution state
        String message;
        if (!hasRun) {
            message = "No execution has been performed yet.\n\n" +
                    "Current Degree: ---\n" +
                    "Maximum Possible Degree: " + maxDegree;
        } else {
            message = "Current Degree: " + currentDegree + "\n" +
                    "Maximum Possible Degree: " + maxDegree;
        }

        // Create information popup
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Expansion Degree Information");
        alert.setHeaderText("Current and Maximum Expansion Degree");
        alert.setContentText(message);

        // Add an 'Expand Now' button to trigger the Expand action
        ButtonType expandButton = new ButtonType("Expand Now", ButtonBar.ButtonData.APPLY);
        alert.getButtonTypes().setAll(expandButton, ButtonType.CLOSE);

        // Handle 'Expand Now' click
        alert.showAndWait().ifPresent(response -> {
            if (response == expandButton) {
                onExpandButton();
            }
        });
    }

    private void onExpandButton() {
        if (loadedProgram == null) {
            showError("No program loaded.");
        }
        Expand.expandAction(loadedProgram,architecture);
    }

    // =====================================================
    // Credits
    // =====================================================


    public void setUserCredits(int credits) {
        this.userCredits = credits;
        Platform.runLater(() -> creditsLabel.setText("Available Credits: " + credits));
    }
    private void updateCreditsAfterExecution(String programName, int creditsUsed) {
        int remaining = UserSession.getUserCredits();
        creditsLabel.setText("Available Credits: " + remaining);

        Platform.runLater(() -> {
            creditsLabel.setText("Available Credits: " + remaining);
            System.out.println("Execution completed for " + programName +
                    " | Credits used: " + creditsUsed +
                    " | Remaining: " + remaining);
        });
        System.out.println("CLIENT: current credits=" + UserSession.getUserCredits());

    }

    @FXML
    public void onBackToDashboard() {
        Stage stage = (Stage) creditsLabel.getScene().getWindow();
        stage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/dashboard/S-Emulator-Dashboard.fxml"));
            Parent root = loader.load();
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("S-Emulator – Dashboard");
            dashboardStage.setScene(new Scene(root));
            dashboardStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            UiUtils.showError("Failed to return to dashboard: " + e.getMessage());
        }
    }


    public int getUserCredits() {
        return  userCredits;
    }
    @FXML
    private void onArchitectureSelectionClicked() {
        ChoiceDialog<ArchitectureData> dialog = new ChoiceDialog<>(
                ArchitectureData.I, ArchitectureData.values());
        dialog.setTitle("Architecture Selection");
        dialog.setHeaderText("Choose execution architecture");
        dialog.setContentText("Available architectures:");

        Optional<ArchitectureData> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        ArchitectureData chosenArch = result.get();
        int cost = chosenArch.getCreditsCost();
        int currentCredits = userCredits;

        if (currentCredits < cost) {
            UiUtils.showError("Not enough credits to select " + chosenArch.name());
            return;
        }

        userCredits -= cost;
        architecture = ArchitectureData.valueOf(chosenArch.name());
        UserSession.getInstance().setLastArchitecture(architecture);


        ExecutionRunner.architecture = architecture;
        updateArchitectureLabel(architecture);

        Platform.runLater(() -> creditsLabel.setText("Available Credits: " + userCredits));

        UiUtils.showAlert("Architecture '" + chosenArch.name() + "' selected.\nCost: " + cost + " credits.");
        instructionsTable.refresh();

    }


    private final ExpandedTable expandedTable = new ExpandedTable();

    public void updateSummaryLine(String text) {
        if (summaryLabel == null)
            return;
        Platform.runLater(() -> {
            summaryLabel.setText(text);
            summaryLabel.setTooltip(new Tooltip(text));
        });
    }

}

