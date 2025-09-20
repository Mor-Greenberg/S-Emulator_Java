package gui.highlightSelectionPopup;

import gui.instructionTable.InstructionRow;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class HighlightAction {
    private final TableView<InstructionRow> instructionTable;
    private String labelToHighlight = null;
    private String variableToHighlight = null;

    public HighlightAction(TableView<InstructionRow> instructionTable) {
        this.instructionTable = instructionTable;
        initRowFactory();
    }

    private void initRowFactory() {
        instructionTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InstructionRow row, boolean empty) {
                super.updateItem(row, empty);
                if (row == null || empty) {
                    setStyle("");
                } else if (labelToHighlight != null && row.getLabel().equals(labelToHighlight)) {
                    setStyle("-fx-background-color: yellow;");
                } else if (variableToHighlight != null && row.getCommand().contains(variableToHighlight)) {
                    setStyle("-fx-background-color: yellow;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    public void highlightByLabel(String label) {
        instructionTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InstructionRow row, boolean empty) {
                super.updateItem(row, empty);
                if (row == null || empty) {
                    setStyle("");
                    return;
                }

                String labelFromRow = row.getLabel() != null ? row.getLabel().trim() : "";
                String command = row.getCommand() != null ? row.getCommand().trim() : "";

                boolean isLabelDefinition = label.equalsIgnoreCase(labelFromRow);
                boolean isLabelMentioned = Pattern.compile("\\b" + Pattern.quote(label) + "\\b")
                        .matcher(command)
                        .find();

                if (isLabelDefinition || isLabelMentioned) {
                    setStyle("-fx-background-color: yellow;");
                } else {
                    setStyle("");
                }
            }
        });

        instructionTable.refresh();
    }



    public void highlightByVariable(String variable) {
        this.variableToHighlight = variable;
        instructionTable.refresh();
    }


    public void clearHighlight() {
        this.labelToHighlight = null;
        this.variableToHighlight = null;
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
