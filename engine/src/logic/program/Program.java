package logic.program;

import logic.Variable.Variable;
import logic.instruction.Instruction;
import logic.label.Label;

import java.util.List;
import java.util.Set;

public interface Program {
    String getName();
    void addInstruction(Instruction instruction);
    List<Instruction> getInstructions();
    Set<Variable> getVars();
    Set<Label> getLabels();
    int getNextIndexByLabel(Label nextLabel);
    void addVar(Variable variable);
     void addLabel(Label label) ;

        boolean validate();
    int calculateMaxDegree();
    int calculateCycles();
}
