package logic.execution;

import logic.Variable.Variable;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramExecutorImpl implements ProgramExecutor {
    private final Program program;
    public List <Instruction> instructionsActivated;

    private Map<Variable, Long> variableState;
    public ProgramExecutorImpl(Program program) {
        this.program = program;
        this.variableState = new HashMap<Variable, Long>();
        this.instructionsActivated = new ArrayList<Instruction>();
    }
    private void initVarsInMap(){
        for (Variable var : program.getVars()){
            variableState.put(var,0L);
        }
    }
    public List <Instruction> getInstructionsActivated(){
        return instructionsActivated;
    }

    public long run() {

        int pc = 0;
        ExecutionContext context = new ExecutionContextImpl(variableState);

        Label nextLabel = FixedLabel.EMPTY;
        boolean b = nextLabel != FixedLabel.EXIT && (pc == program.getInstructions().size());
        do {
            Instruction currentInstruction = program.getInstructions().get(pc);
            instructionsActivated.add(currentInstruction);


            nextLabel = currentInstruction.execute(context);
            if (nextLabel == FixedLabel.EMPTY) {
                pc++;
            } else if (nextLabel != FixedLabel.EXIT) {
                pc = program.getNextIndexByLabel(nextLabel);
            }
        } while (b);

        return context.getVariableValue(Variable.RESULT);
    }


    @Override
    public Map<Variable, Long> getVariableState(){
        return variableState;
    }




}
