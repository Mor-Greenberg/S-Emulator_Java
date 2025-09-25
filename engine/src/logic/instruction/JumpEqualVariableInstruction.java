package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualVariableInstruction extends AbstractInstruction{
    public Label JEVariableLabel;
    public Variable variableName; // second variable: V′

    public JumpEqualVariableInstruction(Variable v, Variable vPrime, Label JEVariableLabel, Label label) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, v, label,InstructionType.S);
        this.JEVariableLabel = JEVariableLabel;
        this.variableName = vPrime;
    }

    public JumpEqualVariableInstruction(Variable v, Variable vPrime, Label JEVariableLabel) {
        this(v, vPrime, JEVariableLabel, FixedLabel.EMPTY);
    }
    public Label getTargetLabel() {
        return JEVariableLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long v1 = context.getVariableValue(getVariable());      // value of V
        long v2 = context.getVariableValue(variableName);       // value of V'

        if (v1 == v2) {
            return JEVariableLabel; // jump if equal
        }

        return FixedLabel.EMPTY; // no jump
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "IF " + variable.toString() + " = "+ variableName.getRepresentation()+ " GOTO "+ JEVariableLabel.toString();
        return output;
    }

    public List<AbstractInstruction> expand(ExecutionContext context) {
        List<AbstractInstruction> result = new ArrayList<>();

        Variable v1 = getVariable();       // variable V
        Variable v2 = variableName;        // variable V'

        // Allocate temporary variables z1 and z2
        Variable z1 = context.findAvailableVariable();
        Variable z2 = context.findAvailableVariable();

        // Copy v1 → z1, v2 → z2
        result.add(new AssignmentInstruction(z1, v1));
        result.add(new AssignmentInstruction(z2, v2));

        // Create labels: L1 (exit), L2 (loop), L3 (final check)
        Label L1 = context.findAvailableLabel();
        Label L2 = context.findAvailableLabel();
        Label L3 = context.findAvailableLabel();

        // L2: Loop start
        AbstractInstruction loopStart = new NoOpInstruction(z1);
        loopStart.setLabel(L2);
        result.add(loopStart);

        // IF z1 == 0 → GOTO L3
        result.add(new JumpZeroInstruction(z1, L3));

        // IF z2 == 0 → GOTO L1
        result.add(new JumpZeroInstruction(z2, L1));

        // z1 ← z1 - 1, z2 ← z2 - 1
        result.add(new DecreaseInstruction(z1));
        result.add(new DecreaseInstruction(z2));

        // GOTO L2
        result.add(new GoToLabelInstruction(z1, L2));

        // L3: Final check: IF z2 == 0 → GOTO JEVariableLabel
        AbstractInstruction finalCheck = new JumpZeroInstruction(z2, JEVariableLabel);
        finalCheck.setLabel(L3);
        result.add(finalCheck);

        // L1: No-op exit
        AbstractInstruction exit = new NoOpInstruction(z1);
        exit.setLabel(L1);
        result.add(exit);

        for (AbstractInstruction instr : result) {
            markAsDerivedFrom(instr, this);
        }


        return result;
    }
    private Variable getPrimeVar(){
        return variableName;
    }

    @Override
    public AbstractInstruction clone() {
        return new JumpEqualVariableInstruction(this.getVariable(),this.getPrimeVar(),this.JEVariableLabel,this.getLabel());
    }

    @Override
    public void replaceVariables(Map<String, Variable> variableMap) {
        String v1Name = getVariable().getRepresentation();
        String v2Name = variableName.getRepresentation();

        if (variableMap.containsKey(v1Name)) {
            this.setVariable(variableMap.get(v1Name));
        }

        if (variableMap.containsKey(v2Name)) {
            this.variableName = variableMap.get(v2Name);
        }
    }
    @Override
    public void replaceJumpLabel(Label from, Label to) {
        if (this.JEVariableLabel.equals(from)) {
            this.JEVariableLabel = to;
        }
    }

    @Override
    public boolean jumpsTo(Label label) {
        return this.JEVariableLabel.equals(label);
    }




}
