package logic.xml;

import logic.instruction.*;
import logic.label.FixedLabel;
import logic.program.Program;

import java.io.File;
import java.util.*;

public class XmlValidation {


    public static int validateXmlFilePath(String path) {
        File file = new File(path);

        if (!path.toLowerCase().endsWith(".xml")) {
            return 2;
        }
        if (!file.exists()) {
            return 1;
        }
        return 3;
    }

    public static boolean validateLabels(List<Instruction> instructions,
                                         List<String> errors,
                                         String scope) {
        Set<String> definedLabels = new HashSet<>();
        Set<String> referencedLabels = new HashSet<>();

        for (Instruction instr : instructions) {
            if (instr.getLabel() != null && !instr.getLabel().equals(FixedLabel.EMPTY)) {
                String labelName = instr.getLabel().getLabelRepresentation();
                definedLabels.add(labelName);
            }

            if (instr instanceof GoToLabelInstruction gotoInstr) {
                String labelName = gotoInstr.getTargetLabel().getLabelRepresentation();
                referencedLabels.add(labelName);
            } else if (instr instanceof JumpNotZeroInstruction jnzInstr) {
                String labelName = jnzInstr.getTargetLabel().getLabelRepresentation();
                referencedLabels.add(labelName);
            } else if (instr instanceof JumpZeroInstruction jzInstr) {
                String labelName = jzInstr.getTargetLabel().getLabelRepresentation();
                referencedLabels.add(labelName);
            }
        }

        Set<String> specialLabels = Set.of("EXIT");

        for (String label : referencedLabels) {
            if (!definedLabels.contains(label) && !specialLabels.contains(label)) {
                String msg = scope + " uses undefined label: " + label;
                errors.add(msg);
            }
        }

        return errors.isEmpty();
    }


    public static boolean validateFunctions(Program mainProgram,
                                            List<Program> functions,
                                            List<String> errors) {
        Set<String> definedFunctions = new HashSet<>();
        for (Program func : functions) {
            definedFunctions.add(func.getName());
        }

        checkFunctionCalls(mainProgram, definedFunctions, "Main Program", errors);

        for (Program func : functions) {
            checkFunctionCalls(func, definedFunctions, "Function: " + func.getName(), errors);
        }

        return errors.isEmpty();
    }

    private static void checkFunctionCalls(Program program,
                                           Set<String> definedFunctions,
                                           String scope,
                                           List<String> errors) {
        for (Instruction instr : program.getInstructions()) {
            if (instr instanceof QuoteInstruction qi) {
                String functionName = qi.getQuotedFunctionName();
                if (!definedFunctions.contains(functionName)) {
                    errors.add(scope + " calls undefined function: " + functionName);
                }
            }
        }
    }

    public static void validateAll(String path,
                                   Program mainProgram,
                                   List<Program> functions) {
        List<String> errors = new ArrayList<>();

        int fileCheck = validateXmlFilePath(path);
        if (fileCheck == 1) {
            errors.add("XML file does not exist: " + path);
        } else if (fileCheck == 2) {
            errors.add("Invalid file type (must be .xml): " + path);
        }

        validateFunctions(mainProgram, functions, errors);

        validateLabels(mainProgram.getInstructions(), errors, "Main Program");

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("File validation failed:\n" + String.join("\n", errors));
        }
    }
}
