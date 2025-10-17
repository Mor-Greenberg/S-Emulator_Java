package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.Label;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public interface Instruction {
      int getDegree();
    String getName();
    Label execute(ExecutionContext context);
    int getCycles();
    Label getLabel();
    Variable getVariable();
    InstructionType getType();
    String commandDisplay();
     boolean hasOrigin();
     AbstractInstruction getOrigin();

    List<AbstractInstruction> expand(ExecutionContext context);
     int getUniqueId();
     void  setUniqueId (int id);
     AbstractInstruction clone();

    void replaceVariables(Map<String, Variable> variableMap);
    boolean jumpsTo(Label label);
    void replaceJumpLabel(Label from, Label to);

}
