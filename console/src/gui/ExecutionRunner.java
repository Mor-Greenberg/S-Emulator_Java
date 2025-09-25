package gui;

import gui.instructionTable.InstructionRow;
import gui.variablesTable.VariableRow;
import handleExecution.HandleExecution;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.TableView;
import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.execution.ProgramExecutorImpl;
import logic.history.RunHistoryEntry;
import logic.program.Program;
import printExpand.expansion.PrintExpansion;
import programDisplay.ProgramDisplayImpl;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class ExecutionRunner {

    private static Map<Variable, Long> lastInputsMap = new HashMap<>();

    private static HandleExecution debugHandleExecution;

    private static Map<Variable, Long> lastVariableState = new HashMap<>();
    private static int currentDegree = 0;
    private static int runCounter = 1;
    private static final List<RunHistoryEntry> history = new ArrayList<>();

    // 🟢 Debug fields
    private static List<Instruction> debugInstructions;
    private static int currentIndex;
    private static ExecutionContext debugContext;
    private static boolean debugMode = false;
    private static boolean stopped = false;
    private static int executedCycles = 0;
    private static Program expandedProgram;


    public static int getCurrentDegree() {
        return currentDegree;
    }

    public static void runProgram(Program program, ProgramDisplayImpl programDisplay) {



        Map<Variable, Long> variableState = new HashMap<>();
        ExecutionContext context = new ExecutionContextImpl(variableState,program.getFunctionMap());
        currentDegree = program.askForDegree(context);

        program.expandToDegree(currentDegree, context);
        Program expandedProgram = program;

        HandleExecution handleExecution = new HandleExecution(expandedProgram);
        handleExecution.collectInputFromUserFX(expandedProgram, context);
        lastInputsMap = new HashMap<>(handleExecution.getInputsMap());


        ProgramExecutorImpl executor = new ProgramExecutorImpl(expandedProgram);
        long result = executor.run(context);

        programDisplay.printInstructions(program.getActiveInstructions());

        if (currentDegree != 0) {
            PrintExpansion expansion = new PrintExpansion(expandedProgram);
            AbstractInstruction.resetIdCounter();
            expansion.printProgramWithOrigins(expandedProgram);
        }



        int sumCycles = program.calculateCycles();


        RunHistoryEntry entry = new RunHistoryEntry(runCounter++, currentDegree,
                handleExecution.getInputsMap(), result, sumCycles, false);
        history.add(entry);
        lastVariableState = new HashMap<>(variableState);
    }

    // ---------------- Debug ----------------


    public static void startDebug(Program program) {
        debugMode = true;
        currentIndex = 0;
        executedCycles = 0;


        debugContext = new ExecutionContextImpl(new HashMap<>(),program.getFunctionMap());

        currentDegree = program.askForDegree(debugContext);
        debugHandleExecution = new HandleExecution(program);
        debugHandleExecution.collectInputFromUserFX(program, debugContext);

        program.expandToDegree(currentDegree, debugContext);
        expandedProgram = program;
        debugInstructions = new ArrayList<>(expandedProgram.getActiveInstructions());

        Platform.runLater(() -> {
            MainScreenController ctrl = MainScreenController.getInstance();
            ctrl.setOriginalInstructions(debugInstructions);
            ctrl.clearInstructionTable(); // מתחילים טבלה ריקה
            ctrl.updateVariablesView();
            ctrl.updateCyclesView(0);
        });
    }




    public static void stepOver() {
        if (!debugMode || currentIndex >= debugInstructions.size()) return;

        Instruction instr = debugInstructions.get(currentIndex);

        int rowNumber = currentIndex + 1;
        Instruction currentInstr = debugInstructions.get(currentIndex);
        currentInstr.execute(debugContext);
        executedCycles += currentInstr.getCycles();

        Platform.runLater(() -> {
            InstructionRow row = new InstructionRow(
                    rowNumber,
                    currentInstr.getType().toString(),
                    currentInstr.getLabel().getLabelRepresentation(),
                    currentInstr.commandDisplay(),
                    currentInstr.getCycles()
            );
            MainScreenController.getInstance().addInstructionRow(row);
            MainScreenController.getInstance().highlightCurrentInstruction(currentIndex);
            MainScreenController.getInstance().updateVariablesView();
            MainScreenController.getInstance().updateCyclesView(executedCycles);
        });

        currentIndex++;

    }


    public static int getExecutedCycles() {
        return executedCycles;
    }

    public static void resume() {
        if (!debugMode || currentIndex >= debugInstructions.size()) {
            return;
        }

        new Thread(() -> {
            while (debugMode && currentIndex < debugInstructions.size()) {
                Instruction instr = debugInstructions.get(currentIndex);

                int rowNumber = currentIndex + 1;
                Instruction currentInstr = debugInstructions.get(currentIndex);
                currentInstr.execute(debugContext);
                executedCycles += currentInstr.getCycles();

                final int rowIndex = currentIndex;

                Platform.runLater(() -> {
                    InstructionRow row = new InstructionRow(
                            rowNumber,
                            currentInstr.getType().toString(),
                            currentInstr.getLabel().getLabelRepresentation(),
                            currentInstr.commandDisplay(),
                            currentInstr.getCycles()
                    );
                    MainScreenController.getInstance().addInstructionRow(row);
                    MainScreenController.getInstance().highlightCurrentInstruction(rowIndex);
                    MainScreenController.getInstance().updateVariablesView();
                    MainScreenController.getInstance().updateCyclesView(executedCycles);
                });

                currentIndex++;

                try {
                    Thread.sleep(0); // 🟢 השהייה קצרה כדי לראות את ההתקדמות (0.4 שניות)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (currentIndex >= debugInstructions.size()) {
                // להפעיל את הפקודה האחרונה אם צריך
                if (currentIndex > 0 && currentIndex <= debugInstructions.size()) {
                    Instruction last = debugInstructions.get(currentIndex - 1);
                    last.execute(debugContext); // לוודא שהתוצאה עודכנה
                }

                long result;
                if (debugContext.getVariableState().containsKey(Variable.RESULT)) {
                    result = debugContext.getVariableValue(Variable.RESULT);
                    System.out.println("✔ RESULT = " + result);
                } else {
                    result = -1;
                    System.out.println("❌ RESULT not found in context.");
                }

                RunHistoryEntry entry = new RunHistoryEntry(
                        runCounter++,
                        currentDegree,
                        debugHandleExecution.getInputsMap(),
                        result,
                        executedCycles,
                        true
                );
                history.add(entry);
                lastVariableState = new HashMap<>(debugContext.getVariableState());
            }

        }).start();
    }
    public static void stop() {
        debugMode = false;
    }


    public static List<RunHistoryEntry> getHistory() {
        return history;
    }

    public static Map<Variable, Long> getExecutionContextMap() {
        return lastVariableState;
    }

    public static ExecutionContext getDebugContext() {
        return debugContext;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }
    public static int getCurrentIndex() {
        return currentIndex;
    }


    public static void highlightCurrentInstruction(int index, TableView<InstructionRow> instructionTable) {
        instructionTable.getSelectionModel().clearAndSelect(index);
        instructionTable.scrollTo(index);
    }


}
