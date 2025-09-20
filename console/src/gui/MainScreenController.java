package gui;

import gui.highlightSelectionPopup.HighlightAction;
import gui.highlightSelectionPopup.HighlightChoiceListener;
import gui.highlightSelectionPopup.HighlightSelectionController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
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
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.program.Program;
import logic.xml.XmlLoader;
import programDisplay.ProgramDisplayImpl;

import java.io.File;
import java.io.IOException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static gui.instructionTable.InstructionRow.getAllLabels;
import static gui.instructionTable.InstructionRow.getAllVariables;
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


    @FXML private TableView<InstructionRow> instructionTable;
    @FXML private TableColumn<InstructionRow, Number> colNumber;
    @FXML private TableColumn<InstructionRow, String> colType;
    @FXML private TableColumn<InstructionRow, String> colLabel;
    @FXML private TableColumn<InstructionRow, String> colCommand;
    @FXML private TableColumn<InstructionRow, Number> colCycles;
    @FXML private Label summaryLabel;



    private ObservableList<InstructionRow> instructionData = FXCollections.observableArrayList();

    @FXML private ToggleButton animationToggleButton;

    private boolean enableLoadingAnimation = true; // set to false to skip animation

    @FXML
    public void initialize() {
        animationToggleButton.setSelected(true);
        animationToggleButton.setText("Animations Off");
        colNumber.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumber()));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colLabel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabel()));
        colCommand.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCommand()));
        colCycles.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCycles()));
        instructionTable.setItems(instructionData);

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

                        if (enableLoadingAnimation) {
                            loadingProgressBar.setProgress(0);
                            animateProgressBar(2.0, enableLoadingAnimation, loadingProgressBar);
                        } else {
                            loadingProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS); // אין אנימציה
                        }
                    });

                    Thread.sleep(500);

                    loadedSProgram = XmlLoader.loadFromFile(selectedFile.getAbsolutePath());
                    loadedProgram = new XmlLoader().SprogramToProgram(loadedSProgram);

                    Platform.runLater(() -> {
                        if (!enableLoadingAnimation) {
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

        if (programDisplay == null) {
            showError("Program display not initialized.");
            return;
        }

        ExecutionRunner.runProgram(loadedProgram, programDisplay);
    }


    @FXML
    private void stopExecution(ActionEvent event) {
        System.out.println("Stop Execution");
    }

    @FXML
    private void startDebug(ActionEvent event) {
        System.out.println("Start Debug Execution");
    }

    @FXML
    private void resumeExecution(ActionEvent event) {
        System.out.println("Resume Execution");
    }

    @FXML
    private void toggleAnimations(ActionEvent event) {
        enableLoadingAnimation = animationToggleButton.isSelected();
        if (enableLoadingAnimation) {
            animationToggleButton.setText("Animations Off");
        } else {
            animationToggleButton.setText("Animations On");
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

        instructionTable.setItems(rows);
        summaryLabel.setText(generateSummary(instructions));


    }
    @FXML
    void onHighlightSelectionClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/highlightSelectionPopup/highlight_selection_popup.fxml"));
        Parent root = loader.load();

        HighlightAction highlightAction = new HighlightAction(instructionTable);
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
        Map<Variable, Long> allVariables = ExecutionRunner.getExecutionContextMap();
        showVariablesPopup(allVariables);
    }

    private void showVariablesPopup(Map<Variable, Long> allVariables) {
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
