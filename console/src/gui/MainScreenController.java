package gui;

import gui.highlightSelectionPopup.HighlightAction;
import gui.highlightSelectionPopup.HighlightChoiceListener;
import gui.highlightSelectionPopup.HighlightSelectionController;
import gui.instructionTable.ExpandedTable;
import gui.showStatus.Status;
import gui.variablesTable.VariableRow;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jaxbV2.jaxb.v2.SProgram;
import gui.instructionTable.InstructionRow;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.program.Program;
import logic.xml.XmlLoader;
import printExpand.expansion.Expand;
import programDisplay.ProgramDisplayImpl;

import java.io.File;
import java.io.IOException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static gui.instructionTable.InstructionRow.*;
import static gui.showStatus.Status.showVariablesPopup;
import static gui.stats.ShowStats.presentStatistics;
import static printExpand.expansion.PrintExpansion.getInstructionHistoryChain;
import static utils.Utils.*;

public class MainScreenController {

    @FXML private Button loadFileButton;
    private SProgram loadedSProgram;
    private Program loadedProgram;
    private ProgramDisplayImpl programDisplay;
    public void setProgramDisplay(ProgramDisplayImpl programDisplay) {
        this.programDisplay = programDisplay;
    }

    @FXML private Label xmlPathLabel;
    @FXML private ProgressBar loadingProgressBar;
    @FXML private Label statusLabel;

    @FXML private Button showStatusButton;


    @FXML private TableView<InstructionRow> instructionTable;
    @FXML private TableColumn<InstructionRow, Number> colNumber;
    @FXML private TableColumn<InstructionRow, String> colType;
    @FXML private TableColumn<InstructionRow, String> colLabel;
    @FXML private TableColumn<InstructionRow, String> colCommand;
    @FXML private TableColumn<InstructionRow, Number> colCycles;
    @FXML private Label summaryLabel;
    @FXML private Button currMaxDegreeButton;
    @FXML private Button expandButton;
    private final ExpandedTable expandedTable = new ExpandedTable();
    @FXML
    private TableView<VariableRow> variablesTable;

    @FXML
    private TableColumn<VariableRow, String> variableNameCol;

    @FXML
    private TableColumn<VariableRow, Number> variableValueCol;

    private static MainScreenController instance;

    public MainScreenController() {
        instance = this;
    }

    public static MainScreenController getInstance() {
        return instance;
    }


    @FXML
    private VBox historyContainer;

    public List<Instruction> originalInstructions = java.util.Collections.emptyList();
    public void setOriginalInstructions(List<Instruction> list) {
        this.originalInstructions = (list != null) ? list : java.util.Collections.emptyList();
    }
    private ObservableList<InstructionRow> instructionData = FXCollections.observableArrayList();

    @FXML private ToggleButton animationToggleButton;

    private boolean enableAnimation = true; // set to false to skip animation


