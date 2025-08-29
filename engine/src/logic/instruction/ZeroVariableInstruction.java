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


    public ZeroVariableInstruction(Variable variable) {
        super(InstructionData.ZERO_VARIABLE, variable,InstructionType.S);
        this.degree=1;
    }

    public ZeroVariableInstruction(Variable variable, Label label) {
        super(InstructionData.ZERO_VARIABLE, variable, label,InstructionType.S);
        this.degree=1;
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable(), 0);  // ← זה האיפוס החשוב
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


}
