package printExpand.expansion;

import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.program.Program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrintExpansion {
    Program program;

    public PrintExpansion(Program program) {
        this.program = program;
    }

    public void printProgramWithOrigins(Program program) {
        List<Instruction> instructions = program.getActiveInstructions();
        int counter = 1;

        int nextId = 1;
        for (Instruction instr : instructions) {
            if (instr instanceof AbstractInstruction absInstr && absInstr.getUniqueId() == 0) {
                absInstr.setUniqueId(nextId++);
            }
        }
        for (Instruction instr : instructions) {
            StringBuilder line = new StringBuilder();

            int id = (instr instanceof Instruction absInstr) ? absInstr.getUniqueId() : -1;
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

            counter++;
            System.out.println(line);
        }
    }
    public static List<AbstractInstruction> getInstructionHistoryChain(AbstractInstruction instruction) {
        List<AbstractInstruction> history = new ArrayList<>();
        AbstractInstruction current = instruction;
        while (current != null) {
            history.add(current);
            current = current.getOrigin();
        }
        Collections.reverse(history);
        return history;
    }




}
