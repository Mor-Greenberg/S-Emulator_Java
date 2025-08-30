package programDisplay;

import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.label.Label;

public class InstructionFormat {
    int instructionNumber;
    String instructionType;
    Label label;
    String commandDisplay;
    int cycles;
    InstructionFormat(Instruction instruction) {
        this.instructionNumber = instruction.getUniqueId();
        this.instructionType = String.valueOf(instruction.getType());
        this.label = instruction.getLabel();
        this.commandDisplay = instruction.commandDisplay();
        this.cycles = instruction.getCycles();
    }
    public void printInstruction() {
        String labelFormatted = String.format("[%-5s]", (label != null && label != FixedLabel.EMPTY) ? label.toString() : "");

        String output = String.format("#%d (%s) %s %s (%d)", instructionNumber, instructionType, labelFormatted, commandDisplay, cycles);
        System.out.println(output);

    }


}
