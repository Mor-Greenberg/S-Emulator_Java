package gui;

import gui.instructionTable.InstructionRow;
import handleExecution.HandleExecution;
import javafx.application.Platform;
import javafx.scene.control.TableView;
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

    private static Map<Variable, Long> lastInputsMap = new HashMap<>();
    private static HandleExecution debugHandleExecution;

    private static Map<Variable, Long> lastVariableState = new HashMap<>();
    private static int currentDegree = 0;
    private static int runCounter = 1;
    private static final List<RunHistoryEntry> history = new ArrayList<>();

    // Debug fields
    private static List<Instruction> debugInstructions;
    private static int currentIndex;
    private static ExecutionContext debugContext;
    private static boolean debugMode = false;
    private static int executedCycles = 0;
    private static Program expandedProgram;

    public static int getCurrentDegree() {
        return currentDegree;
    }

    // ---------------- Normal Run ----------------
    public static void runProgram(Program program, ProgramDisplayImpl programDisplay) {
        Map<Variable, Long> variableState = new HashMap<>();
        ExecutionContext context = new ExecutionContextImpl(variableState, program.getFunctionMap());

        currentDegree = program.askForDegree(context);

        HandleExecution handleExecution = new HandleExecution(program);
        handleExecution.collectInputFromUserFX(program, context);
        lastInputsMap = new HashMap<>(handleExecution.getInputsMap());

        // ---------------- Degree 0 (black-box) ----------------
        if (currentDegree == 0) {
            long result = program.executeBlackBox(context);
            System.out.println("Black-box result for y = " + result);

            debugContext = context;
            expandedProgram = program;
            debugInstructions = new ArrayList<>(program.getInstructions());

            Platform.runLater(() -> {
                MainScreenController ctrl = MainScreenController.getInstance();
                ctrl.setOriginalInstructions(debugInstructions);
                ctrl.clearInstructionTable();

                int counter = 1;
                for (Instruction instr : debugInstructions) {
                    InstructionRow row = new InstructionRow(
                            counter++,
                            instr.getType().toString(),
                            instr.getLabel() != null ? instr.getLabel().getLabelRepresentation() : "",
                            instr.commandDisplay(),
                            instr.getCycles()
                    );
                    ctrl.addInstructionRow(row);
                }

                ctrl.updateVariablesView();

                int basicCount = (int) debugInstructions.stream()
                        .filter(i -> i.getType().toString().equals("B"))
                        .count();
                int syntheticCount = (int) debugInstructions.stream()
                        .filter(i -> i.getType().toString().equals("S"))
                        .count();
                int totalCycles = debugInstructions.stream().mapToInt(Instruction::getCycles).sum();

                ctrl.updateSummaryView(debugInstructions.size(), basicCount, syntheticCount, totalCycles);
                ctrl.updateCyclesView(totalCycles);
            });

            RunHistoryEntry entry = new RunHistoryEntry(
                    runCounter++, 0,
                    handleExecution.getInputsMap(),
                    result,
                    debugInstructions.stream().mapToInt(Instruction::getCycles).sum(),
                    false
            );
            history.add(entry);
            lastVariableState = new HashMap<>(context.getVariableState());
            return;
        }

        // ---------------- Degree > 0 ----------------
        program.expandToDegree(currentDegree, context);
        expandedProgram = program;

        ProgramExecutorImpl executor = new ProgramExecutorImpl(expandedProgram);
        long result = executor.run(context);

        List<Instruction> activeInstr = program.getActiveInstructions();

        Platform.runLater(() -> {
            MainScreenController ctrl = MainScreenController.getInstance();
            ctrl.setOriginalInstructions(activeInstr);
            ctrl.clearInstructionTable();

            int counter = 1;
            for (Instruction instr : activeInstr) {
                InstructionRow row = new InstructionRow(
                        counter++,
                        instr.getType().toString(),
                        instr.getLabel() != null ? instr.getLabel().getLabelRepresentation() : "",
                        instr.commandDisplay(),
                        instr.getCycles()
                );
                ctrl.addInstructionRow(row);
            }

            ctrl.updateVariablesView();

            int basicCount = (int) activeInstr.stream()
                    .filter(i -> i.getType().toString().equals("B"))
                    .count();
            int syntheticCount = (int) activeInstr.stream()
                    .filter(i -> i.getType().toString().equals("S"))
                    .count();
            int totalCycles = activeInstr.stream().mapToInt(Instruction::getCycles).sum();

            ctrl.updateSummaryView(activeInstr.size(), basicCount, syntheticCount, totalCycles);
            ctrl.updateCyclesView(totalCycles);
        });

        PrintExpansion expansion = new PrintExpansion(expandedProgram);
        AbstractInstruction.resetIdCounter();
        expansion.printProgramWithOrigins(expandedProgram);

        int sumCycles = program.calculateCycles();

        RunHistoryEntry entry = new RunHistoryEntry(
                runCounter++, currentDegree,
                handleExecution.getInputsMap(), result, sumCycles, false);
        history.add(entry);
        lastVariableState = new HashMap<>(variableState);
    }

    // ---------------- Debug ----------------
    private static int bbPc = -1;
    private static Map<String, Integer> bbLabelToIndex = new HashMap<>();

    public static void startDebug(Program program) {
        debugMode = true;
        currentIndex = 0;
        executedCycles = 0;

        Platform.runLater(() -> MainScreenController.getInstance().clearInstructionTable());

        debugContext = new ExecutionContextImpl(new HashMap<>(), program.getFunctionMap());

        currentDegree = program.askForDegree(debugContext);
        debugHandleExecution = new HandleExecution(program);
        debugHandleExecution.collectInputFromUserFX(program, debugContext);

        if (currentDegree == 0) {
            long result = program.executeBlackBox(debugContext);
            System.out.println("Black-box result for y = " + result);

            expandedProgram = program;
            debugInstructions = new ArrayList<>(program.getInstructions());
            bbLabelToIndex.clear();
            for (int i = 0; i < debugInstructions.size(); i++) {
                var lbl = debugInstructions.get(i).getLabel();
                if (lbl != null && lbl != logic.label.FixedLabel.EMPTY) {
                    bbLabelToIndex.put(lbl.getLabelRepresentation(), i);
                }
            }
            bbPc = 0;


            Platform.runLater(() -> {
                MainScreenController ctrl = MainScreenController.getInstance();
                ctrl.setOriginalInstructions(debugInstructions);
                ctrl.clearInstructionTable();     // ← להתחיל מטבלה ריקה
                ctrl.updateVariablesView();
                ctrl.updateCyclesView(0);
                ctrl.updateSummaryView(
                        debugInstructions.size(),
                        (int) debugInstructions.stream().filter(i -> i.getType().toString().equals("B")).count(),
                        (int) debugInstructions.stream().filter(i -> i.getType().toString().equals("S")).count(),
                        0
                );

                if (!debugInstructions.isEmpty()) {
                    ctrl.highlightCurrentInstruction(0);
                }
            });

            RunHistoryEntry entry = new RunHistoryEntry(
                    runCounter++, 0,
                    debugHandleExecution.getInputsMap(), result, 0, true);
            history.add(entry);
            lastVariableState = new HashMap<>(debugContext.getVariableState());

            return;
        }

        program.expandToDegree(currentDegree, debugContext);
        expandedProgram = program;
        debugInstructions = new ArrayList<>(expandedProgram.getActiveInstructions());

        Platform.runLater(() -> {
            MainScreenController ctrl = MainScreenController.getInstance();
            ctrl.setOriginalInstructions(debugInstructions);
            ctrl.clearInstructionTable();
            ctrl.updateVariablesView();
            ctrl.updateSummaryView(
                    debugInstructions.size(),
                    (int) debugInstructions.stream().filter(i -> i.getType().toString().equals("B")).count(),
                    (int) debugInstructions.stream().filter(i -> i.getType().toString().equals("S")).count(),
                    0
            );
            ctrl.updateCyclesView(0);
        });
    }

    public static void stepOver() {
        if (!debugMode) return;

        // -------- Degree 0: black-box single step --------
        if (currentDegree == 0) {
            if (bbPc < 0 || bbPc >= debugInstructions.size()) return;

            int executedIndex = bbPc;                 // אינדקס הפקודה שמריצים עכשיו
            Instruction instr = debugInstructions.get(bbPc);

            // מריצים צעד אחד ומקבלים PC הבא
            bbPc = blackBoxStepDegree0(instr, bbPc, bbLabelToIndex, debugInstructions, debugContext, expandedProgram);
            executedCycles += instr.getCycles();
            currentIndex   = bbPc;                    // לשמירת סנכרון כללי

            Platform.runLater(() -> {
                MainScreenController ctrl = MainScreenController.getInstance();

                // מספר שורה חדש בטבלת הביצוע (log) – שורה אחרונה + 1
                int rowNumber = ctrl.getInstructionTable().getItems().size() + 1;

                // מוסיפים שורה לטבלה
                InstructionRow row = new InstructionRow(
                        rowNumber,
                        instr.getType().toString(),
                        instr.getLabel() != null ? instr.getLabel().getLabelRepresentation() : "",
                        instr.commandDisplay(),
                        instr.getCycles()
                );
                ctrl.addInstructionRow(row);

                // מסמנים ומגלגלים לשורה שנוספה עכשיו
                ctrl.highlightCurrentInstruction(rowNumber - 1);

                // עדכוני UI
                ctrl.updateVariablesView();
                ctrl.updateCyclesView(executedCycles);
                ctrl.updateSummaryView(
                        debugInstructions.size(),
                        (int) debugInstructions.stream().filter(i -> i.getType().toString().equals("B")).count(),
                        (int) debugInstructions.stream().filter(i -> i.getType().toString().equals("S")).count(),
                        executedCycles
                );
            });

            // אם הגענו לסוף – שומרים להיסטוריה
            if (bbPc >= debugInstructions.size()) {
                long result = debugContext.getVariableState().getOrDefault(Variable.RESULT, -1L);
                RunHistoryEntry entry = new RunHistoryEntry(
                        runCounter++,
                        currentDegree,
                        debugHandleExecution != null ? debugHandleExecution.getInputsMap() : Collections.emptyMap(),
                        result,
                        executedCycles,
                        true
                );
                history.add(entry);
                lastVariableState = new HashMap<>(debugContext.getVariableState());
            }
            return;
        }

        // -------- Degree > 0: regular step over on expanded instructions --------
        if (currentIndex >= debugInstructions.size()) return;

        Instruction currentInstr = debugInstructions.get(currentIndex);
        currentInstr.execute(debugContext);
        executedCycles += currentInstr.getCycles();

        Platform.runLater(() -> {
            MainScreenController ctrl = MainScreenController.getInstance();

            int rowNumber = ctrl.getInstructionTable().getItems().size() + 1;
            InstructionRow row = new InstructionRow(
                    rowNumber,
                    currentInstr.getType().toString(),
                    currentInstr.getLabel() != null ? currentInstr.getLabel().getLabelRepresentation() : "",
                    currentInstr.commandDisplay(),
                    currentInstr.getCycles()
            );
            ctrl.addInstructionRow(row);
            ctrl.highlightCurrentInstruction(rowNumber - 1);

            ctrl.updateVariablesView();
            ctrl.updateCyclesView(executedCycles);
            ctrl.updateSummaryView(
                    debugInstructions.size(),
                    (int) debugInstructions.stream().filter(i -> i.getType().toString().equals("B")).count(),
                    (int) debugInstructions.stream().filter(i -> i.getType().toString().equals("S")).count(),
                    executedCycles
            );
        });

        currentIndex++;
    }


    public static void resume() {
        if (!debugMode || currentIndex >= debugInstructions.size()) return;

        new Thread(() -> {
            while (debugMode && currentIndex < debugInstructions.size()) {
                Instruction currentInstr = debugInstructions.get(currentIndex);
                currentInstr.execute(debugContext);
                executedCycles += currentInstr.getCycles();

                int rowNumber = currentIndex + 1;
                final int rowIndex = currentIndex;

                Platform.runLater(() -> {
                    InstructionRow row = new InstructionRow(
                            rowNumber,
                            currentInstr.getType().toString(),
                            currentInstr.getLabel() != null ? currentInstr.getLabel().getLabelRepresentation() : "",
                            currentInstr.commandDisplay(),
                            currentInstr.getCycles()
                    );
                    MainScreenController ctrl = MainScreenController.getInstance();
                    ctrl.addInstructionRow(row);
                    ctrl.highlightCurrentInstruction(rowIndex);
                    ctrl.updateVariablesView();
                    ctrl.updateCyclesView(executedCycles);
                });

                currentIndex++;

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (currentIndex >= debugInstructions.size()) {
                saveDebugHistory();
            }
        }).start();
    }

    private static void saveDebugHistory() {
        long result = debugContext.getVariableState().getOrDefault(Variable.RESULT, -1L);
        RunHistoryEntry entry = new RunHistoryEntry(
                runCounter++, currentDegree,
                debugHandleExecution.getInputsMap(), result, executedCycles, true
        );
        history.add(entry);
        lastVariableState = new HashMap<>(debugContext.getVariableState());
    }

    public static void stop() {
        if (!debugMode) return;
        debugMode = false;
        saveDebugHistory();
    }

    // ---------------- Getters ----------------
    public static List<RunHistoryEntry> getHistory() { return history; }
    public static Map<Variable, Long> getExecutionContextMap() { return lastVariableState; }
    public static ExecutionContext getDebugContext() { return debugContext; }
    public static boolean isDebugMode() { return debugMode; }
    public static int getCurrentIndex() { return currentIndex; }

    public static void highlightCurrentInstruction(int index, TableView<InstructionRow> instructionTable) {
        instructionTable.getSelectionModel().clearAndSelect(index);
        instructionTable.scrollTo(index);
    }
    private static int blackBoxStepDegree0(
            Instruction instr,
            int pc,
            Map<String, Integer> labelToIndex,
            List<Instruction> instrs,
            ExecutionContext context,
            Program program
    ) {
        String name = instr.getName();

        switch (name) {
            // שמות “מקוצרים” ו”מפורשים” – תומך בשניהם
            case "ASSIGNMENT" -> {
                logic.instruction.AssignmentInstruction a = (logic.instruction.AssignmentInstruction) instr;
                long val = context.getVariableValue(a.getSource());
                context.updateVariable(a.getDestination(), val);
                return pc + 1;
            }
            case "CONSTANT_ASSIGNMENT" -> {
                logic.instruction.ConstantAssignmentInstruction c = (logic.instruction.ConstantAssignmentInstruction) instr;
                context.updateVariable(c.getVariable(), c.getConstantValue());
                return pc + 1;
            }
            case "INCREASE" -> {
                long v = context.getVariableValue(instr.getVariable());
                context.updateVariable(instr.getVariable(), v + 1);
                return pc + 1;
            }
            case "DECREASE" -> {
                long v = context.getVariableValue(instr.getVariable());
                context.updateVariable(instr.getVariable(), v - 1);
                return pc + 1;
            }
            case "ZERO_VARIABLE" -> {
                context.updateVariable(instr.getVariable(), 0);
                return pc + 1;
            }

            case "JNZ", "JUMP_NOT_ZERO" -> {
                logic.instruction.JumpNotZeroInstruction j = (logic.instruction.JumpNotZeroInstruction) instr;
                long v = context.getVariableValue(j.getVariable());
                if (v != 0 && j.getJnzLabel() != logic.label.FixedLabel.EMPTY) {
                    return labelToIndex.getOrDefault(j.getJnzLabel().getLabelRepresentation(), pc + 1);
                }
                return pc + 1;
            }

            case "JZ","JUMP_ZERO" -> {
                    logic.instruction.JumpZeroInstruction j = (logic.instruction.JumpZeroInstruction) instr;
                    long v = context.getVariableValue(j.getVariable());
                    if (v == 0 && j.getJZLabel() != logic.label.FixedLabel.EMPTY) {
                        return labelToIndex.getOrDefault(j.getJZLabel().getLabelRepresentation(), pc + 1);
                    }
                    return pc + 1;
                }
                case "JUMP_EQUAL_CONSTANT" -> {
                    logic.instruction.JumpEqualConstantInstruction j = (logic.instruction.JumpEqualConstantInstruction) instr;
                    long v = context.getVariableValue(j.getVariable());
                    if (v == j.getConstantValue() && j.getJumpToLabel() != logic.label.FixedLabel.EMPTY) {
                        return labelToIndex.getOrDefault(j.getJumpToLabel().getLabelRepresentation(), pc + 1);
                    }
                    return pc + 1;
                }
                case "JUMP_EQUAL_VARIABLE" -> {
                    logic.instruction.JumpEqualVariableInstruction j = (logic.instruction.JumpEqualVariableInstruction) instr;
                    long v1 = context.getVariableValue(j.getVariable());
                    long v2 = context.getVariableValue(j.getVariableName());
                    if (v1 == v2 && j.getTargetLabel() != logic.label.FixedLabel.EMPTY) {
                        return labelToIndex.getOrDefault(j.getTargetLabel().getLabelRepresentation(), pc + 1);
                    }
                    return pc + 1;
                }
                case "GOTO_LABEL" -> {
                    logic.instruction.GoToLabelInstruction g = (logic.instruction.GoToLabelInstruction) instr;
                    if (g.getGoToLabel() == logic.label.FixedLabel.EXIT) {
                        return instrs.size();  // יציאה
                    }
                    return labelToIndex.getOrDefault(g.getGoToLabel().toString(), pc + 1);
                }

                case "QUOTE" -> {
                    logic.program.ProgramImpl tmp = new logic.program.ProgramImpl("step-quote");
                    tmp.setFunctionMap(program.getFunctionMap());
                    tmp.setVariables(program.getVars());
                    tmp.addInstruction(instr);
                    tmp.executeBlackBox(context);
                    return pc + 1;
                }

                default -> {
                    System.out.println(" Unsupported black-box step for: " + name + " (skipping)");
                    return pc + 1;
                }
        }
    }

}
