package logic.execution;

import logic.Variable.Variable;

import java.util.Map;

public interface ProgramExecutor {
    long run(ExecutionContext context);
    Map<Variable, Long> getVariableState();
}
