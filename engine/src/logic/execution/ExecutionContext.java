package logic.execution;

import logic.Variable.Variable;

import java.util.HashMap;
import java.util.Map;

public interface ExecutionContext {

    Map<Variable, Long> variableState= new HashMap<Variable,Long>();

    static Map<Variable, Long> getVariableState() {
        return variableState;
    }

    long getVariableValue(Variable v);
    void updateVariable(Variable v, long value);
}
