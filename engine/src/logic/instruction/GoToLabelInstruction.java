package logic.instruction;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;

import java.util.Arrays;
import java.util.List;

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
    public Label execute(ExecutionContext context) { // TODO in general
        Variable var= new VariableImpl(VariableType.WORK,1); //TODO: list of occupied Zs

        return null;
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "GOTO " + goToLabel.toString();
        return output;
    }
//    @Override
//    public List<AbstractInstruction> expand(ExecutionContext context){
//
//        // 1) קבלת משתנה עזר ייחודי להרחבה זו
//        Variable z =
//
//        // 2) בניית "z <- z + 1"
//
//        // 3) בניית "IF z != 0 GOTO L"
//        JumpNotZeroInstruction jnz = new JumpNotZeroInstruction(z, gotoLabel);
//
//        // --- שימור תוויות לפי הכלל: אם יש תווית על GOTO, היא עוברת לפקודה הראשונה ---
//        // אם incZ כבר דורש תווית משלו במקרי קצה — השתמשי ב"ניוטרל" עם התווית (ראה סעיף 1 בהנחיות)
//        if (!getLabel().isEmpty()) {
//            incZ.setLabel(getLabel());
//            // התווית של הפקודה המקורית "נצרכה", אין צורך לשמור אותה כאן
//            this.setLabel(FixedLabel.EMPTY);
//        }
//
//        // --- היסטוריית מוצא לצורך הצגת <<< ---
//        // אם יש לך מנגנון מקור/אב לפקודות שנוצרו, קשרי אותו כאן
//        markAsDerivedFrom(incZ, this);  // TODO: החליפי במתודת פרויקט (למשל setOrigin)
//        markAsDerivedFrom(jnz, this);   // TODO
//
//        return Arrays.asList(incZ, jnz);
//    }

}
