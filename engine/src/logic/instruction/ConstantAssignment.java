package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.Label;

public class ConstantAssignment extends AbstractInstruction {

    InstructionType type = InstructionType.S;

    //TODO: understand where K comes from
    int K=0;
    public ConstantAssignment(Variable variable) {
        super(InstructionData.CONSTANT_ASSIGNMENT, variable);
    }

    public ConstantAssignment(Variable variable, Label label) {
        super(InstructionData.CONSTANT_ASSIGNMENT, variable, label);
    }
    @Override
    public Label execute(ExecutionContext context) { // TODO
        return null;
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = variable.toString() +"<-" + K;
        return output;
    }
}
