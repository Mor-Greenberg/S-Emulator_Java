package gui;

import handleExecution.HandleExecution;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.execution.ProgramExecutorImpl;
import logic.jaxb.schema.generated.SProgram;
import logic.program.Program;
import logic.xml.XmlLoader;
import programDisplay.ProgramDisplayImpl;
import utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static utils.Utils.showError;
import static utils.Utils.toHex;

public class MainScreenController {

    @FXML
    private Button loadFileButton;
    private SProgram loadedSProgram;
    private Program loadedProgram;
    private ProgramDisplayImpl programDisplay = new ProgramDisplayImpl(loadedProgram);

    @FXML private Label xmlPathLabel;
    @FXML private ProgressBar loadingProgressBar;
    @FXML private Label statusLabel;
    private Timeline loadingTimeline;

    @FXML private ToggleButton animationToggleButton;

    private boolean enableLoadingAnimation = true; // set to false to skip animation
    @FXML
    public void initialize() {
        animationToggleButton.setSelected(true);
        animationToggleButton.setText("Animations Off");
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
                    // התחלת טעינה
                    Platform.runLater(() -> {
                        statusLabel.setText("Loading...");
                        xmlPathLabel.setText(selectedFile.getAbsolutePath());

                        if (enableLoadingAnimation) {
                            loadingProgressBar.setProgress(0); // נתחיל באנימציה רגילה
                            animateProgressBar(2.0);
                        } else {
                            loadingProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS); // אין אנימציה
                        }
                    });

                    // זמן טעינה מזויף
                    Thread.sleep(500);

                    // טעינת הקובץ
                    loadedSProgram = XmlLoader.loadFromFile(selectedFile.getAbsolutePath());
                    loadedProgram = new XmlLoader().SprogramToProgram(loadedSProgram);

                    Platform.runLater(() -> {
                        if (!enableLoadingAnimation) {
                            loadingProgressBar.setProgress(1.0); // במידה ואין אנימציה
                            statusLabel.setText("Done!");
                        } else {
                            // נקבע פעולה בסיום האנימציה: עדכון סטטוס
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
    private void animateProgressBar(double durationInSeconds) {
        if (!enableLoadingAnimation) {
            loadingProgressBar.setProgress(1.0);
            loadingProgressBar.setStyle("-fx-accent: pink;");
            return;
        }

        loadingProgressBar.setProgress(0);

        Timeline timeline = new Timeline();
        int frames = 60; // 60 steps
        for (int i = 0; i <= frames; i++) {
            double progress = (double) i / frames;
            Duration time = Duration.seconds(progress * durationInSeconds);

            // גוון צבע לפי ההתקדמות (Hue בין 0 ל-360)
            double hue = progress * 360;
            Color color = Color.hsb(hue, 0.7, 1.0);
            String hexColor = toHex(color);

            KeyFrame frame = new KeyFrame(time, e -> {
                loadingProgressBar.setProgress(progress);
                loadingProgressBar.setStyle("-fx-accent: " + hexColor + ";");
            });
            timeline.getKeyFrames().add(frame);
        }

        timeline.setCycleCount(1);
        timeline.play();
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




}
