package programDisplay;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.instruction.DecreaseInstruction;
import logic.instruction.IncreaseInstruction;
import logic.instruction.SInstruction;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.program.SProgramImpl;

import java.util.ArrayList;
import java.util.List;

public class MAIN {
    public static void main(String[] args) {
        Variable var = new VariableImpl(VariableType.INPUT, 1);


        // יצירת הוראות
        List<SInstruction> instructions = new ArrayList<>();
        instructions.add(new IncreaseInstruction(var));
        instructions.add(new DecreaseInstruction(var));

        List<Label> labels = new ArrayList<>();
        labels.add(new LabelImpl(1));

        SProgramImpl prog = new SProgramImpl("MyTestProgram", List.of(var), labels);

        programDisplayImpl display = new programDisplayImpl(prog);

        display.printProgram();

    }
}
