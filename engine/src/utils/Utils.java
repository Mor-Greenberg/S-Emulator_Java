package utils;

import javafx.application.Platform;
import logic.architecture.ArchitectureRules;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionRunner;
import logic.instruction.*;
import logic.program.Program;
import ui.executionBoard.ExecutionBoardController;

import java.util.List;



public class Utils {
    public static String generateSummary(List<Instruction> instructions) {
        if (instructions == null || instructions.isEmpty())
            return null;

        long total = instructions.size();
        long basic = instructions.stream()
                .filter(instr -> instr.getType() == InstructionType.B)
                .count();

        long synthetic = instructions.stream()
                .filter(instr -> instr.getType() == InstructionType.S)
                .count();

        long cycles = instructions.stream()
                .mapToLong(Instruction::getCycles)
                .sum();

        long supported = 0;
        if (ExecutionRunner.architecture != null) {
            supported = instructions.stream()
                    .filter(instr -> ArchitectureRules.isAllowed(ExecutionRunner.architecture, instr.getData()))
                    .count();
        }

        String summary = String.format(
                "SUMMARY: total: %d | basic: %d | synthetic: %d | cycles: %d | supported: %d",
                total, basic, synthetic, cycles, supported
        );

        Platform.runLater(() -> {
            ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
            if (ctrl != null) {
                ctrl.updateSummaryLine(summary);
            }
        });
        return summary;
    }


    public static int computeProgramDegree(Program program, ExecutionContext context) {
        int maxDegree = 0;

        for (Instruction instr : program.getInstructions()) {
            if (instr instanceof QuoteInstruction q) {
                q.computeDegree();
                maxDegree = Math.max(maxDegree, q.getDegree());

                Program quoted = context.getProgramMap(q.getQuotedProgramName());
                if (quoted != null) {
                    maxDegree = Math.max(maxDegree, computeProgramDegree(quoted, context));
                }
            } else if (instr instanceof AbstractInstruction ai) {
                maxDegree = Math.max(maxDegree, ai.getDegree());
            }
        }

        return maxDegree;
    }


}
