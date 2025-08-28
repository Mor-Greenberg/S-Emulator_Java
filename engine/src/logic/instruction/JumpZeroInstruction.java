package logic.instruction;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.label.LabelImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JumpZeroInstruction extends AbstractInstruction {
    private Label JZLabel;
    public InstructionType type = InstructionType.S;


    public JumpZeroInstruction(Variable variable, Label zLabel) {
        this(variable, zLabel, FixedLabel.EMPTY);
        this.degree = 2;
    }

    public JumpZeroInstruction(Variable variable, Label zLabel, Label label) {
        super(InstructionData.JUMP_NOT_ZERO, variable, label);
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
    public static void main(String[] args) {
        // יצירת משתנה עם ערך 0
        Variable x = new VariableImpl(VariableType.INPUT, 1);
        Map<Variable, Long> vars = new HashMap<>();
        ExecutionContext context = new ExecutionContextImpl(vars);
        context.updateVariable(x, 0);  // שימי לב: ערך 0

        // תווית ליעד הקפיצה
        Label targetLabel = new LabelImpl(42); // לדוגמה L42

        // יצירת פקודת JumpZero
        JumpZeroInstruction jzInstr = new JumpZeroInstruction(x, targetLabel);

        // הפעלת הפקודה
        Label result = jzInstr.execute(context);

        // הדפסת תוצאה
        System.out.println("Variable x = " + context.getVariableValue(x));
        System.out.println("Command: " + jzInstr.commandDisplay());
        System.out.println("Jump result: " + result);  // אמור להיות L42
    }



}
