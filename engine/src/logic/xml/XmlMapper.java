package logic.xml;

import jaxbV2.jaxb.v2.*;
import logic.Variable.QuoteVariable;
import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.instruction.*;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;
import logic.program.ProgramImpl;
import session.UserSession;

import java.util.*;

import static logic.xml.XmlParsingUtils.parseVariable;

public class XmlMapper {
    private final ExecutionContext context;

    public XmlMapper(ExecutionContext context) {
        this.context = context;
    }

    public Program map(SProgram sProgram, String path, String uploader) {
        this.context.reset();


        Map<String, Program> localFunctionMap = new HashMap<>();
        if (sProgram.getSFunctions() != null && sProgram.getSFunctions().getSFunction() != null) {
            localFunctionMap = loadFunctions(sProgram.getSFunctions().getSFunction(), sProgram.getName());
        }


        Program mainProgram = convertToProgram(
                sProgram.getName(),
                sProgram.getSInstructions().getSInstruction(),
                localFunctionMap
        );
        mainProgram.setFunctionMap(localFunctionMap);
        (mainProgram).setParentProgramName("MAIN");

        for (Program func : localFunctionMap.values()) {
            func.setUploaderName(uploader);
        }

        Map<String, Program> globalProgramsSnapshot = new HashMap<>(ExecutionContextImpl.getGlobalProgramMap());

        XmlValidation.validateAll(
                path,
                mainProgram,
                new ArrayList<>(localFunctionMap.values()),
                globalProgramsSnapshot
        );

        this.context.setFunctionMap(localFunctionMap);

        recomputeQuoteDegrees(mainProgram, localFunctionMap);
        for (Program func : localFunctionMap.values()) {
            recomputeQuoteDegrees(func, localFunctionMap);
        }

        ExecutionContextImpl.addGlobalProgram(mainProgram);
        for (Map.Entry<String, Program> entry : localFunctionMap.entrySet()) {
            ProgramImpl func = (ProgramImpl) entry.getValue();
            func.setFunction(true);
            func.setParentProgramName(mainProgram.getName());
            ExecutionContextImpl.addGlobalProgram(func);
            System.out.println("Added function: " + func.getName() + " (parent=" + func.getParentProgramName() + ")");
        }

        System.out.println("Saved to program map: " + mainProgram.getName());
        System.out.println("Now in map: " + ExecutionContextImpl.getGlobalProgramMap().keySet());





        return mainProgram;
    }


    private Program convertToProgram(String name, List<SInstruction> sInstructions, Map<String, Program> functionMap) {
        Program program = new ProgramImpl(name);

        for (SInstruction sInstr : sInstructions) {
            Variable variable = parseVariable(sInstr.getSVariable());

            Label regularLabel = XmlParsingUtils.parseLabel(sInstr.getSLabel())
                    .orElse(FixedLabel.EMPTY);

            Label jumpLabel = extractJumpLabel(sInstr);

            Instruction instr = buildInstruction(
                    program,
                    sInstr.getName(),
                    variable,
                    regularLabel,
                    jumpLabel,
                    sInstr.getSInstructionArguments(),
                    functionMap
            );

            program.addVar(variable);
            program.addLabel(regularLabel);
            program.addInstruction(instr);
        }

        return program;
    }


    private Label extractJumpLabel(SInstruction sInstr) {
        if (sInstr.getSInstructionArguments() == null) return FixedLabel.EMPTY;

        for (SInstructionArgument arg : sInstr.getSInstructionArguments().getSInstructionArgument()) {
            if (arg.getName().endsWith("Label")) {
                return XmlParsingUtils.parseLabel(arg.getValue()).orElse(FixedLabel.EMPTY);
            }
        }
        return FixedLabel.EMPTY;
    }

