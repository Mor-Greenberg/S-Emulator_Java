package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

public class Assignment extends AbstractInstruction {
    InstructionType type = InstructionType.S;
    public Assignment(Variable variable) {
        super(InstructionData.ASSIGNMENT, variable);
    }

    public Assignment(Variable variable, Label label) {
        super(InstructionData.ASSIGNMENT, variable, label);
    }
    @Override
    public Label execute(ExecutionContext context) { // TODO
        return null;
    }
    @Override
    public String commandDisplay(){ // TODO
        Variable variable = getVariable();
        String output = " ";
        return output;
    }
}
