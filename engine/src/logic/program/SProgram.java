package logic.program;

import logic.Variable.Variable;
import logic.instruction.SInstruction;
import logic.label.Label;

import java.util.List;

public interface SProgram {
    String getName();
    void addInstruction(SInstruction instruction);
    List<SInstruction> getInstructions();
    List<Variable> getVars();
    List<Label> getLabels();

        boolean validate();
    int calculateMaxDegree();
    int calculateCycles();
}
