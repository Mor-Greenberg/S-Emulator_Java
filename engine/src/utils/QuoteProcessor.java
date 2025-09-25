package utils;

import logic.execution.ExecutionContext;
import logic.program.Program;
import logic.instruction.Instruction;

import java.util.Comparator;



import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.AssignmentInstruction;
import logic.instruction.Instruction;
import logic.instruction.NoOpInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;

import java.util.*;

/**
 * Utility class for handling quoted programs (used in QuoteInstruction).
 */
public class QuoteProcessor {

    /**
     * Computes the degree of a quoted program.
     * The degree is defined as 1 + max(degree of instruction in program).
     */
    public static int computeDegree(Program quotedProgram) {
        if (quotedProgram == null || quotedProgram.getInstructions().isEmpty()) {
            return 1;
        }

        return quotedProgram.getInstructions().stream()
                .mapToInt(Instruction::getDegree)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Rewrites a quoted program's instructions, replacing variable and label references according to given maps.
     *
     * @param quotedProgram The quoted function (Program)
     * @param variableMap   Mapping from formal param names (x1, x2, ..., y) to actual Variables
     * @param endLabel      Label to use instead of EXIT
     * @param context       Execution context (for generating new labels/vars)
     * @return A list of AbstractInstructions with replacements applied
     */
    public static List<AbstractInstruction> rewriteInstructions(Program quotedProgram,
                                                                Map<String, Variable> variableMap,
                                                                Label endLabel,
                                                                ExecutionContext context) {
        List<AbstractInstruction> result = new ArrayList<>();

        for (Instruction instr : quotedProgram.getInstructions()) {
            AbstractInstruction clone = instr.clone(); // בהנחה שיש clone()

            // החלפת משתנים
            clone.replaceVariables(variableMap);

            // החלפת תווית קפיצה ל־EXIT
            if (clone.jumpsTo(FixedLabel.EXIT)) {
                clone.replaceJumpLabel(FixedLabel.EXIT, endLabel);
            }

            result.add(clone);
        }

        return result;
    }
}