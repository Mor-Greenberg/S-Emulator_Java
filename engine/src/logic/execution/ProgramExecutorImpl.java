package logic.execution;

import logic.Variable.Variable;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;

import java.util.HashMap;
import java.util.Map;

public class ProgramExecutorImpl implements ProgramExecutor {
    private final Program program;

    private Map<Variable, Long> variableState;
    public ProgramExecutorImpl(Program program) {
        this.program = program;
        this.variableState = new HashMap<Variable, Long>();
    }

    @Override
    public long run(Long... input) {
        int pc=0;

        ExecutionContext context = new ExecutionContextImpl();// create the context with inputs.
//        List<Variable> inputVars = program.getInputVariables(); // אם קיים
//        for (int i = 0; i < input.length; i++) {
//            context.updateVariable(inputVars.get(i), input[i]);
//        }

        Label nextLabel;
        do {
            Instruction currentInstruction=program.getInstructions().get(pc);
            nextLabel = currentInstruction.execute(context);

            if (nextLabel == FixedLabel.EMPTY) {
                pc++;
            } else if (nextLabel != FixedLabel.EXIT) {
                pc = program.getNextIndexByLabel(nextLabel); // נדרש מימוש
            }
        } while (nextLabel != FixedLabel.EXIT);

        return context.getVariableValue(Variable.RESULT);
    }

    @Override
    public Map<Variable, Long> getVariableState(){
        return variableState;
    }




}
