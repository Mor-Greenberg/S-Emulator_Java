package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.Label;

public interface Instruction {
    String getName();
    Label execute(ExecutionContext context);
    int cycles();
    Label getLabel();
    Variable getVariable();
    InstructionType getType();
    String commandDisplay();
}
