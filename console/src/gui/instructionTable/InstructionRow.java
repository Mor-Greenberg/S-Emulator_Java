package gui.instructionTable;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class InstructionRow {
    private final SimpleIntegerProperty number;
    private final SimpleStringProperty type;
    private final SimpleStringProperty label;
    private final SimpleStringProperty command;
    private final SimpleIntegerProperty cycles;

    public InstructionRow(int number, String type, String label, String command, int cycles) {
        this.number = new SimpleIntegerProperty(number);
        this.type = new SimpleStringProperty(type);
        this.label = new SimpleStringProperty(label);
        this.command = new SimpleStringProperty(command);
        this.cycles = new SimpleIntegerProperty(cycles);
    }

    public int getNumber() { return number.get(); }
    public String getType() { return type.get(); }
    public String getLabel() { return label.get(); }
    public String getCommand() { return command.get(); }
    public int getCycles() { return cycles.get(); }

}

