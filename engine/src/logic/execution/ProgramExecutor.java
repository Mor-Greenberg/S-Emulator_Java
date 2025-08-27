package logic.execution;

import logic.Variable.Variable;

import java.util.Map;

public interface ProgramExecutor {
    long run();
    Map<Variable, Long> getVariableState();
}
