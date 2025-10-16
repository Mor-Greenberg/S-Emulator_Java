package logic.instruction;

import logic.program.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * כלי עזר לעבודה עם פקודות בתוכנית.
 * אחראי לזהות שמות פונקציות שמופיעות בתוך פקודות.
 */
public class InstructionUtils {

    /**
     * מחלץ את כל שמות הפונקציות שהתוכנית עושה בהן שימוש.
     * כולל QUOTE, JUMP_EQUAL_FUNCTION, וקריאות ישירות.
     */
    public static List<String> extractCalledFunctions(Program program) {
        List<String> calledFunctions = new ArrayList<>();

        for (Instruction instr : program.getInstructions()) {
            if (instr instanceof QuoteInstruction quoteInstr) {
                addIfNotExists(calledFunctions, quoteInstr.getFunctionName());
            }
            else if (instr instanceof JumpEqualFunctionInstruction jumpEqFunc) {
                addIfNotExists(calledFunctions, jumpEqFunc.getFunctionName());
            }
        }

        return calledFunctions;
    }

    private static void addIfNotExists(List<String> list, String name) {
        if (name != null && !name.isBlank() && !list.contains(name)) {
            list.add(name);
        }
    }
}
