package logic.execution;

import logic.Variable.Variable;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;

import java.util.*;

public class ProgramExecutorImpl implements ProgramExecutor {
    private final Program program;
    public List <Instruction> instructionsActivated;

//    public Set<Label> activeLabels;
//    public Set<Variable> activeVariables;


    private Map<Variable, Long> variableState;
    public ProgramExecutorImpl(Program program) {
        this.program = program;
        this.variableState = new HashMap<Variable, Long>();
        this.instructionsActivated = new ArrayList<Instruction>();
//        activeLabels = new HashSet<>();
//        activeVariables = new HashSet<>();
    }
    private void initVarsInMap(){
        for (Variable var : program.getVars()){
            variableState.put(var,0L);
        }
    }
    public List <Instruction> getInstructionsActivated(){
        return instructionsActivated;
    }


    public long run(ExecutionContext context) {

        int pc = 0;

        Label nextLabel = FixedLabel.EMPTY;
        boolean b = (nextLabel != FixedLabel.EXIT && (pc < program.getInstructions().size()));
        while (nextLabel != FixedLabel.EXIT && (pc < program.getInstructions().size())) {
            Instruction currentInstruction = program.getInstructions().get(pc);
            instructionsActivated.add(currentInstruction);

            nextLabel = currentInstruction.execute(context);
            if (nextLabel == FixedLabel.EMPTY) {
                pc++;
            } else if (nextLabel != FixedLabel.EXIT) {
                pc = program.getNextIndexByLabel(nextLabel);
            }
        }


        return context.getVariableValue(Variable.RESULT);
    }


    @Override
    public Map<Variable, Long> getVariableState(){
        return variableState;
    }
//    @Override
//    public void addActiveVariable(Variable variable) {
//        this.activeVariables.add(variable);
//    }
//    @Override
//    public void addActiveLabel(Label label) {
//        this.activeLabels.add(label);
//    }
//
//    @Override
//    public Set<Variable> getActiveVariables() {
//        return activeVariables;
//    }
//
//    @Override
//    public Set<Label> getActiveLabels() {
//        return activeLabels;
//    }




}
