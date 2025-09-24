//package programDisplay;
//
//import gui.MainScreenController;
//import javafx.application.Platform;
//import logic.Variable.Variable;
//import logic.instruction.Instruction;
//import logic.label.FixedLabel;
//import logic.label.Label;
//import logic.program.Program;
//
//import java.util.List;
//
//public class ProgramDisplayImpl {
//    Program program;
//
//    private MainScreenController controller;
//    public ProgramDisplayImpl(MainScreenController controller) {
//        this.controller = controller;
//    }
//
//    public ProgramDisplayImpl(Program program) {
//        this.program = program;
//    }
//    public void printProgram(boolean xmlLoded)
//    {
//        if (!xmlLoded) {
//            System.out.println("XML is not loaded, returning");
//            return;
//        }
//        System.out.println("Program Name: " + program.getName());
//        System.out.println("*Variables*");
//        for (Variable variable : program.getVars()){
//            System.out.println(variable.toString());
//
//        }
//        int exitCounter=0;
//
//        System.out.println("*Labels*");
//        for(Label label:program.getLabels()){
//            if(label != FixedLabel.EXIT && label != FixedLabel.EMPTY) {
//                System.out.println(label.toString());
//            }
//            else if(label == FixedLabel.EXIT) {
//                exitCounter++;
//            }
//
//        }
//        if(exitCounter!=0){
//            System.out.println(FixedLabel.EXIT.toString());
//        }
//        System.out.println("*Instructions*");
//
//        printInstructions(program.getInstructions());
//
//
//    }
//
//
////    public void printInstructions(List<Instruction> instructions)
////    {
////       for (Instruction instruction : instructions) {
////          InstructionFormat formattedInst = new InstructionFormat(instruction);
////          formattedInst.printInstruction();
////       }
////
////    }
//public void printInstructions(List<Instruction> instructions) {
//    Platform.runLater(() -> controller.printInstructions(instructions));
//}
//
//
//
//}
package programDisplay;

import gui.MainScreenController;
import javafx.application.Platform;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.program.Program;

import java.util.List;

public class ProgramDisplayImpl {
    private Program program;
    private MainScreenController controller;

    public ProgramDisplayImpl(Program program) {
        System.out.println("📦 Constructor called with PROGRAM only!");
        this.program = program;
    }

    public ProgramDisplayImpl(MainScreenController controller) {
        System.out.println("📦 Constructor called with CONTROLLER!");
        this.controller = controller;
    }


    public void setProgram(Program program) {
        this.program = program;
    }

    /**
     * הדפסת פקודות לתוך טבלת JavaFX
     */
    public void printInstructions(List<Instruction> instructions) {
        if (controller != null) {
            Platform.runLater(() -> controller.printInstructions(instructions));
        } else {
            printInstructionsToConsole(instructions);
        }
    }


    private void printInstructionsToConsole(List<Instruction> instructions) {
        int counter = 1;

        int nextId = 1;
        for (Instruction instr : instructions) {
            if (instr instanceof AbstractInstruction absInstr && absInstr.getUniqueId() == 0) {
                absInstr.setUniqueId(nextId++);
            }
        }

        for (Instruction instr : instructions) {
            StringBuilder line = new StringBuilder();

            int id = (instr instanceof AbstractInstruction absInstr) ? absInstr.getUniqueId() : -1;
            line.append("#").append(String.format("%-3d", counter)).append(" (")
                    .append(instr.getType()).append(") ");

            String labelStr = "";
            if (instr.getLabel() != null && !instr.getLabel().equals(FixedLabel.EMPTY)) {
                labelStr = instr.getLabel().toString();
            }
            line.append("[").append(String.format("%-5s", labelStr)).append("] ");

            line.append(instr.commandDisplay());

            line.append(" (").append(instr.getCycles()).append(")");

            if (instr instanceof AbstractInstruction abs && abs.hasOrigin()) {
                Instruction origin = abs.getOrigin();

                if (origin instanceof AbstractInstruction absOrigin) {
                    int originId = absOrigin.getUniqueId();
                    line.append("  <<<   #").append(String.format("%-3d", originId))
                            .append(" (").append(origin.getType()).append(") ")
                            .append("[")
                            .append(String.format(
                                    "%-5s",
                                    (origin.getLabel() != null && !origin.getLabel().equals(FixedLabel.EMPTY))
                                            ? origin.getLabel().toString()
                                            : ""
                            ))
                            .append("] ")
                            .append(origin.commandDisplay())
                            .append(" (").append(origin.getCycles()).append(")");
                }
            }

            System.out.println(line);
            counter++;
        }
    }

    public void printProgram(boolean xmlLoaded) {
        if (!xmlLoaded) {
            System.out.println("XML is not loaded, returning");
            return;
        }

        System.out.println("Program Name: " + program.getName());
        System.out.println("*Variables*");
        for (var variable : program.getVars()) {
            System.out.println(variable.getRepresentation());
        }
    }
}

