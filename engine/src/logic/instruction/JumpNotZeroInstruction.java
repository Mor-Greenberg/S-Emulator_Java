package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.Map;

public class JumpNotZeroInstruction extends AbstractInstruction{
    private Label jnzLabel;


    public JumpNotZeroInstruction(Variable variable, Label jnzLabel) {
        this(variable, jnzLabel, FixedLabel.EMPTY);
    }

    public JumpNotZeroInstruction(Variable variable, Label jnzLabel, Label label) {
        super(InstructionData.JUMP_NOT_ZERO, variable, label,InstructionType.B);
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
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "IF " + variable.toString() + " != 0 GOTO " + jnzLabel.toString();
        return output;
    }
    public Label getTargetLabel() {
        return jnzLabel;
    }
    @Override
    public AbstractInstruction clone() {
        return new JumpNotZeroInstruction(getVariable(), getTargetLabel(), getLabel());
    }
    @Override
    public void replaceVariables(Map<String, Variable> variableMap) {
        String varName = getVariable().getRepresentation();
        if (variableMap.containsKey(varName)) {
            this.setVariable(variableMap.get(varName));
        }
    }

    @Override
    public void replaceJumpLabel(Label from, Label to) {
        if (this.jnzLabel.equals(from)) {
            this.jnzLabel = to;
        }
    }

    @Override
    public boolean jumpsTo(Label label) {
        return this.jnzLabel.equals(label);
    }

    public Label getJnzLabel() {
        return jnzLabel;
    }


}