    private Instruction buildInstruction(
            Program program,
            String name,
            Variable variable,
            Label regularLabel,
            Label jumpLabel,
            SInstructionArguments sArgs,
            Map<String, Program> functionMap
    ) {
        switch (name) {
            case "INCREASE":
                return new IncreaseInstruction(variable, regularLabel);

            case "DECREASE":
                return new DecreaseInstruction(variable, regularLabel);

            case "JUMP_NOT_ZERO":
                return new JumpNotZeroInstruction(variable, jumpLabel, regularLabel);

            case "NEUTRAL":
                return new NoOpInstruction(variable, regularLabel);

            case "JUMP_ZERO":
                return new JumpZeroInstruction(variable, jumpLabel, regularLabel);

            case "JUMP_EQUAL_CONSTANT": {
                String valueStr = getArgValueByName(sArgs, "constantValue");
                if (valueStr == null)
                    throw new IllegalArgumentException("Missing 'constantValue' for JUMP_EQUAL_CONSTANT");
                int cmpValue = Integer.parseInt(valueStr);
                return new JumpEqualConstantInstruction(variable, jumpLabel, regularLabel, cmpValue);
            }

            case "JUMP_EQUAL_VARIABLE": {
                String cmpVarStr = getArgValueByName(sArgs, "variableName");
                if (cmpVarStr == null)
                    throw new IllegalArgumentException("Missing 'variableName' for JUMP_EQUAL_VARIABLE");
                Variable cmpVar = parseVariable(cmpVarStr);
                return new JumpEqualVariableInstruction(variable, cmpVar, jumpLabel, regularLabel);
            }


            case "ZERO_VARIABLE":
                return new ZeroVariableInstruction(variable, regularLabel);

            case "GOTO_LABEL":
                return new GoToLabelInstruction(variable, jumpLabel, regularLabel);

            case "ASSIGNMENT": {
                String assignedVarStr = getArgValueByName(sArgs, "assignedVariable");
                Variable source = parseVariable(assignedVarStr);
                program.addVar(source);
                return new AssignmentInstruction(regularLabel, variable, source);
            }

            case "CONSTANT_ASSIGNMENT": {
                String constantStr = getArgValueByName(sArgs, "constantValue");
                if (constantStr == null)
                    throw new IllegalArgumentException("Missing 'constantValue' argument for CONSTANT_ASSIGNMENT");
                int constant = Integer.parseInt(constantStr);
                return new ConstantAssignmentInstruction(variable, regularLabel, constant);
            }
            case "JUMP_EQUAL_FUNCTION": {
                String targetLabelStr = getArgValueByName(sArgs, "JEFunctionLabel");
                if (targetLabelStr == null) {
                    throw new IllegalArgumentException("Missing 'JEFunctionLabel' for JUMP_EQUAL_FUNCTION");
                }
                Label targetLabel = XmlParsingUtils.parseLabel(targetLabelStr).orElse(FixedLabel.EMPTY);

                String functionName = getArgValueByName(sArgs, "functionName");
                if (functionName == null) {
                    throw new IllegalArgumentException("Missing 'functionName' for JUMP_EQUAL_FUNCTION");
                }

                String rawArgs = getArgValueByName(sArgs, "functionArguments");
                List<Variable> argList = new ArrayList<>();
                if (rawArgs != null && !rawArgs.isEmpty()) {
                    argList = parseQuoteArgs(rawArgs, functionMap, program, variable);
                }

                return new JumpEqualFunctionInstruction(variable, targetLabel, functionName, argList, regularLabel);
            }

            case "QUOTE": {
                String quotedFunctionName = getArgValueByName(sArgs, "functionName");
                String rawArgs = getArgValueByName(sArgs, "functionArguments");

                List<Variable> argumentList = parseQuoteArgs(rawArgs, functionMap, program, variable);
                return new QuoteInstruction(regularLabel, quotedFunctionName, argumentList, variable);
            }

            default:
                throw new IllegalArgumentException("Unknown instruction: " + name);
        }
    }

    private String getArgValueByName(SInstructionArguments args, String name) {
        if (args == null || args.getSInstructionArgument() == null) return null;

        for (SInstructionArgument arg : args.getSInstructionArgument()) {
            if (arg.getName().equals(name)) {
                return arg.getValue();
            }
        }
        return null;
    }

    private List<Variable> parseQuoteArgs(String rawArgs,
                                          Map<String, Program> functionMap,
                                          Program program,
                                          Variable destination) {
        List<Variable> argumentList = new ArrayList<>();
        if (rawArgs == null || rawArgs.isEmpty()) return argumentList;

        List<String> tokens = splitTopLevel(rawArgs);

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            if (token.startsWith("(") && token.endsWith(")")) {
                token = token.substring(1, token.length() - 1).trim();
            }

            List<String> parts = splitTopLevel(token);
            if (parts.isEmpty()) continue;

            String head = parts.get(0).trim();

            // --- 💡 זיהוי פונקציה או ביטוי ---
            if (functionMap.containsKey(head)) {
                List<Variable> innerArgs = new ArrayList<>();
                for (int i = 1; i < parts.size(); i++) {
                    innerArgs.addAll(parseQuoteArgs(parts.get(i).trim(), functionMap, program, destination));
                }
                QuoteInstruction innerQuote = new QuoteInstruction(head, innerArgs, destination);
                argumentList.add(new QuoteVariable(innerQuote));
            }
            // --- 💡 זיהוי משתנה תקף בלבד ---
            else if (head.matches("^[xyz]\\d*$") || head.equals("y")) {
                Variable var = parseVariable(head);
                program.addVar(var);
                argumentList.add(var);
            }
            // --- 💡 CONST0 ופונקציות שלא מוגדרות במפה ---
            else if (functionMap.containsKey(head) || head.startsWith("CONST")) {
                // פונקציה פנימית כמו CONST0, NOT, EQUAL וכו'
                QuoteInstruction innerQuote = new QuoteInstruction(head, new ArrayList<>(), destination);
                argumentList.add(new QuoteVariable(innerQuote));
            }
            else {
                System.out.println("Skipping unrecognized token: " + head);
            }
        }

        return argumentList;
    }


    private List<String> splitTopLevel(String expr) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') {
                depth++;
                current.append(c);
            } else if (c == ')') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        return result;
    }


    private void recomputeQuoteDegrees(Program program, Map<String, Program> functionMap) {
        for (Instruction instr : program.getInstructions()) {
            if (instr instanceof QuoteInstruction q) {
                q.computeDegree();
            }
        }
    }
    private Map<String, Program> loadFunctions(List<SFunction> functionList, String mainProgramName) {
        Map<String, Program> functionMap = new HashMap<>();

        for (SFunction func : functionList) {
            ProgramImpl prog = new ProgramImpl(func.getName());
            prog.setFunction(true);
            prog.setParentProgramName(mainProgramName);
            functionMap.put(func.getName(), prog);
        }

        for (SFunction func : functionList) {
            Program prog = convertToProgram(func.getName(), func.getSInstructions().getSInstruction(), functionMap);

            if (prog instanceof ProgramImpl impl) {
                impl.setFunction(true);
                impl.setParentProgramName(mainProgramName);
            }

            functionMap.put(prog.getName(), prog);
        }

        return functionMap;
    }

}
