package logic.instruction;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZeroVariableInstruction extends AbstractInstruction {

    public InstructionType type = InstructionType.S;

    public ZeroVariableInstruction(Variable variable) {
        super(InstructionData.ZERO_VARIABLE, variable);
        this.degree=1;
    }

    public ZeroVariableInstruction(Variable variable, Label label) {
        super(InstructionData.ZERO_VARIABLE, variable, label);
        this.degree=1;
    }

    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;

    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = variable.toString() +"<-" + "0";
        return output;
    }
    @Override
    public InstructionType getType() {
        return InstructionType.S;
    }


    public List<AbstractInstruction> expand(ExecutionContext context) {
        Label loopLabel = context.findAvailableLabel();
        Variable x = this.getVariable();
        AbstractInstruction dec = new DecreaseInstruction(x, loopLabel);
        AbstractInstruction jnz = new JumpNotZeroInstruction(x, loopLabel);
        if (getLabel()!= FixedLabel.EMPTY) {
            dec.setLabel(getLabel());
            this.setLabel(FixedLabel.EMPTY);
        }

        // 6. סימון מקור ל־<<< אם יש לך מנגנון כזה
        markAsDerivedFrom(dec, this);
        markAsDerivedFrom(jnz, this);

        return Arrays.asList(dec, jnz);
    }




        public static void main(String[] args) {
            Map<Variable,Long> map = new HashMap<Variable,Long>();
            // 1. צור משתנה
            Variable x = new VariableImpl(VariableType.INPUT,1);

            // 2. צור הקשר
            ExecutionContext context = new ExecutionContextImpl(map);
            context.updateVariable(x,5);

            // 3. צור את ההוראה
            ZeroVariableInstruction zeroInstr = new ZeroVariableInstruction(x);

            // 4. קרא ל-expand
            List<AbstractInstruction> expanded = zeroInstr.expand(context);

            // 5. הרץ את הפקודות בלולאה עד שהערך של x הוא 0
            Label currentLabel = expanded.get(0).getLabel(); // מתחילים בלייבל הראשון (של decrease)

            while (context.getVariableValue(x) != 0) {
                for (AbstractInstruction instr : expanded) {
                    if (instr.getLabel().equals(currentLabel) || instr.getLabel().equals(FixedLabel.EMPTY)) {
                        System.out.println(instr.commandDisplay() + "  |  Label: " + instr.getLabel());
                        Label next = instr.execute(context);
                        currentLabel = next;
                        break; // רק פקודה אחת לכל צעד
                    }
                }
            }

            System.out.println("x now equals: " + context.getVariableValue(x));
        }






}
