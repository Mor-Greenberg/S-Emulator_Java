package utils;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionType;
import logic.instruction.QuoteInstruction;
import logic.program.Program;

import java.util.List;

public class Utils {
    public static String generateSummary(List<Instruction> instructions) {
        long basicCount = instructions.stream()
                .filter(instr -> instr.getType() == InstructionType.B)
                .count();

        long syntheticCount = instructions.stream()
                .filter(instr -> instr.getType() == InstructionType.S)
                .count();

        long cyclesCount = instructions.stream()
                .mapToLong(Instruction::getCycles)
                .sum();

        return "SUMMARY: Total instructions: " + instructions.size()
                + " | Basic: " + basicCount
                + " | Synthetic: " + syntheticCount
                + " | cycles: " + cyclesCount;
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
