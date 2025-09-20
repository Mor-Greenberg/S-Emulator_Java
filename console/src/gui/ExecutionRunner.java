package gui;

import handleExecution.HandleExecution;
import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.execution.ProgramExecutorImpl;
import logic.history.RunHistoryEntry;
import logic.program.Program;
import printExpand.expansion.PrintExpansion;
import programDisplay.ProgramDisplayImpl;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;

import java.util.*;

public class ExecutionRunner {

    private static Map<Variable, Long> lastVariableState = new HashMap<>();


    private static int runCounter = 1;
    private static final List<RunHistoryEntry> history = new ArrayList<>();

    public static void runProgram(Program program, ProgramDisplayImpl programDisplay) {
        if (program == null) {
            System.out.println("Program is not loaded, returning");
            return;
        }

        int degree = program.askForDegree();

        Map<Variable, Long> variableState = new HashMap<>();
        ExecutionContext context = new ExecutionContextImpl(variableState);
        program.expandToDegree(degree, context);
        Program expandedProgram = program;

        HandleExecution handleExecution = new HandleExecution(expandedProgram);
        handleExecution.collectInputFromUserFX(expandedProgram, context);

        ProgramExecutorImpl executor = new ProgramExecutorImpl(expandedProgram);
        long result = executor.run(context);

        System.out.println("Instructions activated:");
        programDisplay.printInstructions(program.getActiveInstructions());

        if (degree != 0) {
            System.out.println("Instructions expanded:");
            PrintExpansion expansion = new PrintExpansion(expandedProgram);
            AbstractInstruction.resetIdCounter();
            expansion.printProgramWithOrigins(expandedProgram);
        }

        System.out.println("\nProgram result (y): " + result);

        System.out.println("Variable values:");
        variableState.entrySet().stream()
                .sorted(Comparator.comparing(e -> {
                    String name = e.getKey().getRepresentation();
                    if (name.equals("y")) return "0";
                    if (name.startsWith("x")) return "1" + name.substring(1);
                    if (name.startsWith("z")) return "2" + name.substring(1);
                    return name;
                }))
                .forEach(entry -> System.out.println(entry.getKey().getRepresentation() + " = " + entry.getValue()));

        int sumCycles=program.calculateCycles();

        System.out.println("Number of cycles: " + sumCycles);

        RunHistoryEntry entry = new RunHistoryEntry(runCounter++, degree,
                handleExecution.getInputsMap(), result, sumCycles);
        history.add(entry);
        lastVariableState = new HashMap<>(variableState);

    }

    public static List<RunHistoryEntry> getHistory() {
        return history;
    }
    public static Map<Variable, Long> getExecutionContextMap() {
        return lastVariableState;
    }

}
