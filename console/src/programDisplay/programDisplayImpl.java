package programDisplay;

import logic.Variable.Variable;
import logic.instruction.AbstractInstruction;
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

        printInstructions(program.getInstructions());


    }


    public void printInstructions(List<Instruction> instructions)
    {
        int instructionCounter=1;

       for (Instruction instruction : instructions) {
          InstructionFormat formattedInst = new InstructionFormat(instructionCounter,
                  instruction.getType().toString(),
                  instruction.getLabel(),
                  instruction.commandDisplay(),
                  instruction.getCycles());
          formattedInst.printInstruction();
          instructionCounter++;
       }

    }
    public void printProgramWithOrigins(Program program) {
        List<Instruction> instructions = program.getActiveInstructions();
        int index = 1;

        for (Instruction instr : instructions) {
            StringBuilder line = new StringBuilder();

            // מספר הפקודה
            line.append("#").append(index).append(" (")
                    .append(instr.getType()).append(") ");

            // התווית (אם יש)
            String labelStr = instr.getLabel() != null ? instr.getLabel().toString() : "";
            line.append("[").append(String.format("%-8s", labelStr)).append("] ");

            // התצוגה של הפקודה (commandDisplay)
            line.append(instr.commandDisplay());

            // מספר המחזורים
            line.append(" (").append(instr.getCycles()).append(")");

            // אם יש origin – מוסיפים >>> ומציגים את הפקודה המקורית
            if (instr instanceof AbstractInstruction abs && abs.hasOrigin()) {
                Instruction origin = abs.getOrigin();
                line.append("  <<<   #")
                        .append(findInstructionIndex(instructions, origin) + 1) // מיקום הפקודה המקורית
                        .append(" (").append(origin.getType()).append(") ")
                        .append("[")
                        .append(String.format("%-8s", origin.getLabel() != null ? origin.getLabel().toString() : ""))
                        .append("] ")
                        .append(origin.commandDisplay())
                        .append(" (").append(origin.getCycles()).append(")");
            }

            System.out.println(line);
            index++;
        }
    }
    private int findInstructionIndex(List<Instruction> instructions, Instruction target) {
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i) == target) {
                return i;
            }
        }
        return -1; // לא נמצא – לא קריטי כי הוא יופיע רק אם כן נמצא
    }


}
