package logic.program;

import logic.Variable.Variable;
import logic.instruction.Instruction;
import logic.label.Label;

import java.util.List;

public interface Program {
    String getName();
    void addInstruction(Instruction instruction);
    List<Instruction> getInstructions();
    List<Variable> getVars();
    List<Label> getLabels();
    int getNextIndexByLabel(Label nextLabel);

        boolean validate();
    int calculateMaxDegree();
    int calculateCycles();
}
