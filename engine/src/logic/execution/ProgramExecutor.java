package logic.execution;

import logic.Variable.Variable;
import logic.label.Label;

import java.util.Map;
import java.util.Set;

public interface ProgramExecutor {
    long run(ExecutionContext context);
    Map<Variable, Long> getVariableState();

}
