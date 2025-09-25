package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.Arrays;
import java.util.List;

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
        context.updateVariable(getVariable(), 0);
        return FixedLabel.EMPTY;
    }

    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = variable.toString() +"<-" + "0";
        return output;
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

        markAsDerivedFrom(dec, this);
        markAsDerivedFrom(jnz, this);

        return Arrays.asList(dec, jnz);
    }

    @Override
    public AbstractInstruction clone() {
        return new ZeroVariableInstruction(getVariable(),getLabel());
    }

    @Override
    public void replaceVariables(java.util.Map<String, Variable> variableMap) {
        String varName = getVariable().getRepresentation();
        if (variableMap.containsKey(varName)) {
            this.variable = variableMap.get(varName);
        }
    }

}
