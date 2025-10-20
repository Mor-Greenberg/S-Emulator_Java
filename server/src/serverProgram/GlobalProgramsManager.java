package serverProgram;

import logic.program.Program;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class GlobalProgramsManager {
    private static final Map<String, Program> globalPrograms = new ConcurrentHashMap<>();

    public static void addProgram(Program program) {
        globalPrograms.put(program.getName(), program);
    }

    public static Program getProgram(String name) {
        return globalPrograms.get(name);
    }

    public static Map<String, Program> getAllPrograms() {
        return globalPrograms;
    }

    public static void clear() {
        globalPrograms.clear();
    }
}
