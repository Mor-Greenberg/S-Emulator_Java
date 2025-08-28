package logic.instruction;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.program.Program;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoToLabelInstruction extends AbstractInstruction {

    private Label goToLabel;
    public InstructionType type = InstructionType.S;


    public GoToLabelInstruction(Variable variable, Label goToLabel) {
        this(variable, goToLabel, FixedLabel.EMPTY);
        this.degree=1;
    }

    public GoToLabelInstruction(Variable variable, Label goToLabel, Label label) {
        super(InstructionData.GOTO_LABEL, variable, label);
        this.goToLabel = goToLabel;
        this.degree=1;
    }
    public Label getGotoLabel() {
        return goToLabel;
    }
    @Override
    public InstructionType getType() {
        return type;
    }
    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "GOTO " + goToLabel.toString();
        return output;
    }
    public List<AbstractInstruction> expand(ExecutionContext context) {
        // 1. צור משתנה עזר חדש z
        Variable z = context.findAvailableVariable();

        // 2. צור את שתי הפקודות
        AbstractInstruction inc = new IncreaseInstruction(z);
        AbstractInstruction jnz = new JumpNotZeroInstruction(z, goToLabel);

        // 3. אם יש תווית לפקודת המקור – שים אותה על הראשונה
        if (getLabel() != FixedLabel.EMPTY) {
            inc.setLabel(getLabel());
            this.setLabel(FixedLabel.EMPTY);
        }

        // 4. קשר בין הפקודה המקורית לפקודות החדשות
        markAsDerivedFrom(inc, this);
        markAsDerivedFrom(jnz, this);

        // 5. החזר את הפקודות
        return Arrays.asList(inc, jnz);
    }

    public static void main(String[] args) {
        // 1. צור ExecutionContext עם מפה ריקה
        Map<Variable, Long> map = new HashMap<>();
        ExecutionContextImpl context = new ExecutionContextImpl(map);

        // 2. צור תווית יעד לקפיצה
        Label targetLabel = new LabelImpl(1);

        // 3. צור פקודת GoToLabelInstruction (x סתם משתנה placeholder לצורך הבנאי)
        Variable dummy = new VariableImpl(VariableType.INPUT, 1);
        GoToLabelInstruction goTo = new GoToLabelInstruction(dummy, targetLabel);

        // 4. בצע expand
        List<AbstractInstruction> expanded = goTo.expand(context);

        // 5. הדפס את הפקודות שהתקבלו
        System.out.println("Expanded instructions:");
        for (AbstractInstruction instr : expanded) {
            System.out.println(instr.commandDisplay() + "  |  Label: " + instr.getLabel());
        }
    }
}
