package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

public class IncreaseInstruction extends AbstractInstruction {

    public IncreaseInstruction(Variable variable) {
        super(InstructionData.INCREASE, variable,InstructionType.B);
    }

    public IncreaseInstruction(Variable variable, Label label) {
        super(InstructionData.INCREASE, variable, label,InstructionType.B);
    }

    @Override
    public Label execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getVariable());
        variableValue++;
        context.updateVariable(getVariable(), variableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = variable.toString() +"<-" + variable.toString() + "+1";
        return output;
    }
    @Override
    public AbstractInstruction clone() {
        return new IncreaseInstruction(this.getVariable(),this.getLabel());
    }
    @Override
    public void replaceVariables(java.util.Map<String, Variable> variableMap) {
        String varName = getVariable().getRepresentation();
        if (variableMap.containsKey(varName)) {
            Variable newVar = variableMap.get(varName);
            IncreaseInstruction replaced = new IncreaseInstruction(newVar, this.getLabel());
            replaced.setDegree(this.getDegree());
            replaced.setUniqueId(this.getUniqueId());
            replaced.setOrigin(this.getOrigin());

            this.setLabel(replaced.getLabel());
            this.type = replaced.getType();

        }
    }


}
