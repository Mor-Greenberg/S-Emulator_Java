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

public class ConstantAssignmentInstruction extends AbstractInstruction {

    InstructionType type = InstructionType.S;
    private final int constantValue;


    public ConstantAssignmentInstruction(Variable variable, int constantValue) {
        super(InstructionData.CONSTANT_ASSIGNMENT, variable);
        this.constantValue = constantValue;
        this.degree = 2;
    }

    public ConstantAssignmentInstruction(Variable variable, Label label ,  int constantValue) {
        super(InstructionData.CONSTANT_ASSIGNMENT, variable, label);
        this.constantValue = constantValue;
        this.degree = 2;
    }
    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable(), 0);
        return FixedLabel.EMPTY;
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = variable.toString() +"<-" + constantValue;
        return output;
    }

    public List<AbstractInstruction> expand(ExecutionContext context) {
        List<AbstractInstruction> result = new ArrayList<>();

        Variable v = getVariable();

        AbstractInstruction zero = new ZeroVariableInstruction(v);
        if (getLabel() != FixedLabel.EMPTY) {
            zero.setLabel(getLabel());
            this.setLabel(FixedLabel.EMPTY);
        }
        markAsDerivedFrom(zero, this);
        result.add(zero);

        for (int i = 0; i < constantValue; i++) {
            AbstractInstruction inc = new IncreaseInstruction(v);
            markAsDerivedFrom(inc, this);
            result.add(inc);
        }

        return result;
    }

    public static void main(String[] args) {
        // 1. יצירת משתנה וקונטקסט
        Variable z1 = new VariableImpl(VariableType.WORK, 1);
        Map<Variable, Long> vars = new HashMap<>();
        ExecutionContext context = new ExecutionContextImpl(vars);

        // נניח שהערך ההתחלתי של z1 הוא 2
        context.updateVariable(z1, 8);

        // הדפסת ערך התחלתי
        System.out.println("🟡 Initial value:");
        System.out.println(z1.getRepresentation() + " = " + context.getVariableValue(z1));

        // 2. יצירת הפקודה עם הערך הרצוי
        ConstantAssignmentInstruction instr = new ConstantAssignmentInstruction(z1, 5);

        // 3. הפעלת expand
        List<AbstractInstruction> expanded = instr.expand(context);

        // 4. הרצה מדומה של הפקודות
        System.out.println("\n🚀 Executing...\n");
        for (AbstractInstruction ai : expanded) {
            ai.execute(context);
        }

        // 5. הדפסת מצב סופי
        System.out.println("📦 Final state:");
        System.out.println(z1.getRepresentation() + " = " + context.getVariableValue(z1));
    }



}
