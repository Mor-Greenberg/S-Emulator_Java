package logic.execution;

import logic.Variable.Variable;

import java.util.Map;

public class ExecutionContextImpl implements ExecutionContext {
    public Map<Variable, Long> variableState;
    public ExecutionContextImpl(Map<Variable, Long> variableState) {
        this.variableState = variableState;
    }


    @Override
    public long getVariableValue(Variable v) {
        return variableState.getOrDefault(v, 0L);
    }

    @Override
    public void updateVariable(Variable v, long value) {
        variableState.put(v, value);
    }
}
