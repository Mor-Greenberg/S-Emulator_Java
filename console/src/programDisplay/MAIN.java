package programDisplay;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.instruction.DecreaseInstruction;
import logic.instruction.IncreaseInstruction;
import logic.instruction.Instruction;
import logic.label.LabelImpl;
import logic.program.ProgramImpl;

import java.util.ArrayList;
import java.util.List;

import static logic.label.Label.labels;

public class MAIN {
    public static void main(String[] args) {
        Variable var = new VariableImpl(VariableType.INPUT, 1);


        // יצירת הוראות
        List<Instruction> instructions = new ArrayList<>();
        instructions.add(new IncreaseInstruction(var,new LabelImpl(1)));
        instructions.add(new DecreaseInstruction(var));




        ProgramImpl prog = new ProgramImpl("MyTestProgram", List.of(var), labels);

        programDisplayImpl display = new programDisplayImpl(prog);

        display.printProgram();

    }
}
