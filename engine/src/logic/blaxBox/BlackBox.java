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
                logic.instruction.AssignmentInstruction a = (logic.instruction.AssignmentInstruction) instr;
                long val = context.getVariableValue(a.getSource());
                context.updateVariable(a.getDestination(), val);
                return pc + 1;
            }
            case "CONSTANT_ASSIGNMENT" -> {
                logic.instruction.ConstantAssignmentInstruction c = (logic.instruction.ConstantAssignmentInstruction) instr;
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
                logic.instruction.JumpNotZeroInstruction j = (logic.instruction.JumpNotZeroInstruction) instr;
                long v = context.getVariableValue(j.getVariable());
                if (v != 0 && j.getJnzLabel() != logic.label.FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(j.getJnzLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }

            case "JUMP_ZERO" -> {
                logic.instruction.JumpZeroInstruction j = (logic.instruction.JumpZeroInstruction) instr;
                long v = context.getVariableValue(j.getVariable());
                if (v == 0 && j.getJZLabel() != logic.label.FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(j.getJZLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }
            case "JUMP_EQUAL_CONSTANT" -> {
                logic.instruction.JumpEqualConstantInstruction j = (logic.instruction.JumpEqualConstantInstruction) instr;
                long v = context.getVariableValue(j.getVariable());
                if (v == j.getConstantValue() && j.getJumpToLabel() != logic.label.FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(j.getJumpToLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }
            case "JUMP_EQUAL_VARIABLE" -> {
                logic.instruction.JumpEqualVariableInstruction j = (logic.instruction.JumpEqualVariableInstruction) instr;
                long v1 = context.getVariableValue(j.getVariable());
                long v2 = context.getVariableValue(j.getVariableName());
                if (v1 == v2 && j.getTargetLabel() != logic.label.FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(j.getTargetLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }
            case "GOTO_LABEL" -> {
                logic.instruction.GoToLabelInstruction g = (logic.instruction.GoToLabelInstruction) instr;
                if (g.getGoToLabel() == logic.label.FixedLabel.EXIT) {
                    return instrs.size();  // יציאה
                }
                return labelToIndex.getOrDefault(g.getGoToLabel().toString(), pc + 1);
            }
            case "JUMP_EQUAL_FUNCTION" -> {
                JumpEqualFunctionInstruction jef = (JumpEqualFunctionInstruction) instr;
                Program func = program.getFunctionMap().get(jef.getFunctionName());
                if (func == null) {
                    System.out.println("⚠ Unknown function in JUMP_EQUAL_FUNCTION: " + jef.getFunctionName());
                    return pc + 1;
                }

                ExecutionContext subContext = new ExecutionContextImpl(new HashMap<>(), program.getFunctionMap(),context.getLoadedPrograms());
                List<Variable> args = jef.getArguments();
                List<Variable> funcInputs = func.getVars().stream()
                        .filter(v -> v.getType() == VariableType.INPUT)
                        .toList();
                for (int i = 0; i < Math.min(args.size(), funcInputs.size()); i++) {
                    long argVal = context.getVariableValue(args.get(i));
                    subContext.updateVariable(funcInputs.get(i), argVal);
                }

                long qVal = executeBlackBox(subContext,program);
                long vVal = context.getVariableValue(jef.getVariable());

                if (vVal == qVal && jef.getTargetLabel() != logic.label.FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(jef.getTargetLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }


            case "QUOTE" -> {
                logic.program.ProgramImpl tmp = new logic.program.ProgramImpl("step-quote");
                tmp.setFunctionMap(program.getFunctionMap());
                tmp.setVariables(program.getVars());
                tmp.addInstruction(instr);
                executeBlackBox(context,program);
                return pc + 1;
            }

            default -> {
                System.out.println(" Unsupported black-box0 step for: " + name + " (skipping)");
                return pc + 1;
            }

        }
    }

    public static long executeBlackBox(ExecutionContext context, Program program, Set<String> visitedFunctions) {
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

            switch (instr.getName()) {
                case "ASSIGNMENT" -> {
                    AssignmentInstruction a = (AssignmentInstruction) instr;
                    long val = context.getVariableValue(a.getSource());
                    context.updateVariable(a.getDestination(), val);
                    pc++;
                }

                case "CONSTANT_ASSIGNMENT" -> {
                    ConstantAssignmentInstruction c = (ConstantAssignmentInstruction) instr;
                    context.updateVariable(c.getVariable(), c.getConstantValue());
                    pc++;
                }
                case "INCREASE" -> {
                    long val = context.getVariableValue(instr.getVariable());
                    context.updateVariable(instr.getVariable(), val + 1);
                    pc++;
                }
                case "DECREASE" -> {
                    long val = context.getVariableValue(instr.getVariable());
                    context.updateVariable(instr.getVariable(), val - 1);
                    pc++;
                }
                case "ZERO_VARIABLE" -> {
                    context.updateVariable(instr.getVariable(), 0);
                    pc++;
                }
                case "JUMP_NOT_ZERO" -> {
                    JumpNotZeroInstruction jnz = (JumpNotZeroInstruction) instr;
                    long val = context.getVariableValue(jnz.getVariable());
                    if (val != 0 && jnz.getJnzLabel() != FixedLabel.EMPTY) {
                        pc = labelToIndex.getOrDefault(jnz.getJnzLabel().getLabelRepresentation(), pc + 1);
                    } else {
                        pc++;
                    }
                }

                case "JUMP_ZERO" -> {
                    JumpZeroInstruction jz = (JumpZeroInstruction) instr;
                    long val = context.getVariableValue(jz.getVariable());
                    if (val == 0 && jz.getJZLabel() != FixedLabel.EMPTY) {
                        pc = labelToIndex.getOrDefault(jz.getJZLabel().getLabelRepresentation(), pc + 1);
                    } else {
                        pc++;
                    }
                }

                case "JUMP_EQUAL_CONSTANT" -> {
                    JumpEqualConstantInstruction jec = (JumpEqualConstantInstruction) instr;
                    long val = context.getVariableValue(jec.getVariable());
                    if (val == jec.getConstantValue() && jec.getJumpToLabel() != FixedLabel.EMPTY) {
                        pc = labelToIndex.getOrDefault(jec.getJumpToLabel().getLabelRepresentation(), pc + 1);
                    } else {
                        pc++;
                    }
                }
                case "JUMP_EQUAL_VARIABLE" -> {
                    JumpEqualVariableInstruction jev = (JumpEqualVariableInstruction) instr;
                    long v1 = context.getVariableValue(jev.getVariable());
                    long v2 = context.getVariableValue(jev.getVariableName());
                    if (v1 == v2 && jev.getTargetLabel() != FixedLabel.EMPTY) {
                        pc = labelToIndex.getOrDefault(jev.getTargetLabel().getLabelRepresentation(), pc + 1);
                    } else {
                        pc++;
                    }
                }
                case "GOTO_LABEL" -> {
                    GoToLabelInstruction g = (GoToLabelInstruction) instr;
                    if (g.getGoToLabel() == FixedLabel.EXIT) {
                        pc = instrs.size();
                    } else {
                        pc = labelToIndex.getOrDefault(g.getGoToLabel().toString(), pc + 1);
                    }
                }
                case "JUMP_EQUAL_FUNCTION" -> {
                    JumpEqualFunctionInstruction jef = (JumpEqualFunctionInstruction) instr;
                    Program func = program.getFunctionMap().get(jef.getFunctionName());
                    if (func == null || visitedFunctions.contains(jef.getFunctionName())) {
                        pc++;
                        break;
                    }

                    visitedFunctions.add(jef.getFunctionName());
                    ExecutionContext subCtx = new ExecutionContextImpl(new HashMap<>(), program.getFunctionMap(),context.getLoadedPrograms());
                    List<Variable> args = jef.getArguments();
                    List<Variable> funcInputs = func.getVars().stream()
                            .filter(v -> v.getType() == VariableType.INPUT)
                            .toList();

                    for (int i = 0; i < Math.min(args.size(), funcInputs.size()); i++) {
                        long argVal = context.getVariableValue(args.get(i));
                        subCtx.updateVariable(funcInputs.get(i), argVal);
                    }

                    long result = executeBlackBox(subCtx, func, new HashSet<>(visitedFunctions));
                }

                case "QUOTE" -> {
                    QuoteInstruction q = (QuoteInstruction) instr;
                    Program func = program.getFunctionMap().get(q.getQuotedFunctionName());
                    if (func == null || visitedFunctions.contains(q.getQuotedFunctionName())) {
                        pc++;
                        continue;
                    }

                    visitedFunctions.add(q.getQuotedFunctionName());
                    ExecutionContext subContext = new ExecutionContextImpl(new HashMap<>(), program.getFunctionMap(),context.getLoadedPrograms());
                    List<Variable> args = q.getArguments();
                    List<Variable> funcInputs = func.getVars().stream()
                            .filter(v -> v.getType() == VariableType.INPUT)
                            .toList();

                    for (int i = 0; i < Math.min(args.size(), funcInputs.size()); i++) {
                        long argVal = context.getVariableValue(args.get(i));
                        subContext.updateVariable(funcInputs.get(i), argVal);
                    }

                    long subResult = executeBlackBox(subContext, func, new HashSet<>(visitedFunctions));
                    context.updateVariable(q.getVariable(), subResult);
                    pc++;
                }

                default -> {
                    pc++;
                }
            }
        }

        Variable resultVar =program.getVars().stream()
                .filter(v -> v.getType() == VariableType.RESULT)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No result variable found"));
        return context.getVariableValue(resultVar);
    }
    public static long executeBlackBox(ExecutionContext context, Program program) {
        return executeBlackBox(context, program, new HashSet<>());
    }

}
