package programDisplay;

import logic.Variable.Variable;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;

import java.util.List;

public class ProgramDisplayImpl {
    Program program;

    public ProgramDisplayImpl(Program program)
    {
        this.program = program;

    }

    public void printProgram(boolean xmlLoded)
    {
        if (!xmlLoded) {
            System.out.println("XML is not loaded, returning");
            return;
        }
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

        printInstructions(program.getInstructions());


    }


    public void printInstructions(List<Instruction> instructions)
    {
       for (Instruction instruction : instructions) {
          InstructionFormat formattedInst = new InstructionFormat(instruction);
          formattedInst.printInstruction();
       }

    }



}
