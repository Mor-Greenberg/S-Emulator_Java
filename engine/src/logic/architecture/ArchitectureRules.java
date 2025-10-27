package logic.architecture;

import logic.instruction.InstructionData;
import java.util.*;

import static logic.instruction.InstructionData.*;
import static logic.architecture.ArchitectureData.*;

public class ArchitectureRules {

    private static final Map<ArchitectureData, Set<InstructionData>> allowed = Map.of(
            I, Set.of(NO_OP, INCREASE, DECREASE, JUMP_NOT_ZERO),
            II, Set.of(NO_OP, INCREASE, DECREASE, JUMP_NOT_ZERO,
                    ZERO_VARIABLE, CONSTANT_ASSIGNMENT, GOTO_LABEL),
            III, Set.of(NO_OP, INCREASE, DECREASE, JUMP_NOT_ZERO,
                    ZERO_VARIABLE, CONSTANT_ASSIGNMENT, GOTO_LABEL,
                    ASSIGNMENT, JUMP_ZERO, JUMP_EQUAL_CONSTANT, JUMP_EQUAL_VARIABLE),
            IV, Set.of(InstructionData.values())
    );

    public static boolean isAllowed(ArchitectureData arch, InstructionData instr) {
        return allowed.getOrDefault(arch, Set.of()).contains(instr);
    }
    public static ArchitectureData getMinArchitectureFor(InstructionData instruction) {
        for (ArchitectureData arch : ArchitectureData.values()) {
            if (allowed.get(arch).contains(instruction)) {
                return arch;
            }
        }
        return null;
    }
}
