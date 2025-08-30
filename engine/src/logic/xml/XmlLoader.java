package logic.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.instruction.*;
import logic.jaxb.schema.generated.*;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.program.Program;
import logic.program.ProgramImpl;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class XmlLoader {

    public static SProgram loadFromFile(String path) {

        if ((XmlValidation.validateXmlFilePath(path)) == 1) {
            System.out.println("XML file does not exist");
            return null;
        } else if (XmlValidation.validateXmlFilePath(path) == 2) {
            System.out.println("XML file does not end with .xml");
            return null;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(SProgram.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File file = new File(path);
            return (SProgram) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Variable StringToVariable(String str) {
        char first = str.charAt(0);
        String rest = str.substring(1);
        int number = Optional.ofNullable(rest)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .orElse(0);


        VariableType type;

        switch (first) {
            case 'x':
                type = VariableType.INPUT;
                break;
            case 'y':
                type = VariableType.RESULT;
                break;
            case 'z':
                type = VariableType.WORK;
                break;
            default:
                throw new IllegalArgumentException("Unknown variable type: " + first);
        }
        Variable v;
        if (type == VariableType.INPUT) {
            v = new VariableImpl(type, number);
        } else if (type == VariableType.RESULT) {
            v = new VariableImpl(type);
        } else { //WORK
            v = new VariableImpl(type, number);
        }

        return v;
    }

    public Optional<Label> StringToLabel(String str) {
        if (str == null || str.isEmpty()) {
            return Optional.empty();
        }

        if (str.equals("EXIT")) {
            return Optional.of(FixedLabel.EXIT); // או איך שאת מגדירה את Label ה־EXIT
        }

        if (str.charAt(0) != 'L') {
            throw new IllegalArgumentException("Unknown label type: " + str);
        }

        String rest = str.substring(1);
        int number = Integer.parseInt(rest);
        Label label = new LabelImpl(number);

        return Optional.of(label);
    }

    private String getArgValueByName(SInstructionArguments args, String name) {
        if (args == null || args.getSInstructionArgument() == null)
            return null;

        for (SInstructionArgument arg : args.getSInstructionArgument()) {
            if (arg.getName().equals(name)) {
                return arg.getValue();
            }
        }

        return null;
    }


    public Instruction StringToInstruction(String name, Variable variable, Label regularLabel, Label jumpLabel, SInstructionArguments sArgs) {
        switch (name) {
            case "INCREASE":
                return new IncreaseInstruction(variable, regularLabel);

            case "DECREASE":
                return new DecreaseInstruction(variable, regularLabel);

            case "JUMP_NOT_ZERO":
                return new JumpNotZeroInstruction(variable, jumpLabel,regularLabel);

            case "NEUTRAL":
                return new NoOpInstruction(variable, regularLabel);

            case "ZERO_VARIABLE":
                return new ZeroVariableInstruction(variable, regularLabel);

            case "GOTO_LABEL":
                return new GoToLabelInstruction(variable, jumpLabel,regularLabel);

            case "ASSIGNMENT": {
                String assignedVarStr = getArgValueByName(sArgs, "assignedVariable");
                if (assignedVarStr == null)
                    throw new IllegalArgumentException("Missing 'assignedVariable' argument for ASSIGNMENT");
                Variable source = StringToVariable(assignedVarStr);
                return new AssignmentInstruction(regularLabel, variable, source);
            }

            case "CONSTANT_ASSIGNMENT": {
                String constantStr = getArgValueByName(sArgs, "constantValue");
                if (constantStr == null)
                    throw new IllegalArgumentException("Missing 'constantValue' argument for CONSTANT_ASSIGNMENT");
                int constant = Integer.parseInt(constantStr);
                return new ConstantAssignmentInstruction(variable, regularLabel, constant);
            }

            case "JUMP_EQUAL_CONSTANT": {
                String valueStr = getArgValueByName(sArgs, "constantValue");
                if (valueStr == null)
                    throw new IllegalArgumentException("Missing 'constantValue' for JUMP_EQUAL_CONSTANT");
                int cmpValue = Integer.parseInt(valueStr);
                return new JumpEqualConstantInstruction(variable, jumpLabel, regularLabel, cmpValue);
            }

            case "JUMP_ZERO":
                return new JumpZeroInstruction(variable, jumpLabel,regularLabel);

            case "JUMP_EQUAL_VARIABLE": {
                String cmpVarStr = getArgValueByName(sArgs, "variableName");
                if (cmpVarStr == null)
                    throw new IllegalArgumentException("Missing 'variableName' for JUMP_EQUAL_VARIABLE");
                Variable cmpVar = StringToVariable(cmpVarStr);
                return new JumpEqualVariableInstruction(variable, cmpVar, jumpLabel, regularLabel);
            }

            default:
                throw new IllegalArgumentException("Unknown instruction: " + name);
        }
    }



    public InstructionType StringToInstructionType(String str) {
        InstructionType instructionType;
        switch (str) {
            case "basic":
                instructionType = InstructionType.B;
                break;
            case "synthetic":
                instructionType = InstructionType.S;
                break;
            default:
                throw new IllegalArgumentException("Unknown instruction type: " + str);

        }
        return instructionType;
    }


    public Program SprogramToProgram(SProgram Sprogram) {
        Program program = new ProgramImpl(Sprogram.getName());

        List<SInstruction> sInstructionsList = Sprogram.getSInstructions().getSInstruction();
        for (SInstruction sinstruction : sInstructionsList) {
            String sInstuctionName = sinstruction.getName();
            SInstructionArguments sArgs = sinstruction.getSInstructionArguments();

            String sType = sinstruction.getType();
            InstructionType instructionType = StringToInstructionType(sType);

            // Variable
            String sVariable = sinstruction.getSVariable();
            Variable variable = StringToVariable(sVariable);
            program.addVar(variable);

            // Regular label (S-Label)
            String sLabel = sinstruction.getSLabel();
            Optional<Label> optionalRegularLabel = StringToLabel(sLabel);
            Label regularLabel = optionalRegularLabel.orElse(FixedLabel.EMPTY);
            program.addLabel(regularLabel);

            // Jump label (from arguments)
            Optional<Label> jumpLabel = Optional.empty();
            if (sArgs != null && sArgs.getSInstructionArgument() != null) {
                for (SInstructionArgument sArg : sArgs.getSInstructionArgument()) {
                    String argName = sArg.getName();
                    String argValue = sArg.getValue();

                    if (argName.equals("JNZLabel") || argName.equals("gotoLabel") ||
                            argName.equals("JEConstantLabel") || argName.equals("JZLabel") ||
                            argName.equals("JEVariableLabel")) {
                        jumpLabel = StringToLabel(argValue);
                        program.addLabel(jumpLabel.orElse(FixedLabel.EMPTY));
                    }
                }
            }

            // Create the instruction with both labels
            Instruction instruction = StringToInstruction(
                    sInstuctionName, variable, regularLabel, jumpLabel.orElse(FixedLabel.EMPTY), sArgs
            );
            program.addInstruction(instruction);
        }

        return program;
    }

}


