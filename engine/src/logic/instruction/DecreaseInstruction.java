package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

public class DecreaseInstruction extends AbstractInstruction {
    public InstructionType type = InstructionType.B;

    public DecreaseInstruction(Variable variable) {
        super(InstructionData.DECREASE, variable);
    }

    public DecreaseInstruction(Variable variable, Label label) {
        super(InstructionData.DECREASE, variable, label);
    }

    @Override
    public Label execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getVariable());
        variableValue = Math.max(0, variableValue - 1);
        context.updateVariable(getVariable(), variableValue);

        return FixedLabel.EMPTY;
    }
    @Override
    public InstructionType getType(){
        return type;
    }

    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = variable.toString() +"<-" + variable.toString() + "-1";
        return output;
    }
}
