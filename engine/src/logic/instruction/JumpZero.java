package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.List;

public class JumpZero extends AbstractInstruction {
    private Label zLabel;
    public InstructionType type = InstructionType.S;


    public JumpZero(Variable variable, Label zLabel) {
        this(variable, zLabel, FixedLabel.EMPTY);
    }

    public JumpZero(Variable variable, Label zLabel, Label label) {
        super(InstructionData.JUMP_NOT_ZERO, variable, label);
        this.zLabel = zLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;
    }
//    public List<AbstractInstruction> expand(ExecutionContext context){
//
//    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "IF " + variable.toString() + " = 0 GOTO " + zLabel.toString();
        return output;
    }
}
