package ui.executionBoard.instructionTable;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.instruction.AbstractInstruction;

import java.util.List;

public class ExpandedTable {
    public TableView<AbstractInstruction> buildInstructionTableFrom(List<AbstractInstruction> instructions) {
        TableView<AbstractInstruction> table = new TableView<>();
        table.setPrefHeight(150);

        TableColumn<AbstractInstruction, String> idCol = new TableColumn<>("#");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getUniqueId())));

        TableColumn<AbstractInstruction, String> labelCol = new TableColumn<>("Label");
        labelCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLabel() != null ? data.getValue().getLabel().toString() : ""
        ));

        TableColumn<AbstractInstruction, String> instrCol = new TableColumn<>("Instruction");
        instrCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().commandDisplay()));

        TableColumn<AbstractInstruction, String> cyclesCol = new TableColumn<>("Cycles");
        cyclesCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCycles())));

        table.getColumns().addAll(idCol, labelCol, instrCol, cyclesCol);
        table.setItems(FXCollections.observableArrayList(instructions));
        return table;
    }

}
