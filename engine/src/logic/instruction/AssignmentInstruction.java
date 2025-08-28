package logic.instruction;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentInstruction extends AbstractInstruction {
    InstructionType type = InstructionType.S;
    private final Variable destination; // ← V
    private final Variable source;      // ← V′

    public AssignmentInstruction(Variable destination, Variable source) {
        super(InstructionData.ASSIGNMENT,source);
        this.destination = destination;
        this.source = source;
        this.degree=2;
    }

    public AssignmentInstruction(Label label, Variable destination, Variable source) {
        super(InstructionData.ASSIGNMENT,source, label);
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

    public static void main(String[] args) {
        // 1. הגדר משתנים
        Variable x2 = new VariableImpl(VariableType.INPUT, 2); // V′
        Variable y = new VariableImpl(VariableType.RESULT);   // V

        // 2. צור מפה עם ערך התחלתי ל־x2
        Map<Variable, Long> state = new HashMap<>();
        state.put(x2, 3L); // נניח שהערך המקורי הוא 3

        // 3. צור ExecutionContext
        ExecutionContextImpl context = new ExecutionContextImpl(state);

        // 4. צור את פקודת AssignmentInstruction (y ← x2)
        AssignmentInstruction instr = new AssignmentInstruction(y, x2);

        // 5. קרא ל-expand
        List<AbstractInstruction> expanded = instr.expand(context);

        // 6. הדפס את הפקודות שהתקבלו
        System.out.println("🔧 Expanded instructions:");
        for (AbstractInstruction ai : expanded) {
            System.out.println((ai.getLabel().equals(FixedLabel.EMPTY) ? "" : ai.getLabel() + ": ") +
                    ai.commandDisplay());
        }

        // 7. הרץ את כל הפקודות
        System.out.println("\n🚀 Executing...");
        for (AbstractInstruction ai : expanded) {
            ai.execute(context);
        }

        // 8. הדפס מצב משתנים סופי
        System.out.println("\n📦 Final state:");
        for (Map.Entry<Variable, Long> entry : context.variableState.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

}
