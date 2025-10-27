package logic.blaxBox;

import logic.Variable.Variable;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.instruction.*;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;

import java.util.*;

public class BlackBox {

    /** Executes a single step in degree 0 mode */
    public static int blackBoxStepDegree0(
            Instruction instr,
            int pc,
            Map<String, Integer> labelToIndex,
            List<Instruction> instrs,
            ExecutionContext context,
            Program program
    ) {
        String name = instr.getName();

        switch (name) {
            case "ASSIGNMENT" -> {
                AssignmentInstruction a = (AssignmentInstruction) instr;
                long val = context.getVariableValue(a.getSource());
                context.updateVariable(a.getDestination(), val);
                return pc + 1;
            }

            case "CONSTANT_ASSIGNMENT" -> {
                ConstantAssignmentInstruction c = (ConstantAssignmentInstruction) instr;
                context.updateVariable(c.getVariable(), c.getConstantValue());
                return pc + 1;
            }

            case "INCREASE" -> {
                long v = context.getVariableValue(instr.getVariable());
                context.updateVariable(instr.getVariable(), v + 1);
                return pc + 1;
            }

            case "DECREASE" -> {
                long v = context.getVariableValue(instr.getVariable());
                context.updateVariable(instr.getVariable(), v - 1);
                return pc + 1;
            }

            case "ZERO_VARIABLE" -> {
                context.updateVariable(instr.getVariable(), 0);
                return pc + 1;
            }

            case "JUMP_NOT_ZERO" -> {
                JumpNotZeroInstruction j = (JumpNotZeroInstruction) instr;
                long v = context.getVariableValue(j.getVariable());
                if (v != 0 && j.getJnzLabel() != FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(j.getJnzLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }

            case "JUMP_ZERO" -> {
                JumpZeroInstruction j = (JumpZeroInstruction) instr;
                long v = context.getVariableValue(j.getVariable());
                if (v == 0 && j.getJZLabel() != FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(j.getJZLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }

            case "JUMP_EQUAL_CONSTANT" -> {
                JumpEqualConstantInstruction j = (JumpEqualConstantInstruction) instr;
                long v = context.getVariableValue(j.getVariable());
                if (v == j.getConstantValue() && j.getJumpToLabel() != FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(j.getJumpToLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }

            case "JUMP_EQUAL_VARIABLE" -> {
                JumpEqualVariableInstruction j = (JumpEqualVariableInstruction) instr;
                long v1 = context.getVariableValue(j.getVariable());
                long v2 = context.getVariableValue(j.getVariableName());
                if (v1 == v2 && j.getTargetLabel() != FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(j.getTargetLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }

            case "GOTO_LABEL" -> {
                GoToLabelInstruction g = (GoToLabelInstruction) instr;
                if (g.getGoToLabel() == FixedLabel.EXIT) {
                    return instrs.size();
                }
                return labelToIndex.getOrDefault(g.getGoToLabel().toString(), pc + 1);
            }

            case "JUMP_EQUAL_FUNCTION" -> {
                JumpEqualFunctionInstruction jef = (JumpEqualFunctionInstruction) instr;
                Program func = program.getFunctionMap().get(jef.getFunctionName());
                if (func == null) {
                    return pc + 1;
                }

                ExecutionContext subCtx = new ExecutionContextImpl(
                        new HashMap<>(),
                        program.getFunctionMap(),
                        context.getLoadedPrograms()
                );

                List<Variable> args = jef.getArguments();
                List<Variable> funcInputs = func.getVars().stream()
                        .filter(v -> v.getType() == VariableType.INPUT)
                        .toList();

                for (int i = 0; i < Math.min(args.size(), funcInputs.size()); i++) {
                    long argVal = context.getVariableValue(args.get(i));
                    subCtx.updateVariable(funcInputs.get(i), argVal);
                }

                long qVal = executeBlackBox(subCtx, func); // uses visited tracking internally
                long vVal = context.getVariableValue(jef.getVariable());

                if (vVal == qVal && jef.getTargetLabel() != FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(jef.getTargetLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }

            case "QUOTE" -> {
                QuoteInstruction q = (QuoteInstruction) instr;
                Program func = program.getFunctionMap().get(q.getQuotedFunctionName());
                if (func != null) {
                    ExecutionContext subCtx = new ExecutionContextImpl(
                            new HashMap<>(),
                            program.getFunctionMap(),
                            context.getLoadedPrograms()
                    );
                    executeBlackBox(subCtx, func);
                }
                return pc + 1;
            }

            default -> {
                return pc + 1;
            }
        }
    }

    /** Executes a program in degree 0 black-box mode */
    public static long executeBlackBox(ExecutionContext context, Program program, Set<String> visitedFunctions) {
        if (program == null) return 0;

        if (visitedFunctions.contains(program.getName())) {
            return 0;
        }
        visitedFunctions.add(program.getName());

        List<Instruction> instrs = program.getInstructions();

        Map<String, Integer> labelToIndex = new HashMap<>();
        for (int i = 0; i < instrs.size(); i++) {
            Label lbl = instrs.get(i).getLabel();
            if (lbl != null && lbl != FixedLabel.EMPTY) {
                labelToIndex.put(lbl.toString(), i);
            }
        }

        int pc = 0;
        while (pc < instrs.size()) {
            Instruction instr = instrs.get(pc);
            pc = blackBoxStepDegree0(instr, pc, labelToIndex, instrs, context, program);
        }

        Variable resultVar = program.getVars().stream()
                .filter(v -> v.getType() == VariableType.RESULT)
                .findFirst()
                .orElse(null);

        if (resultVar == null) return 0;
        return context.getVariableValue(resultVar);
    }

    public static long executeBlackBox(ExecutionContext context, Program program) {
        return executeBlackBox(context, program, new HashSet<>());
    }
}
