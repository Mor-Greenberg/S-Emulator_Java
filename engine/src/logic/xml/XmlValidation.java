package logic.xml;

import logic.instruction.*;
import logic.label.FixedLabel;
import logic.program.Program;
import serverProgram.GlobalProgramStore;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class XmlValidation {

    public static void validateAll(String path,
                                   Program mainProgram,
                                   List<Program> newFunctions,
                                   Map<String, Program> globalProgramsMap) {


        List<String> errors = new ArrayList<>();

        if (path != null && !path.equalsIgnoreCase("FromString")) {
            int fileCheck = validateXmlFilePath(path);
            if (fileCheck == 1) errors.add("XML file does not exist: " + path);
            else if (fileCheck == 2) errors.add("Invalid file type (must be .xml): " + path);
        }

        if (globalProgramsMap.containsKey(mainProgram.getName())) {
            errors.add("Main program name '" + mainProgram.getName() + "' already exists in the system.");
        }

        Set<String> calledFunctions = mainProgram.getFunctionRefs();
        Set<String> definedFunctionNames = newFunctions.stream()
                .map(Program::getName)
                .collect(Collectors.toSet());

        for (String func : calledFunctions) {
            if (!globalProgramsMap.containsKey(func) && !definedFunctionNames.contains(func)) {
                errors.add("Main program calls undefined function: " + func);
            }
        }

        for (Program func : newFunctions) {
            if (globalProgramsMap.containsKey(func.getName())) {
                errors.add("Function '" + func.getName() + "' already exists in the system.");
            }
        }

        validateLabels(mainProgram.getInstructions(), errors, "Main Program");
        for (Program func : newFunctions) {
            validateLabels(func.getInstructions(), errors, "Function '" + func.getName() + "'");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("File validation failed:\n" + String.join("\n", errors));
        }
    }


    public static int validateXmlFilePath(String path) {
        File file = new File(path);
        if (!path.toLowerCase().endsWith(".xml")) return 2;
        if (!file.exists()) return 1;
        return 3;
    }

    public static boolean validateLabels(List<Instruction> instructions,
                                         List<String> errors,
                                         String scope) {
        Set<String> defined = new HashSet<>();
        Set<String> referenced = new HashSet<>();

        for (Instruction instr : instructions) {
            if (instr.getLabel() != null && !instr.getLabel().equals(FixedLabel.EMPTY)) {
                defined.add(instr.getLabel().getLabelRepresentation());
            }

            if (instr instanceof GoToLabelInstruction g)
                referenced.add(g.getTargetLabel().getLabelRepresentation());
            else if (instr instanceof JumpZeroInstruction jz)
                referenced.add(jz.getTargetLabel().getLabelRepresentation());
            else if (instr instanceof JumpNotZeroInstruction jnz)
                referenced.add(jnz.getTargetLabel().getLabelRepresentation());
        }

        for (String label : referenced) {
            if (!defined.contains(label) && !label.equals("EXIT")) {
                errors.add(scope + " uses undefined label: " + label);
            }
        }
        return errors.isEmpty();
    }
}
