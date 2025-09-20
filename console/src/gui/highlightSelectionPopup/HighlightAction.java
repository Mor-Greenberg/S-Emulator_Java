package gui.highlightSelectionPopup;

import gui.instructionTable.InstructionRow;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class HighlightAction {
    private final TableView<InstructionRow> instructionTable;
    private String labelToHighlight = null;
    private String variableToHighlight = null;
    private boolean enableLoadingAnimation;

    public HighlightAction(TableView<InstructionRow> instructionTable, boolean enableAnimation) {
        this.instructionTable = instructionTable;
        this.enableLoadingAnimation = enableAnimation;
        initRowFactory();
    }

    private void initRowFactory() {
        instructionTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InstructionRow row, boolean empty) {
                super.updateItem(row, empty);

                if (row == null || empty) {
                    setStyle("");
                    return;
                }

                boolean shouldHighlight = false;

                if (labelToHighlight != null && row.getLabel().equals(labelToHighlight)) {
                    shouldHighlight = true;
                } else if (variableToHighlight != null && row.getCommand().contains(variableToHighlight)) {
                    shouldHighlight = true;
                }

                if (shouldHighlight) {
                    setStyle("-fx-background-color: yellow;");
                    if (enableLoadingAnimation) {
                        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), this);
                        ft.setFromValue(1.0);
                        ft.setToValue(0.1);
                        ft.setCycleCount(4);
                        ft.setAutoReverse(true);
                        ft.play();
                    }
                } else {
                    setStyle("");
                }
            }
        });
    }

    public void highlightByLabel(String label) {
        this.labelToHighlight = label;
        this.variableToHighlight = null;
        initRowFactory();
        instructionTable.refresh();
    }

    public void highlightByVariable(String variable) {
        this.variableToHighlight = variable;
        this.labelToHighlight = null;
        initRowFactory();
        instructionTable.refresh();
    }

    public void clearHighlight(boolean enableAnimation) {
        this.labelToHighlight = null;
        this.variableToHighlight = null;
        this.enableLoadingAnimation = enableAnimation;
        initRowFactory();
        instructionTable.refresh();
    }

    public String showChoicePopup(List<String> choices, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/highlightSelectionPopup/highlight_choice_popup.fxml"));
        Parent root = loader.load();

        HighlightChoiceController controller = loader.getController();
        controller.setChoices(choices);

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        return controller.getSelectedChoice();
    }
}
