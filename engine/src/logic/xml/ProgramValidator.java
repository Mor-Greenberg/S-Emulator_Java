package logic.xml;

import logic.instruction.Instruction;
import logic.instruction.QuoteInstruction;
import logic.program.Program;

import java.util.*;

public class ProgramValidator {

    /**
     * Validates a newly uploaded main program and its functions according to the rules of Exercise 3.
     *
     * @param newProgram     The new main program that was uploaded.
     * @param allPrograms    All existing main programs in the system.
     * @param newFunctionMap The function map included in the uploaded file.
     * @return null if valid; otherwise, an error message explaining why the program is invalid.
     */
    public static String validate(Program newProgram,
                                  List<Program> allPrograms,
                                  Map<String, Program> newFunctionMap) {

        // Collect all existing main program names
        Set<String> existingProgramNames = new HashSet<>();
        for (Program p : allPrograms) {
            existingProgramNames.add(p.getName());
        }

        // Rule 1: Main program name must be unique
        if (existingProgramNames.contains(newProgram.getName())) {
            return "A program with the name \"" + newProgram.getName() + "\" already exists in the system.";
        }

        // Rule 2: The program must only use functions that already exist in the system
        Set<String> availableFunctionNames = new HashSet<>();
        for (Program p : allPrograms) {
            availableFunctionNames.addAll(p.getFunctionMap().keySet());
        }

        // Include the new functions (they are not part of the system yet, but should be valid to call)
        availableFunctionNames.addAll(newFunctionMap.keySet());

        for (Instruction instr : newProgram.getInstructions()) {
            if (instr instanceof QuoteInstruction q) {
                String calledFunction = q.getQuotedFunctionName();
                if (!availableFunctionNames.contains(calledFunction)) {
                    return "The program uses a function named \"" + calledFunction + "\" which is not defined in the system.";
                }
            }
        }

        // Rule 3: The uploaded file must not redefine an existing function
        for (Program p : allPrograms) {
            Set<String> existingFunctionNames = p.getFunctionMap().keySet();
            for (String newFuncName : newFunctionMap.keySet()) {
                if (existingFunctionNames.contains(newFuncName)) {
                    return "The file contains a function named \"" + newFuncName + "\" which is already defined in the system.";
                }
            }
        }

        // All validations passed
        return null;
    }
}
