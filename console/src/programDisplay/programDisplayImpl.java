package programDisplay;

import logic.Variable.Variable;
import logic.Variable.VariableType;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;

import java.util.List;

public class programDisplayImpl implements programDisplay {
    Program program;

    public programDisplayImpl(Program program)
    {
        this.program = program;

    }

    public void printProgram()
    {
        System.out.println("Program Name: " + program.getName());
        System.out.println("*Variables*");
        for (Variable variable : program.getVars()){
            System.out.println(variable.toString());

        }
        int exitCounter=0;

        System.out.println("*Labels*");
        for(Label label:program.getLabels()){
            if(label != FixedLabel.EXIT && label != FixedLabel.EMPTY) {
                System.out.println(label.toString());
            }
            else if(label == FixedLabel.EXIT) {
                exitCounter++;
            }

        }
        if(exitCounter!=0){
            System.out.println(FixedLabel.EXIT.toString());
        }
        System.out.println("*Instructions*");

        printInstructions();

    }


    public void printInstructions()
    {
        int instructionCounter=1;
       List <Instruction>  instructions =  program.getInstructions();
       for (Instruction instruction : instructions) {
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
