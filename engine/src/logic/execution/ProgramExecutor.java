package logic.execution;

import logic.Variable.Variable;
import logic.label.Label;

import java.util.Map;
import java.util.Set;

public interface ProgramExecutor {
    long run(ExecutionContext context);
    Map<Variable, Long> getVariableState();
    //void addActiveVariable(Variable variable);
    //void addActiveLabel(Label label) ;
    //Set<Variable> getActiveVariables();
    //Set<Label> getActiveLabels();
}
