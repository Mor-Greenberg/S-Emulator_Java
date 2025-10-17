package ui.executionBoard.instructionTable;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import logic.instruction.Instruction;
import logic.label.FixedLabel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class InstructionRow {
    private final SimpleIntegerProperty number;
    private final SimpleStringProperty type;
    private final SimpleStringProperty label;
    private final SimpleStringProperty command;
    private final SimpleIntegerProperty cycles;
    private final SimpleStringProperty architecture;



    public InstructionRow(int number, String type, String label, String command, int cycles, String architecture) {
        this.number = new SimpleIntegerProperty(number);
        this.type = new SimpleStringProperty(type);
        this.label = new SimpleStringProperty(label);
        this.command = new SimpleStringProperty(command);
        this.cycles = new SimpleIntegerProperty(cycles);
        this.architecture = new SimpleStringProperty(architecture);
    }

    public int getNumber() { return number.get(); }
    public String getType() { return type.get(); }
    public String getLabel() { return label.get(); }
    public String getCommand() { return command.get(); }
    public int getCycles() { return cycles.get(); }
    public String getArchitecture(){ return architecture.get(); }




    public static List<String> getAllLabels(List<InstructionRow> rows) {
        return rows.stream()
                .map(InstructionRow::getLabel)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }


    public static List<String> getAllVariables(List<InstructionRow> rows) {
        Set<String> keywords = Set.of("IF", "GOTO", "EXIT"); // מילים שמורות
        return rows.stream()
                .map(InstructionRow::getCommand)
                .flatMap(cmd -> Arrays.stream(cmd.split("[^a-zA-Z0-9_]")))
                .filter(s -> s.matches("[a-zA-Z_][a-zA-Z0-9_]*"))
                .filter(s -> !keywords.contains(s))
                .filter(s -> !s.matches("L\\d+"))
                .distinct()
                .collect(Collectors.toList());
    }
    public static Instruction findInstructionFromRow(InstructionRow row, List<Instruction> originalInstructions) {
        return originalInstructions.stream()
                .filter(instr -> {
                    String type = instr.getType().toString();
                    String label = (instr.getLabel() != null && !instr.getLabel().equals(FixedLabel.EMPTY)) ? instr.getLabel().toString() : "";
                    String command = instr.commandDisplay();
                    int cycles = instr.getCycles();

                    return row.getType().equals(type)
                            && row.getLabel().equals(label)
                            && row.getCommand().equals(command)
                            && row.getCycles() == cycles;
                })
                .findFirst()
                .orElse(null);
    }



}

