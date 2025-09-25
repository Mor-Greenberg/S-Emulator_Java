package logic.instruction;

import logic.Variable.Variable;

import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstantAssignmentInstruction extends AbstractInstruction {

    private final int constantValue;


    public ConstantAssignmentInstruction(Variable variable, int constantValue) {
        super(InstructionData.CONSTANT_ASSIGNMENT, variable,InstructionType.S);
        this.constantValue = constantValue;
        this.degree = 2;
    }

    public ConstantAssignmentInstruction(Variable variable, Label label ,  int constantValue) {
        super(InstructionData.CONSTANT_ASSIGNMENT, variable, label,InstructionType.S);
        this.constantValue = constantValue;
        this.degree = 2;
    }
    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable(), constantValue);
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

    @Override
    public AbstractInstruction clone() {
        return new ConstantAssignmentInstruction(this.getVariable(),this.getLabel(), constantValue);
    }

    @Override
    public void replaceVariables(Map<String, Variable> variableMap) {
        if (variableMap.containsKey(getVariable().toString())) {
            this.variable = variableMap.get(getVariable().toString());
        }
    }
    public int getConstantValue() {
        return constantValue;
    }


}
