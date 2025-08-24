package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

public class NoOpInstruction extends AbstractInstruction {
    public InstructionType type = InstructionType.B;

    public NoOpInstruction(Variable variable) {
        super(InstructionData.NO_OP, variable);
    }

    public NoOpInstruction(Variable variable, Label label) {
        super(InstructionData.NO_OP, variable, label);
    }

    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;

    }
    @Override
    public InstructionType getType(){
        return type;
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = variable.toString() +"<-" + variable.toString();
        return output;
    }
}
