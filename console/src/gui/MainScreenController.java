package gui;

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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.jaxb.schema.generated.SProgram;
import logic.label.FixedLabel;
import logic.program.Program;
import logic.xml.XmlLoader;
import programDisplay.ProgramDisplayImpl;

import java.io.File;
import java.util.List;

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
    }



    @FXML
    private void loadFilePressed(ActionEvent event) {
        System.out.println("Load File button pressed");

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

                        System.out.println("Program loaded successfully from: " + selectedFile.getAbsolutePath());
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
    }


}
