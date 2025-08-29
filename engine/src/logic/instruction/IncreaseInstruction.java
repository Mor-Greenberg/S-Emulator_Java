package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

public class IncreaseInstruction extends AbstractInstruction {

    public IncreaseInstruction(Variable variable) {
        super(InstructionData.INCREASE, variable,InstructionType.B);
    }

    public IncreaseInstruction(Variable variable, Label label) {
        super(InstructionData.INCREASE, variable, label,InstructionType.B);
    }

    @Override
    public Label execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getVariable());
        variableValue++;
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
        String output = variable.toString() +"<-" + variable.toString() + "+1";
        return output;
    }
}
