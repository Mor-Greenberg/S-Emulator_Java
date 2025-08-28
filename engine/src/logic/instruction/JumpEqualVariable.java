package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

public class JumpEqualVariable extends AbstractInstruction{
    private Label JEVariableLabel;
    public InstructionType type = InstructionType.S;
    private Variable variableName;


    public JumpEqualVariable(Variable variable, Label JEVariableLabel) {
        this(variable, JEVariableLabel, FixedLabel.EMPTY);
    }

    public JumpEqualVariable(Variable variable, Label JEVariableLabel, Label label) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, variable, label);
        this.JEVariableLabel = JEVariableLabel;
    }

    @Override
    public Label execute(ExecutionContext context) { //TODO
        return null;
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "IF " + variable.toString() + " = "+ variableName.getRepresentation()+ "GOTO "+ JEVariableLabel.toString();
        return output;
    }
}
