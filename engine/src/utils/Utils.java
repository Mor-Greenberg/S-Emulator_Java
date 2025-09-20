package utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import logic.instruction.Instruction;
import logic.instruction.InstructionType;


import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static String toHex(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }
    public static void animateProgressBar(double durationInSeconds, boolean enableLoadingAnimation,ProgressBar loadingProgressBar)
 {
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
    public static String generateSummary(List<Instruction> instructions) {
        long basicCount = instructions.stream()
                .filter(instr -> instr.getType() == InstructionType.B)
                .count();

        long syntheticCount = instructions.stream()
                .filter(instr -> instr.getType() == InstructionType.S)
                .count();
        long cyclesCount = instructions.stream()
                .mapToLong(Instruction::getCycles)
                .sum();


        return "SUMMARY: Total instructions: " + instructions.size()
                + " | Basic: " + basicCount
                + " | Synthetic: " + syntheticCount
                + " | cycles: " + cyclesCount;
    }






}
