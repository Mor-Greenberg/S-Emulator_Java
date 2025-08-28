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
        if( (XmlValidation.validateXmlFilePath(path)) == 1) {
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



    public Instruction StringToInstruction(String str, Variable variable, Label label) {

        Instruction instruction;
        switch (str) {
            case "INCREASE":
                instruction = new IncreaseInstruction(variable, label);
                break;
            case "DECREASE":
                instruction = new DecreaseInstruction(variable, label);
                break;
            case "JUMP_NOT_ZERO":
                instruction = new JumpNotZeroInstruction(variable, label);
                break;
            case "NEUTRAL":
                instruction = new NoOpInstruction(variable, label);
                break;
            case "ZERO_VARIABLE":
                instruction = new ZeroVariableInstruction(variable, label);
                break;
            default:
                throw new IllegalArgumentException("Unknown instruction: " + str);
        }
        return instruction;
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

    public Optional<Label> handelJumpNotZero(String str) {

        if (str == null || str.isEmpty()) {
            return Optional.empty();
        }
        char first = str.charAt(0);
        if (first != 'L') {
            throw new IllegalArgumentException("Unknown label type: " + first);
        }

        String rest = str.substring(1);
        int number = Integer.parseInt(rest);
        Label label = new LabelImpl(number);
        return Optional.of(label);

    }

    public Label handleSargument(SInstructionArgument sargument) {
        String name = sargument.getName();
        String value = sargument.getValue();
        int number = Integer.parseInt(value);
        Label resultLabel = new LabelImpl(number);
        return resultLabel;
    }

    public Program SprogramToProgram(SProgram Sprogram) {
        Program program = new ProgramImpl(Sprogram.getName());



        List<SInstruction> sInstructionsList = Sprogram.getSInstructions().getSInstruction();
        for (int i = 0; i < sInstructionsList.size(); i++) {

            SInstruction sinstruction = sInstructionsList.get(i);//JAXB
            String sInstuctionName = sinstruction.getName();

            SInstructionArguments sArgs = sinstruction.getSInstructionArguments();

            String sType = sinstruction.getType();
            InstructionType instructionType = StringToInstructionType(sType);
            String sVariable = sinstruction.getSVariable();
            Variable variable = StringToVariable(sVariable);
            program.addVar(variable);
            String sLabel = sinstruction.getSLabel();
            Optional<Label> optionalLabel = StringToLabel(sLabel);
            Label label = optionalLabel.orElse(FixedLabel.EMPTY);
            program.addLabel(label);

            Optional<Label> jumpLabel = Optional.empty();

            if (sArgs != null && sArgs.getSInstructionArgument() != null) {
                for (SInstructionArgument sArg : sArgs.getSInstructionArgument()) {
                    String argName = sArg.getName();
                    String argValue = sArg.getValue();

                    if (argName.equals("JNZLabel")) {
                        jumpLabel = StringToLabel(argValue);
                        program.addLabel(jumpLabel.orElse(label));
                    }


                }


            }
            program.addInstruction(StringToInstruction(sInstuctionName, variable, jumpLabel.orElse(label)));


        }

        return program;
    }
}


