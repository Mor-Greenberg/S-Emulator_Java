package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

public class JumpEqualConstant extends AbstractInstruction {

    private Label JEConstantLabel;
    public InstructionType type = InstructionType.S;

    long constantValue= 0 ;

    public JumpEqualConstant(Variable variable, Label JEConstantLabel) {
        this(variable, JEConstantLabel, FixedLabel.EMPTY);
    }

    public JumpEqualConstant(Variable variable, Label JEConstantLabel, Label label) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, variable, label);
        this.JEConstantLabel = JEConstantLabel;
    }
    @Override
    public Label execute(ExecutionContext context) {
        return null;
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "IF " + variable.toString() + " = " + constantValue+ "GOTO"+JEConstantLabel.toString();
        return output;
    }
}
