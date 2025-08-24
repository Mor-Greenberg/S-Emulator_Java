package programDisplay;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.instruction.DecreaseInstruction;
import logic.instruction.IncreaseInstruction;
import logic.instruction.InstructionData;
import logic.instruction.SInstruction;
import logic.label.Label;

import java.util.ArrayList;
import java.util.List;

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
        String labelFormatted = String.format("[%-5s]", label != null ? label.toString() : "");

        String output = String.format("#%d (%s) %s %s (%d)", instructionNumber, instructionType, labelFormatted, commandDisplay, cycles);
        System.out.println(output);

    }


}
