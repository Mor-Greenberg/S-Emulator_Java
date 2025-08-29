package logic.execution;

import logic.Variable.Variable;
import logic.instruction.Instruction;
import logic.label.Label;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ExecutionContext {

    long getVariableValue(Variable v);
    void updateVariable(Variable v, long value);
     Label findAvailableLabel();
     Variable findAvailableVariable();

     List<Instruction> getActivetedInstructions();

     void addActivetedInstruction(Instruction instruction);
}
