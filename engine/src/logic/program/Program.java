package logic.program;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
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

    public Program expandToDegree(int maxDegree);
     void expandToDegree(int maxDegree, ExecutionContext context) ;
        //Variable getFreshWorkVariable(Set<Variable> activeVariables);
        public Program expandOnce();
     boolean hasSyntheticInstructions();
    public List<Instruction> getActiveInstructions();
        boolean validate();
    int calculateMaxDegree();
    int calculateCycles();
}
