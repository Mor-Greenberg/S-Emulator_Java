package servlets;

import logic.execution.ExecutionContextImpl;
import logic.program.Program;

import java.util.ArrayList;
import java.util.List;

public class ServerState {
    private static final ServerState instance = new ServerState();

    // Holds all uploaded programs
    private final List<Program> allPrograms = new ArrayList<>();

    // Shared execution context
    private final ExecutionContextImpl context = new ExecutionContextImpl();

    private ServerState() {}

    // Singleton instance getter
    public static ServerState getInstance() {
        return instance;
    }

    // Returns a copy of the list of all programs
    public List<Program> getPrograms() {
        return new ArrayList<>(allPrograms);
    }

    // Adds a new program to the system
    public void addProgram(Program program) {
        allPrograms.add(program);
    }

    // Checks if a main program with the same name already exists
    public boolean hasProgramWithMainName(String mainName) {
        return allPrograms.stream()
                .anyMatch(p -> p.getName().equals(mainName)); // Adjusted to match ProgramImpl structure
    }

    // Checks if any function with the given name exists in all programs
    public boolean hasFunctionNamed(String funcName) {
        return allPrograms.stream()
                .flatMap(p -> p.getFunctionMap().values().stream())
                .anyMatch(f -> f.getName().equals(funcName));
    }

    // Returns the shared execution context
    public ExecutionContextImpl getContext() {
        return context;
    }
}
