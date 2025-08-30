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


    private Map<Variable, Long> variableState;
    public ProgramExecutorImpl(Program program) {
        this.program = program;
        this.variableState = new HashMap<Variable, Long>();
        this.instructionsActivated = new ArrayList<Instruction>();
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




}
