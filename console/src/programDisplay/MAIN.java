package programDisplay;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.instruction.DecreaseInstruction;
import logic.instruction.IncreaseInstruction;
import logic.instruction.Instruction;
import logic.jaxb.schema.generated.SProgram;
import logic.label.LabelImpl;
import logic.program.Program;
import logic.program.ProgramImpl;
import logic.xml.XmlLoader;

import java.util.ArrayList;
import java.util.List;

import static logic.label.Label.labels;

public class MAIN {
    public static void main(String[] args) {
        String path = "C:\\Users\\Mor\\Desktop\\computer science\\java\\S-Emulator_sub\\S-Emulator_sub\\badic.xml";
        SProgram sProgram = XmlLoader.loadFromFile(path);
        if (sProgram != null) {
            System.out.println("Program loaded successfully!");
            System.out.println("Program name: " + sProgram.getName());
        }
        XmlLoader xmlLoader = new XmlLoader();
        Program realProgram=xmlLoader.SprogramToProgram(sProgram);
        programDisplayImpl display= new programDisplayImpl(realProgram);
        display.printProgram();





    }
}
