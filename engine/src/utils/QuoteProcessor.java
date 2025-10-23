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

public class QuoteProcessor {


    public static int computeDegree(Program quotedProgram) {
        if (quotedProgram == null || quotedProgram.getInstructions().isEmpty()) {
            return 1;
        }

        return quotedProgram.getInstructions().stream()
                .mapToInt(Instruction::getDegree)
                .max()
                .orElse(0) + 1;
    }


    public static List<AbstractInstruction> rewriteInstructions(Program quotedProgram,
                                                                Map<String, Variable> variableMap,
                                                                Label endLabel,
                                                                ExecutionContext context) {
        List<AbstractInstruction> result = new ArrayList<>();

        for (Instruction instr : quotedProgram.getInstructions()) {
            AbstractInstruction clone = instr.clone();

            clone.replaceVariables(variableMap);

            if (clone.jumpsTo(FixedLabel.EXIT)) {
                clone.replaceJumpLabel(FixedLabel.EXIT, endLabel);
            }

            result.add(clone);
        }

        return result;
    }
}