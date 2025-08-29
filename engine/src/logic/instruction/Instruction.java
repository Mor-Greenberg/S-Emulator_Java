package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.Label;
import logic.program.Program;

import java.util.List;

public interface Instruction {
    String getName();
    Label execute(ExecutionContext context);
    int getCycles();
    Label getLabel();
    Variable getVariable();
    InstructionType getType();
    String commandDisplay();
     boolean hasOrigin();
     AbstractInstruction getOrigin();
     int getDegree();
     List<AbstractInstruction> expand(ExecutionContext context);
}
