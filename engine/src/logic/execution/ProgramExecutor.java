package logic.execution;

import logic.Variable.Variable;

import java.util.Map;

public interface ProgramExecutor {
    long run(Long... input);
    Map<Variable, Long> getVariableState();
}
