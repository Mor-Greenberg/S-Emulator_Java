package logic.execution;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.instruction.Instruction;
import logic.label.Label;
import logic.label.LabelImpl;

import java.util.*;

public class ExecutionContextImpl implements ExecutionContext {
    public Map<Variable, Long> variableState;
    public Set<Label> labels;
    public List<Instruction> activetedInstructions;
    public ExecutionContextImpl(Map<Variable, Long> variableState) {
        this.variableState = variableState;
        this.labels =  new HashSet<>();
        this.activetedInstructions = new ArrayList<>();
    }


    @Override
    public long getVariableValue(Variable v) {
        return variableState.getOrDefault(v, 0L);
    }

    @Override
    public void updateVariable(Variable v, long value) {
        variableState.put(v, value);
    }
    @Override
    public Label findAvailableLabel(){
        int index = 1;
        Label newLabel;
        do {
            newLabel = new LabelImpl(index);
            index++;
        } while (labels.contains(newLabel));
        return newLabel;
    }
    @Override
    public Variable findAvailableVariable() {
        int index = 1;
        Variable candidate;

        do {
            candidate = new VariableImpl(VariableType.WORK,index);
            index++;
        } while (variableState.containsKey(candidate));

        variableState.put(candidate,0L);
        return candidate;
    }
    @Override
    public List<Instruction> getActivetedInstructions() {
        return activetedInstructions;
    }
    @Override
    public void addActivetedInstruction(Instruction instruction) {
        activetedInstructions.add(instruction);
    }
    @Override
    public Map<Variable, Long> getVariableState() {
        return variableState;
    }



}
