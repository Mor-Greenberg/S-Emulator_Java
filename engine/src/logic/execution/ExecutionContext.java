package logic.execution;

import logic.Variable.Variable;

import java.util.HashMap;
import java.util.Map;

public interface ExecutionContext {

    long getVariableValue(Variable v);
    void updateVariable(Variable v, long value);
}
