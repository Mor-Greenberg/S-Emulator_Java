package logic.instruction;

import logic.Variable.Variable;

import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpZeroInstruction extends AbstractInstruction {
    private Label JZLabel;


    public JumpZeroInstruction(Variable variable, Label zLabel) {
        this(variable, zLabel, FixedLabel.EMPTY);
        this.degree = 2;
    }

    public JumpZeroInstruction(Variable variable, Label zLabel, Label label) {
        super(InstructionData.JUMP_NOT_ZERO, variable, label,InstructionType.S);
        this.JZLabel = zLabel;
        this.degree = 2;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long value = context.getVariableValue(getVariable());
        if (value == 0) {
            return JZLabel;
        } else {
            return FixedLabel.EMPTY;
        }
    }


    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "IF " + variable.toString() + " = 0 GOTO " + JZLabel.toString();
        return output;
    }

    public List<AbstractInstruction> expand(ExecutionContext context) {
        List<AbstractInstruction> result = new ArrayList<>();

         Variable v = getVariable();
        Label myLabel = getLabel();
        Label jzTarget = this.JZLabel;  //  v == 0
        Label nextLabel = context.findAvailableLabel();  //  L1

        // 1. IF v ≠ 0 GOTO L1
        AbstractInstruction jumpNotZero = new JumpNotZeroInstruction(v, nextLabel);
        if (myLabel != FixedLabel.EMPTY) {
            jumpNotZero.setLabel(myLabel);
            this.setLabel(FixedLabel.EMPTY);
        }
        markAsDerivedFrom(jumpNotZero, this);
        result.add(jumpNotZero);

        // 2. GOTO jzTarget (אם v == 0)
        AbstractInstruction unconditionalJump = new GoToLabelInstruction(v,jzTarget);
        markAsDerivedFrom(unconditionalJump, this);
        result.add(unconditionalJump);

        // 3. L1: y ← y
        Variable dummy = v;
        AbstractInstruction noOp = new NoOpInstruction(dummy);
        noOp.setLabel(nextLabel);
        markAsDerivedFrom(noOp, this);
        result.add(noOp);

        return result;
    }
    public Label getTargetLabel() {
        return JZLabel;
    }

    @Override
    public AbstractInstruction clone() {
        return new JumpZeroInstruction(getVariable(), getTargetLabel(),getLabel());
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
        if (this.JZLabel.equals(from)) {
            this.JZLabel = to;
        }
    }

    @Override
    public boolean jumpsTo(Label label) {
        return this.JZLabel.equals(label);
    }

    public Label getJZLabel() {
        return JZLabel;
    }

}
