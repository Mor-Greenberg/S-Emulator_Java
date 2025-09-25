package logic.xml;

import jaxbV2.jaxb.v2.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.instruction.*;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.program.Program;
import logic.program.ProgramImpl;

import java.io.File;
import java.util.*;

public class XmlLoader {

//    public static SProgram loadFromFile(String path) {
//
//        if ((XmlValidation.validateXmlFilePath(path)) == 1) {
//            System.out.println("XML file does not exist");
//            return null;
//        } else if (XmlValidation.validateXmlFilePath(path) == 2) {
//            System.out.println("XML file does not end with .xml");
//            return null;
//        }
//        try {
//            JAXBContext context = JAXBContext.newInstance("jaxbV2.jaxb.v2");
//            Unmarshaller unmarshaller = context.createUnmarshaller();
//            File file = new File(path);
//            return (SProgram) unmarshaller.unmarshal(file);
//        } catch (JAXBException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//
//    public Variable StringToVariable(String str) {
//        char first = str.charAt(0);
//        String rest = str.substring(1);
//        int number = Optional.ofNullable(rest)
//                .filter(s -> !s.isEmpty())
//                .map(Integer::parseInt)
//                .orElse(0);
//
//
//        VariableType type;
//
//        switch (first) {
//            case 'x':
//                type = VariableType.INPUT;
//                break;
//            case 'y':
//                type = VariableType.RESULT;
//                break;
//            case 'z':
//                type = VariableType.WORK;
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown variable type: " + first);
//        }
//        Variable v;
//        if (type == VariableType.INPUT) {
//            v = new VariableImpl(type, number);
//        } else if (type == VariableType.RESULT) {
//            v = new VariableImpl(type);
//        } else { //WORK
//            v = new VariableImpl(type, number);
//        }
//
//        return v;
//    }
//
//    public Optional<Label> StringToLabel(String str) {
//        if (str == null || str.isEmpty()) {
//            return Optional.empty();
//        }
//
//        if (str.equals("EXIT")) {
//            return Optional.of(FixedLabel.EXIT); // או איך שאת מגדירה את Label ה־EXIT
//        }
//
//        if (str.charAt(0) != 'L') {
//            throw new IllegalArgumentException("Unknown label type: " + str);
//        }
//
//        String rest = str.substring(1);
//        int number = Integer.parseInt(rest);
//        Label label = new LabelImpl(number);
//
//        return Optional.of(label);
//    }
//
//    private String getArgValueByName(SInstructionArguments args, String name) {
//        if (args == null || args.getSInstructionArgument() == null)
//            return null;
//
//        for (SInstructionArgument arg : args.getSInstructionArgument()) {
//            if (arg.getName().equals(name)) {
//                return arg.getValue();
//            }
//        }
//
//        return null;
//    }
//
//
//    public Instruction StringToInstruction(String name, Variable variable, Label regularLabel, Label jumpLabel, SInstructionArguments sArgs, ExecutionContext context)
//    {
//        switch (name) {
//            case "INCREASE":
//                return new IncreaseInstruction(variable, regularLabel);
//
//            case "DECREASE":
//                return new DecreaseInstruction(variable, regularLabel);
//
//            case "JUMP_NOT_ZERO":
//                return new JumpNotZeroInstruction(variable, jumpLabel,regularLabel);
//
//            case "NEUTRAL":
//                return new NoOpInstruction(variable, regularLabel);
//
//            case "ZERO_VARIABLE":
//                return new ZeroVariableInstruction(variable, regularLabel);
//
//            case "GOTO_LABEL":
//                return new GoToLabelInstruction(variable, jumpLabel,regularLabel);
//
//            case "ASSIGNMENT": {
//                String assignedVarStr = getArgValueByName(sArgs, "assignedVariable");
//                if (assignedVarStr == null)
//                    throw new IllegalArgumentException("Missing 'assignedVariable' argument for ASSIGNMENT");
//                Variable source = StringToVariable(assignedVarStr);
//                return new AssignmentInstruction(regularLabel, variable, source);
//            }
//
//            case "CONSTANT_ASSIGNMENT": {
//                String constantStr = getArgValueByName(sArgs, "constantValue");
//                if (constantStr == null)
//                    throw new IllegalArgumentException("Missing 'constantValue' argument for CONSTANT_ASSIGNMENT");
//                int constant = Integer.parseInt(constantStr);
//                return new ConstantAssignmentInstruction(variable, regularLabel, constant);
//            }
//
//            case "JUMP_EQUAL_CONSTANT": {
//                String valueStr = getArgValueByName(sArgs, "constantValue");
//                if (valueStr == null)
//                    throw new IllegalArgumentException("Missing 'constantValue' for JUMP_EQUAL_CONSTANT");
//                int cmpValue = Integer.parseInt(valueStr);
//                return new JumpEqualConstantInstruction(variable, jumpLabel, regularLabel, cmpValue);
//            }
//
//            case "JUMP_ZERO":
//                return new JumpZeroInstruction(variable, jumpLabel,regularLabel);
//
//            case "JUMP_EQUAL_VARIABLE": {
//                String cmpVarStr = getArgValueByName(sArgs, "variableName");
//                if (cmpVarStr == null)
//                    throw new IllegalArgumentException("Missing 'variableName' for JUMP_EQUAL_VARIABLE");
//                Variable cmpVar = StringToVariable(cmpVarStr);
//                return new JumpEqualVariableInstruction(variable, cmpVar, jumpLabel, regularLabel);
//            }
//            case "QUOTE": {
//                String quotedFunctionName = getArgValueByName(sArgs, "quote");
//
//                // אם אין quote, ננסה את הפורמט החדש עם functionName + functionArguments
//                if (quotedFunctionName == null) {
//                    quotedFunctionName = getArgValueByName(sArgs, "functionName");
//                    String rawArgs = getArgValueByName(sArgs, "functionArguments");
//
//                    if (quotedFunctionName == null || rawArgs == null) {
//                        throw new IllegalArgumentException("Missing 'quote' argument for QUOTE");
//                    }
//
//                    // נפרק את הארגומנטים: (Const7),(Successor,x1)
//                    List<Variable> argumentList = new ArrayList<>();
//                    String[] argTokens = rawArgs.split("\\),\\(");
//
//                    for (int i = 0; i < argTokens.length; i++) {
//                        String token = argTokens[i].replace("(", "").replace(")", "").trim();
//                        if (token.isEmpty()) continue;
//
//                        // מצפים לפורמט: שם פונקציה או משתנה, לדוגמה: Successor,x1
//                        String[] parts = token.split(",");
//                        if (parts.length == 2) {
//                            // ציטוט פנימי עם פרמטר
//                            String subFunctionName = parts[0].trim();
//                            String varName = parts[1].trim();
//
//                            // כרגע נשתמש רק במפרמטר (x1) ונניח שהציטוט ייפתח במהלך ההרחבה
//                            Variable v = parseVariable(varName);
//                            argumentList.add(v);
//                        } else if (parts.length == 1) {
//                            // משתנה רגיל או קבוע (למשל Const7)
//                            argumentList.add(parseVariable(parts[0].trim()));
//                        } else {
//                            throw new IllegalArgumentException("Invalid functionArguments format: " + token);
//                        }
//                    }
//
//                    QuoteInstruction q = new QuoteInstruction(quotedFunctionName, argumentList, variable);
//                    q.computeDegree(context);
//                    return q;
//                }
//            }
//
//            default:
//                throw new IllegalArgumentException("Unknown instruction: " + name);
//        }
//    }
//    private Variable parseVariable(String token) {
//        if (token.matches("x\\d+")) {
//            int num = Integer.parseInt(token.substring(1));
//            return new VariableImpl(VariableType.INPUT, num);
//        } else if (token.matches("z\\d+")) {
//            int num = Integer.parseInt(token.substring(1));
//            return new VariableImpl(VariableType.WORK, num);
//        } else if (token.equals("y")) {
//            return new VariableImpl(VariableType.RESULT, 0);
//        } else {
//            // fallback: treat as work variable name
//            return new VariableImpl(VariableType.WORK, 0); // או זרקי שגיאה אם לא בטוח
//        }
//    }
//
//    private List<Variable> getVariablesListFromArgs(SInstructionArguments sArgs, Map<String, Program> functionMap) {
//        List<Variable> variables = new ArrayList<>();
//
//        // מחפש את שדה functionArguments
//        for (SInstructionArgument arg : sArgs.getSInstructionArgument()) {
//            if ("functionArguments".equals(arg.getName())) {
//                String value = arg.getValue(); // דוגמה: "(Const7),(Successor,x1)"
//                if (value == null || value.isEmpty()) continue;
//
//                // פיצול לפי "),(" ושמירה על מבנה אחיד
//                String[] parts = value.split("\\),\\(");
//                for (String part : parts) {
//                    part = part.replace("(", "").replace(")", ""); // הסרת סוגריים
//                    String[] tokens = part.split(",");
//                    for (String token : tokens) {
//                        token = token.trim();
//                        if (token.matches("x\\d+")) {
//                            int num = Integer.parseInt(token.substring(1));
//                            variables.add(new VariableImpl(VariableType.INPUT, num));
//                        } else if (token.matches("z\\d+")) {
//                            int num = Integer.parseInt(token.substring(1));
//                            variables.add(new VariableImpl(VariableType.WORK, num));
//                        } else if (token.equals("y")) {
//                            variables.add(new VariableImpl(VariableType.RESULT, 0));
//                        }
//                    }
//                }
//            }
//        }
//
//        return variables;
//    }
//
//
//    private Variable parseVariableFromString(String name) {
//        name = name.trim();
//
//        if (name.equals("y")) {
//            return new VariableImpl(VariableType.RESULT, 0);
//        } else if (name.startsWith("x")) {
//            String numStr = name.substring(1).replaceAll("[^\\d]", ""); // שומר רק מספרים
//            int num = Integer.parseInt(numStr);
//            return new VariableImpl(VariableType.INPUT, num);
//        } else if (name.startsWith("z")) {
//            String numStr = name.substring(1).replaceAll("[^\\d]", ""); // שומר רק מספרים
//            int num = Integer.parseInt(numStr);
//            return new VariableImpl(VariableType.WORK, num);
//        } else {
//            throw new IllegalArgumentException("Unknown variable name: " + name);
//        }
//    }
//
//
//    public InstructionType StringToInstructionType(String str) {
//        InstructionType instructionType;
//        switch (str) {
//            case "basic":
//                instructionType = InstructionType.B;
//                break;
//            case "synthetic":
//                instructionType = InstructionType.S;
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown instruction type: " + str);
//
//        }
//        return instructionType;
//    }
//
//
//    public Program SprogramToProgram(SProgram Sprogram,  Map<String, Program> functionMap,ExecutionContext context) {
//        Program program = new ProgramImpl(Sprogram.getName());
//        program.setFunctionMap(functionMap);
//
//        List<SInstruction> sInstructionsList = Sprogram.getSInstructions().getSInstruction();
//        for (SInstruction sinstruction : sInstructionsList) {
//            String sInstuctionName = sinstruction.getName();
//            SInstructionArguments sArgs = sinstruction.getSInstructionArguments();
//
//            String sType = sinstruction.getType();
//            InstructionType instructionType = StringToInstructionType(sType);
//
//            // Variable
//            String sVariable = sinstruction.getSVariable();
//            Variable variable = StringToVariable(sVariable);
//            program.addVar(variable);
//
//            // Regular label (S-Label)
//            String sLabel = sinstruction.getSLabel();
//            Optional<Label> optionalRegularLabel = StringToLabel(sLabel);
//            Label regularLabel = optionalRegularLabel.orElse(FixedLabel.EMPTY);
//            program.addLabel(regularLabel);
//
//            // Jump label (from arguments)
//            Optional<Label> jumpLabel = Optional.empty();
//            if (sArgs != null && sArgs.getSInstructionArgument() != null) {
//                for (SInstructionArgument sArg : sArgs.getSInstructionArgument()) {
//                    String argName = sArg.getName();
//                    String argValue = sArg.getValue();
//
//                    if (argName.equals("JNZLabel") || argName.equals("gotoLabel") ||
//                            argName.equals("JEConstantLabel") || argName.equals("JZLabel") ||
//                            argName.equals("JEVariableLabel")) {
//                        jumpLabel = StringToLabel(argValue);
//                        program.addLabel(jumpLabel.orElse(FixedLabel.EMPTY));
//                    }
//                }
//            }
//
//            // Create the instruction with both labels
//            Instruction instruction = StringToInstruction(
//                    sInstuctionName, variable, regularLabel, jumpLabel.orElse(FixedLabel.EMPTY), sArgs,context
//            );
//
//            program.addInstruction(instruction);
//        }
//
//        return program;
//    }
//
//    public Program sFunctionToProgram(SFunction sFunction,   Map<String, Program> functionMap, ExecutionContext context) {
//        Program program = new ProgramImpl(sFunction.getName());
//
//        List<SInstruction> sInstructionsList = sFunction.getSInstructions().getSInstruction();
//        for (SInstruction sInstr : sInstructionsList) {
//            String sInstuctionName = sInstr.getName();
//            SInstructionArguments sArgs = sInstr.getSInstructionArguments();
//
//            String sType = sInstr.getType();
//            InstructionType instructionType = StringToInstructionType(sType);
//
//            String sVariable = sInstr.getSVariable();
//            Variable variable = StringToVariable(sVariable);
//            program.addVar(variable);
//
//            // Regular label
//            Optional<Label> optionalRegularLabel = StringToLabel(sInstr.getSLabel());
//            Label regularLabel = optionalRegularLabel.orElse(FixedLabel.EMPTY);
//            program.addLabel(regularLabel);
//
//            // Jump label
//            Optional<Label> jumpLabel = Optional.empty();
//            if (sArgs != null && sArgs.getSInstructionArgument() != null) {
//                for (SInstructionArgument sArg : sArgs.getSInstructionArgument()) {
//                    String argName = sArg.getName();
//                    String argValue = sArg.getValue();
//
//                    if (argName.equals("JNZLabel") || argName.equals("gotoLabel") ||
//                            argName.equals("JEConstantLabel") || argName.equals("JZLabel") ||
//                            argName.equals("JEVariableLabel")) {
//                        jumpLabel = StringToLabel(argValue);
//                        program.addLabel(jumpLabel.orElse(FixedLabel.EMPTY));
//                    }
//                }
//            }
//
//            Instruction instr = StringToInstruction(
//                    sInstuctionName, variable, regularLabel, jumpLabel.orElse(FixedLabel.EMPTY), sArgs, context
//            );
//            program.addInstruction(instr);
//        }
//
//        return program;
//    }
//    public Map<String, Program> loadFunctions(List<SFunction> functionList,ExecutionContext context) {
//        Map<String, Program> functionMap = new HashMap<>();
//        for (SFunction func : functionList) {
//            Program program = sFunctionToProgram(func,functionMap,context);
//            functionMap.put(program.getName(), program);
//        }
//        return functionMap;
//    }
//
public static SProgram loadFromFile(String path) {
    if (XmlValidation.validateXmlFilePath(path) == 1) {
        System.out.println("XML file does not exist");
        return null;
    } else if (XmlValidation.validateXmlFilePath(path) == 2) {
        System.out.println("XML file does not end with .xml");
        return null;
    }

    try {
        JAXBContext context = JAXBContext.newInstance("jaxbV2.jaxb.v2");
        Unmarshaller unmarshaller = context.createUnmarshaller();
        File file = new File(path);
        return (SProgram) unmarshaller.unmarshal(file);
    } catch (JAXBException e) {
        throw new RuntimeException("Failed to load XML: " + path, e);
    }
}



}


