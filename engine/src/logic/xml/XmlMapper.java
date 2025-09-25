package logic.xml;

import jaxbV2.jaxb.v2.*;
import logic.Variable.QuoteVariable;
import logic.Variable.Variable;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.instruction.*;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;
import logic.program.ProgramImpl;

import java.util.*;

import static logic.xml.XmlParsingUtils.parseVariable;

public class XmlMapper {
    private final ExecutionContext context;

    public XmlMapper(ExecutionContext context) {
        this.context = context;
    }

    public Program map(SProgram sProgram) {
        Map<String, Program> functionMap = new HashMap<>();
        if (sProgram.getSFunctions() != null && sProgram.getSFunctions().getSFunction() != null) {
            functionMap = loadFunctions(sProgram.getSFunctions().getSFunction());
        }

        this.context.setFunctionMap(functionMap);

        Program mainProgram = convertToProgram(
                sProgram.getName(),
                sProgram.getSInstructions().getSInstruction(),
                functionMap
        );
        mainProgram.setFunctionMap(functionMap);

        recomputeQuoteDegrees(mainProgram, functionMap);

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

    private Map<String, Program> loadFunctions(List<SFunction> functionList) {
        Map<String, Program> functionMap = new HashMap<>();
        for (SFunction func : functionList) {
            Program prog = convertToProgram(func.getName(), func.getSInstructions().getSInstruction(), functionMap);
            functionMap.put(prog.getName(), prog);
        }
        return functionMap;
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
    )
    {
        switch (name) {
            case "INCREASE":
                return new IncreaseInstruction(variable, regularLabel);

            case "DECREASE":
                return new DecreaseInstruction(variable, regularLabel);

            case "JUMP_NOT_ZERO":
                return new JumpNotZeroInstruction(variable, jumpLabel, regularLabel);

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

            case "NEUTRAL":
                return new NoOpInstruction(variable, regularLabel);

            case "ZERO_VARIABLE":
                return new ZeroVariableInstruction(variable, regularLabel);

            case "GOTO_LABEL":
                return new GoToLabelInstruction(variable, jumpLabel, regularLabel);

            case "ASSIGNMENT": {
                String assignedVarStr = getArgValueByName(sArgs, "assignedVariable");
                Variable source = parseVariable(assignedVarStr);
                program.addVar(source); // עכשיו יש לך גישה ל־program
                return new AssignmentInstruction(regularLabel, variable, source);
            }


            case "CONSTANT_ASSIGNMENT": {
                String constantStr = getArgValueByName(sArgs, "constantValue");
                if (constantStr == null)
                    throw new IllegalArgumentException("Missing 'constantValue' argument for CONSTANT_ASSIGNMENT");
                int constant = Integer.parseInt(constantStr);
                return new ConstantAssignmentInstruction(variable, regularLabel, constant);
            }
            case "QUOTE": {
                String quotedFunctionName = getArgValueByName(sArgs, "functionName");
                String rawArgs = getArgValueByName(sArgs, "functionArguments");

                List<Variable> argumentList = parseQuoteArgs(rawArgs, functionMap, program, variable);
                QuoteInstruction q = new QuoteInstruction(quotedFunctionName, argumentList, variable);
                q.computeDegree(context);
                return q;
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
    private List<Variable> parseQuoteArgs(String rawArgs, Map<String, Program> functionMap, Program program, Variable destination) {
        List<Variable> argumentList = new ArrayList<>();
        if (rawArgs == null || rawArgs.isEmpty()) return argumentList;

        String[] argTokens = rawArgs.split("\\),\\(");
        for (String token : argTokens) {
            token = token.replace("(", "").replace(")", "").trim();
            if (token.isEmpty()) continue;

            String[] parts = token.split(",");
            if (parts.length == 1) {
                String funcOrVarName = parts[0].trim();
                if (functionMap.containsKey(funcOrVarName)) {
                    QuoteInstruction innerQuote = new QuoteInstruction(funcOrVarName, new ArrayList<>(), destination);
                    argumentList.add(new QuoteVariable(innerQuote));
                } else {
                    Variable var = parseVariable(funcOrVarName);
                    program.addVar(var); // ← לוודא שנוסף
                    argumentList.add(var);
                }
            } else {
                // פונקציה עם ארגומנטים → נבנה רקורסיבית
                String funcName = parts[0].trim();
                List<Variable> innerArgs = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    Variable innerVar = parseVariable(parts[i].trim());
                    program.addVar(innerVar);
                    innerArgs.add(innerVar);
                }
                QuoteInstruction innerQuote = new QuoteInstruction(funcName, innerArgs, destination);
                argumentList.add(new QuoteVariable(innerQuote));
            }
        }
        return argumentList;
    }
    private void recomputeQuoteDegrees(Program program, Map<String, Program> functionMap) {
        for (Instruction instr : program.getInstructions()) {
            if (instr instanceof QuoteInstruction q) {
                q.computeDegree(this.context);
            }
        }

        for (Program func : functionMap.values()) {
            for (Instruction instr : func.getInstructions()) {
                if (instr instanceof QuoteInstruction q) {
                    q.computeDegree(this.context);
                }
            }
        }
    }
}