    @FXML
    public void initialize() {
        animationToggleButton.setSelected(true);
        animationToggleButton.setText("Animations On");
        colNumber.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumber()));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colLabel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabel()));
        colCommand.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCommand()));
        colCycles.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCycles()));
        instructionTable.setItems(instructionData);
        instructionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) -> {
            if (newRow == null) {
                historyContainer.getChildren().clear();
                return;
            }

            // 🛡️ הגנה: אם אין לנו מקור, אין על מה למפות
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

        variableNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        variableValueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

    }

    @FXML
    private void loadFilePressed(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            new Thread(() -> {
                try {
                    Platform.runLater(() -> {
                        statusLabel.setText("Loading...");
                        xmlPathLabel.setText(selectedFile.getAbsolutePath());

                        if (enableAnimation) {
                            loadingProgressBar.setProgress(0);
                            animateProgressBar(2.0, enableAnimation, loadingProgressBar);
                        } else {
                            loadingProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS); // אין אנימציה
                        }
                    });

                    Thread.sleep(500);

                    loadedSProgram = XmlLoader.loadFromFile(selectedFile.getAbsolutePath());
                    loadedProgram = new XmlLoader().SprogramToProgram(loadedSProgram);

                    Platform.runLater(() -> {
                        if (!enableAnimation) {
                            loadingProgressBar.setProgress(1.0);
                            statusLabel.setText("Done!");
                        } else {
                            Timeline timeline = new Timeline(
                                    new KeyFrame(Duration.seconds(2.0),
                                            e -> statusLabel.setText("Done!")) // רק אחרי שהאנימציה הסתיימה
                            );
                            timeline.play();
                        }

                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Failed to load");
                        showError("Failed to load program: " + e.getMessage());
                        loadingProgressBar.setProgress(0);
                    });
                    e.printStackTrace();
                }
            }).start();
        } else {
            System.out.println("File selection was cancelled.");
        }
    }

    @FXML
    private void startExecution(ActionEvent event) {
        if (loadedProgram == null) {
            showError("No program loaded.");
            return;
        }
        ExecutionRunner.runProgram(loadedProgram, programDisplay);
    }
    @FXML
    private void stepOverExecution(ActionEvent e) {
        ExecutionRunner.stepOver();
        updateVariablesView();
        ExecutionRunner.highlightCurrentInstruction(ExecutionRunner.getCurrentIndex(),instructionTable);
    }



    @FXML
    private void stopExecution(ActionEvent e) {
        if (loadedProgram == null) {
            showError("No program loaded.");
            return;
        }
        ExecutionRunner.stop();
    }


    @FXML
    private void startDebug(ActionEvent e) {
        if (loadedProgram == null) {
            showError("No program loaded.");
            return;
        }
        ExecutionRunner.startDebug(loadedProgram);
        updateVariablesView();
    }

    @FXML
    private void resumeExecution(ActionEvent e) {
        if (loadedProgram == null) {
            showError("No program loaded.");
            return;
        }
        ExecutionRunner.resume();
        updateVariablesView();
    }



    @FXML
    private void toggleAnimations(ActionEvent event) {
        enableAnimation = animationToggleButton.isSelected();
        if (enableAnimation) {
            animationToggleButton.setText("Animations On");
        } else {
            animationToggleButton.setText("Animations Off");
        }
    }


    public void printInstructions(List<Instruction> instructions) {

        ObservableList<InstructionRow> rows = FXCollections.observableArrayList();

        int counter = 1;
        int nextId = 1;
        for (Instruction instr : instructions) {
            if (instr instanceof AbstractInstruction absInstr && absInstr.getUniqueId() == 0) {
                absInstr.setUniqueId(nextId++);
            }
        }

        for (Instruction instr : instructions) {
            String type = instr.getType().toString();
            String label = (instr.getLabel() != null && !instr.getLabel().equals(FixedLabel.EMPTY))
                    ? instr.getLabel().toString()
                    : "";
            String command = instr.commandDisplay();
            int cycles = instr.getCycles();

            rows.add(new InstructionRow(counter++, type, label, command, cycles));

        }
        this.originalInstructions = instructions;
        instructionTable.setItems(rows);
        summaryLabel.setText(generateSummary(instructions));
    }
    @FXML
    void onHighlightSelectionClicked() throws IOException {
        if (loadedProgram == null) {
            showError("No program loaded.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/highlightSelectionPopup/highlight_selection_popup.fxml"));
        Parent root = loader.load();

        HighlightAction highlightAction = new HighlightAction(instructionTable, enableAnimation);
        List<InstructionRow> rows = instructionTable.getItems();
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
                highlightAction.clearHighlight(enableAnimation);
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
            // בריצת דיבאג – ניקח ישירות את ה־state מתוך ה־debugContext
            allVariables = ExecutionRunner.getDebugContext().getVariableState();
        } else {
            // בריצה רגילה – מה־map האחרון ששמרנו
            allVariables = ExecutionRunner.getExecutionContextMap();
        }

        if (enableAnimation) {
            Status.animateStatusButton(showStatusButton, allVariables);
        } else {
            showVariablesPopup(allVariables);
        }
    }


    @FXML
    private void onShowStatisticsClicked() {
        if (loadedProgram == null) {
            showError("No program loaded.");
            return;
        }
        presentStatistics();
    }
    @FXML
    private void onExpandButton() {
        if (loadedProgram == null) {
            showError("No program loaded.");
            return;
        }
        Expand.expandAction(loadedProgram, programDisplay);
    }
    @FXML
    private void onCurrMaxDegreeButton() {
        int current = ExecutionRunner.getCurrentDegree();
        int max = loadedProgram.calculateMaxDegree();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Expansion Degree Info");
        alert.setHeaderText("Current and Max Expansion Degree");
        alert.setContentText("Current Degree: " + current + "\nMax Degree: " + max);
        alert.showAndWait();
    }


    void updateVariablesView() {
        Map<Variable, Long> vars = ExecutionRunner.getDebugContext().getVariableState();

        ObservableList<VariableRow> rows = FXCollections.observableArrayList();
        vars.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getRepresentation()))
                .forEach(entry -> rows.add(
                        new VariableRow(entry.getKey().getRepresentation(), entry.getValue())
                ));

        variablesTable.setItems(rows);
    }


    public void highlightCurrentInstruction(int index) {
        if (index < 0 || index >= instructionTable.getItems().size()) return;
        instructionTable.getSelectionModel().clearAndSelect(index);
        instructionTable.scrollTo(index);
        instructionTable.requestFocus();
    }
    public void clearInstructionTable() {
        instructionTable.getItems().clear();
    }

    public void addInstructionRow(InstructionRow row) {
        instructionTable.getItems().add(row);
    }
    @FXML
    private Label debugCycles;

    public void updateCyclesView(int cycles) {
        debugCycles.setText("Cycles: " + cycles);

        List<Instruction> instructions = loadedProgram.getActiveInstructions(); // או source אחר
        summaryLabel.setText(generateSummary(instructions));
    }



}
