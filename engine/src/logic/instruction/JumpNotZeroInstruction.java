package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

public class JumpNotZeroInstruction extends AbstractInstruction{
    private Label jnzLabel;
    public InstructionType type = InstructionType.B;


    public JumpNotZeroInstruction(Variable variable, Label jnzLabel) {
        this(variable, jnzLabel, FixedLabel.EMPTY);
    }

    public JumpNotZeroInstruction(Variable variable, Label jnzLabel, Label label) {
        super(InstructionData.JUMP_NOT_ZERO, variable, label);
        this.jnzLabel = jnzLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());

        if (variableValue != 0) {
            return jnzLabel;
        }
        return FixedLabel.EMPTY;

    }
    @Override
    public InstructionType getType(){
        return type;
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "IF " + variable.toString() + " != 0 GOTO " + jnzLabel.toString();
        return output;
    }

    public void setJnzLabel(Label jnzLabel) {
        this.jnzLabel=jnzLabel;
    }
}
