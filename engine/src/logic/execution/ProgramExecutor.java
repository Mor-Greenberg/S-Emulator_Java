package logic.execution;

import logic.Variable.Variable;
import logic.label.Label;
import logic.program.Program;

import java.util.Map;
import java.util.Set;

public interface ProgramExecutor {
    long run(ExecutionContext context);
    Map<Variable, Long> getVariableState();
    Program getProgram();


    }
