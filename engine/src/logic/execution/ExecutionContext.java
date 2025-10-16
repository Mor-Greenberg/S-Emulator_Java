
package logic.execution;

import logic.Variable.Variable;
import logic.label.Label;
import logic.program.Program;

import java.util.Map;

public interface ExecutionContext {

    void setFunctionMap(Map<String, Program> functionMap);


        boolean addProgram(Program newProgram) ;
        long getVariableValue(Variable v);

    void updateVariable(Variable v, long value);

    Label findAvailableLabel();

    Variable findAvailableVariable();

    Map<Variable, Long> getVariableState();

    Program getProgramMap(String functionName);

    Map<String, Program> getLoadedPrograms();
    void reset();
    }
