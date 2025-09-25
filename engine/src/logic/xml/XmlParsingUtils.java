package logic.xml;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.instruction.InstructionType;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.label.LabelImpl;

import java.util.Optional;

public class XmlParsingUtils {
    public static Variable parseVariable(String str) {
        char first = str.charAt(0);
        String rest = str.substring(1);
        int number = rest.isEmpty() ? 0 : Integer.parseInt(rest);

        return switch (first) {
            case 'x' -> new VariableImpl(VariableType.INPUT, number);
            case 'y' -> new VariableImpl(VariableType.RESULT);
            case 'z' -> new VariableImpl(VariableType.WORK, number);
            default -> throw new IllegalArgumentException("Unknown variable type: " + first);
        };
    }

    public static Optional<Label> parseLabel(String str) {
        if (str == null || str.isEmpty()) return Optional.empty();

        if ("EXIT".equals(str)) return Optional.of(FixedLabel.EXIT);

        if (str.charAt(0) != 'L') throw new IllegalArgumentException("Unknown label: " + str);

        int number = Integer.parseInt(str.substring(1));
        return Optional.of(new LabelImpl(number));
    }

    public static InstructionType parseInstructionType(String str) {
        return switch (str) {
            case "basic" -> InstructionType.B;
            case "synthetic" -> InstructionType.S;
            default -> throw new IllegalArgumentException("Unknown instruction type: " + str);
        };
    }
}
