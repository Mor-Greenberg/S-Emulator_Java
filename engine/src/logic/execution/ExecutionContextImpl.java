package logic.execution;

import logic.Variable.Variable;
import logic.label.Label;
import logic.label.LabelImpl;

import java.util.*;

public class ExecutionContextImpl implements ExecutionContext {
    public Map<Variable, Long> variableState;
    public Set<Label> labels;
    public ExecutionContextImpl(Map<Variable, Long> variableState) {
        this.variableState = variableState;
        this.labels =  new HashSet<>();
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
}
