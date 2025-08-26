package logic.execution;

import logic.Variable.Variable;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContextImpl implements ExecutionContext {
    public Map<Variable, Long> variableState;

    @Override
    public long getVariableValue(Variable v) {
        return variableState.get(v);
    }

    @Override
    public void updateVariable(Variable v, long value) {
        variableState.put(v, value);
    }
}
