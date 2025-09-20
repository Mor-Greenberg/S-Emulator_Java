package gui.highlightSelectionPopup;

import gui.instructionTable.InstructionRow;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
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




}
