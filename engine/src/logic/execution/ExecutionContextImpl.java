package logic.execution;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.instruction.Instruction;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.program.Program;

import java.util.*;

public class ExecutionContextImpl implements ExecutionContext {
    public Map<Variable, Long> variableState;
    public Set<Label> labels;
    public List<Instruction> activetedInstructions;
    public Map<String, Program> programMap;
    public ExecutionContextImpl(Map<Variable, Long> variableState,Map<String,Program> programMap) {
        this.variableState = variableState;
        this.labels =  new HashSet<>();
        this.activetedInstructions = new ArrayList<>();
        this.programMap = programMap;

    }

    public Program getProgramMap(String name) {
        return programMap.get(name);
    }


    @Override
    public long getVariableValue(Variable v) {
        return variableState.getOrDefault(v, 0L);
    }

    @Override
    public void updateVariable(Variable v, long value) {
        variableState.put(v, value);
    }
    @Override
    public Label findAvailableLabel(){
        int index = 1;
        Label newLabel;
        do {
            newLabel = new LabelImpl(index);
            index++;
        } while (labels.contains(newLabel));
        return newLabel;
    }
    @Override
    public Variable findAvailableVariable() {
        int index = 1;
        Variable candidate;

        do {
            candidate = new VariableImpl(VariableType.WORK,index);
            index++;
        } while (variableState.containsKey(candidate));

        variableState.put(candidate,0L);
        return candidate;
    }
    @Override
    public Map<Variable, Long> getVariableState() {
        return variableState;
    }


    public void setFunctionMap(Map<String, Program> functionMap) {
        this.programMap = functionMap;
    }
    public void initializeVarsFromProgram(Program program) {
        for (Variable v : program.getVars()) {
            variableState.putIfAbsent(v, 0L);
        }
    }
    public void reset() {
        variableState.clear();
        labels.clear();
        activetedInstructions.clear();
        if (programMap != null) {
            programMap.clear();
        }
    }



}
