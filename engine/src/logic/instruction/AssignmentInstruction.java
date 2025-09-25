package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssignmentInstruction extends AbstractInstruction {
    private Variable destination; // ← V
    private Variable source;      // ← V′

    public AssignmentInstruction(Variable destination, Variable source) {
        super(InstructionData.ASSIGNMENT,source,InstructionType.S);
        this.destination = destination;
        this.source = source;
        this.degree=2;
    }

    public AssignmentInstruction(Label label, Variable destination, Variable source) {
        super(InstructionData.ASSIGNMENT,source, label,InstructionType.S);
        this.destination = destination;
        this.source = source;
        this.degree=2;
    }
    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = destination.toString() + " <- " + source.toString();
        return output;
    }

    @Override
    public List<AbstractInstruction> expand(ExecutionContext context) {
        List<AbstractInstruction> result = new ArrayList<>();

        Variable v = this.destination;  // V
        Variable vp = this.source;      // V′
        Variable z = context.findAvailableVariable();


        Label L1 = context.findAvailableLabel();
        Label L2 = context.findAvailableLabel();
        Label L3 = context.findAvailableLabel();

        // 1. V ← 0
        AbstractInstruction clearV = new ZeroVariableInstruction(v);

        // 2. IF V′ ≠ 0 → GOTO L1
        AbstractInstruction jnz1 = new JumpNotZeroInstruction(vp, L1);

        // 3. GOTO L3
        AbstractInstruction goToL3 = new GoToLabelInstruction(z, L3);

        // --- L1 ---
        // V′ ← V′ - 1
        AbstractInstruction decVP = new DecreaseInstruction(vp, L1);
        // z ← z + 1
        AbstractInstruction incZ1 = new IncreaseInstruction(z);
        // IF V′ ≠ 0 → GOTO L1
        AbstractInstruction jnz2 = new JumpNotZeroInstruction(vp, L1);

        // --- L2 ---
        // z ← z - 1
        AbstractInstruction decZ = new DecreaseInstruction(z, L2);
        // V ← V + 1
        AbstractInstruction incV = new IncreaseInstruction(v);
        // V′ ← V′ + 1
        AbstractInstruction incVP = new IncreaseInstruction(vp);
        // IF z ≠ 0 → GOTO L2
        AbstractInstruction jnz3 = new JumpNotZeroInstruction(z, L2);

        // --- L3 ---
        AbstractInstruction nop = new NoOpInstruction(destination);


        decVP.setLabel(L1);
        decZ.setLabel(L2);
        nop.setLabel(L3);


        if (getLabel() != FixedLabel.EMPTY) {
            clearV.setLabel(getLabel());
            this.setLabel(FixedLabel.EMPTY);
        }


        AbstractInstruction[] all = {
                clearV, jnz1, goToL3,
                decVP, incZ1, jnz2,
                decZ, incV, incVP, jnz3,
                nop
        };
        for (AbstractInstruction instr : all) {
            markAsDerivedFrom(instr, this);
            result.add(instr);
        }

        return result;
    }
    @Override
    public AbstractInstruction clone() {
        return new AssignmentInstruction(this.getLabel(), this.destination, this.source);
    }


    @Override
    public void replaceVariables(Map<String, Variable> variableMap) {
        if (variableMap.containsKey(destination.toString())) {
            this.destination = variableMap.get(destination.toString());
        }
        if (variableMap.containsKey(source.toString())) {
            this.source = variableMap.get(source.toString());
        }
    }




}
