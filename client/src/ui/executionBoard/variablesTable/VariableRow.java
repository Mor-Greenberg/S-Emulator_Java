package ui.executionBoard.variablesTable;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
public class VariableRow {
    private final SimpleStringProperty name;
    private final SimpleLongProperty value;

    public VariableRow(String name, long value) {
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleLongProperty(value);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleLongProperty valueProperty() {
        return value;
    }
}
