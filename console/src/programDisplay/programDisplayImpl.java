package programDisplay;

import logic.Variable.Variable;
import logic.Variable.VariableType;
import logic.instruction.InstructionType;
import logic.instruction.SInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.SProgram;

import java.util.List;

public class programDisplayImpl implements programDisplay {
    SProgram program;

    public programDisplayImpl(SProgram program)
    {
        this.program = program;

    }

    public void printProgram()
    {
        System.out.println(program.getName());
        for (Variable variable : program.getVars()){
            if(variable.getType()== VariableType.INPUT){
                System.out.println(variable.toString());
            }
        }
        int exitCounter=0;

        for(Label label:program.getLabels()){
            if(label != FixedLabel.EXIT) {
                System.out.println(label.toString());
            }
            else {
                exitCounter++;
            }

        }
        if(exitCounter!=0){
            System.out.println(FixedLabel.EXIT.toString());
        }

        printInstructions();

    }

    public void printInstructions()
    {
        int instructionCounter=0;
       List <SInstruction>  instructions =  program.getInstructions();
       for (SInstruction instruction : instructions) {
          InstructionFormat formattedInst = new InstructionFormat(instructionCounter,
                  instruction.getType().toString(),
                  instruction.getLabel(),
                  instruction.commandDisplay(),
                  instruction.cycles());
          formattedInst.printInstruction();
          instructionCounter++;
       }

    }
}
