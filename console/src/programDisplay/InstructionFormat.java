package programDisplay;

import logic.label.Label;

public class InstructionFormat {
    int instructionNumber;
    String instructionType;
    Label label;
    String commandDisplay;
    int cycles;
    InstructionFormat(int instructionNumber, String instructionType, Label label, String commandDisplay, int cycles) {
        this.instructionNumber = instructionNumber;
        this.instructionType = instructionType;
        this.label = label;
        this.commandDisplay = commandDisplay;
        this.cycles = cycles;
    }
    public void printInstruction() {
        String output = String.format("#%d (%s) [ %s] %s (%d)", instructionNumber, instructionType, label, commandDisplay, cycles);
        System.out.println(output);

    }
}
