package logic.execution;

import gui.MainScreenController;
import handleExecution.HandleExecution;
import ui.executionBoard.ExecutionBoardController;
import ui.executionBoard.instructionTable.InstructionRow;
import javafx.application.Platform;
import javafx.scene.control.TableView;
import logic.Variable.Variable;
import logic.history.RunHistoryEntry;
import logic.program.Program;
import printExpand.expansion.PrintExpansion;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import ui.guiUtils.DegreeDialog;
import user.User;
import utils.UiUtils;

import java.util.*;

import static logic.blaxBox.BlackBox.blackBoxStepDegree0;
import static logic.blaxBox.BlackBox.executeBlackBox;

import static utils.Utils.generateSummary;

public class ExecutionRunner {

    private static Map<Variable, Long> lastInputsMap = new HashMap<>();
    private static HandleExecution debugHandleExecution;

    private static Map<Variable, Long> lastVariableState = new HashMap<>();
    private static int currentDegree = 0;
    private static int runCounter = 1;
    private static final List<RunHistoryEntry> history = new ArrayList<>();

    public static String architecture;
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

    public static void setPrefilledDegree(int degree) {
        usePrefilledDegree = true;
        prefilledDegree = degree;
    }

    public static void setPrefilledInputs(Map<Variable, Long> inputs) {
        prefilledInputs = (inputs == null) ? null : new HashMap<>(inputs);
    }

    private static int resolveDegree(Program program, ExecutionContext context) {
        if (usePrefilledDegree) {
            usePrefilledDegree = false;
            return prefilledDegree;
        }
        return DegreeDialog.askForDegree(context,program);
    }

    public static void runProgram(Program program) {
        Map<Variable, Long> variableState = new HashMap<>();
        ExecutionContextImpl context = new ExecutionContextImpl(variableState);
        context.setFunctionMap(program.getFunctionMap());

        currentDegree = resolveDegree(program, context);
        applyInputsToContext(program, context);

        // ---------------- Degree 0 (black-box) ----------------
        if (currentDegree == 0) {
            long result = executeBlackBox(context,program);
            System.out.println("Black-box result for y = " + result);

            debugContext = context;
            expandedProgram = program;
            debugInstructions = new ArrayList<>(program.getInstructions());

            Platform.runLater(() -> {
                ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
                ctrl.setOriginalInstructions(debugInstructions);
                ctrl.clearInstructionTable();

                int counter = 1;
                for (Instruction instr : debugInstructions) {
                    InstructionRow row = new InstructionRow(
                            counter++,
                            instr.getType().toString(),
                            instr.getLabel() != null ? instr.getLabel().getLabelRepresentation() : "",
                            instr.commandDisplay(),
                            instr.getCycles(),architecture
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
                    lastInputsMap,
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
            ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
            ctrl.setOriginalInstructions(activeInstr);
            ctrl.clearInstructionTable();

            int counter = 1;
            for (Instruction instr : activeInstr) {
                InstructionRow row = new InstructionRow(
                        counter++,
                        instr.getType().toString(),
                        instr.getLabel() != null ? instr.getLabel().getLabelRepresentation() : "",
                        instr.commandDisplay(),
                        instr.getCycles(),architecture
                );
                ctrl.addInstructionRow(row);
            }

            ctrl.updateVariablesView();

            int basicCount = (int) activeInstr.stream().filter(i -> i.getType().toString().equals("B")).count();
            int syntheticCount = (int) activeInstr.stream().filter(i -> i.getType().toString().equals("S")).count();
            int totalCycles = activeInstr.stream().mapToInt(Instruction::getCycles).sum();

            ctrl.updateSummaryView(activeInstr.size(), basicCount, syntheticCount, totalCycles);
            ctrl.updateCyclesView(totalCycles);
        });

        PrintExpansion expansion = new PrintExpansion(expandedProgram);
        AbstractInstruction.resetIdCounter();
        expansion.printProgramWithOrigins(expandedProgram);

        int sumCycles = program.calculateCycles();

        User uploader = null;
        if (program.getUploaderName() != null) {
            uploader = User.getManager().getUser(program.getUploaderName());
        }

        if (uploader != null) {
            uploader.trackExecution(program.getName(), sumCycles);
        } else {
            System.out.println("⚠️ No uploader found for program '" + program.getName() + "', skipping tracking.");
        }

        RunHistoryEntry entry = new RunHistoryEntry(
                runCounter++, currentDegree,
                lastInputsMap, result, sumCycles, false);
        history.add(entry);
        lastVariableState = new HashMap<>(variableState);
    }

    // ---------------- Debug ----------------
    private static int bbPc = -1;
    private static final Map<String, Integer> bbLabelToIndex = new HashMap<>();

    public static void startDebug(Program program) {
        debugMode = true;
        currentIndex = 0;
        executedCycles = 0;

        Platform.runLater(() -> ExecutionBoardController.getInstance().clearInstructionTable());

         debugContext = new ExecutionContextImpl(new HashMap<>());
        debugContext.setFunctionMap(program.getFunctionMap());


        currentDegree = resolveDegree(program, debugContext);
        applyInputsToContext(program, debugContext);

        if (currentDegree == 0) {
            long result = executeBlackBox(debugContext,program);
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
                ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
                ctrl.setOriginalInstructions(debugInstructions);
                ctrl.clearInstructionTable();
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
                    lastInputsMap, result, 0, true);
            history.add(entry);
            lastVariableState = new HashMap<>(debugContext.getVariableState());
            return;
        }

        program.expandToDegree(currentDegree, debugContext);
        expandedProgram = program;
        debugInstructions = new ArrayList<>(expandedProgram.getActiveInstructions());

        Platform.runLater(() -> {
            ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
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

        if (!debugMode){
            UiUtils.showError("Not on debug mode");
            return;
        }
        // -------- Degree 0: black-box single step --------
        if (currentDegree == 0) {
            if (bbPc < 0 || bbPc >= debugInstructions.size()) return;

            int executedIndex = bbPc;
            Instruction instr = debugInstructions.get(bbPc);

            bbPc = blackBoxStepDegree0(instr, bbPc, bbLabelToIndex, debugInstructions, debugContext, expandedProgram);
            executedCycles += instr.getCycles();
            currentIndex   = bbPc;

            Platform.runLater(() -> {
                ExecutionBoardController ctrl = ExecutionBoardController.getInstance();

                int rowNumber = ctrl.getInstructionTable().getItems().size() + 1;

                InstructionRow row = new InstructionRow(
                        rowNumber,
                        instr.getType().toString(),
                        instr.getLabel() != null ? instr.getLabel().getLabelRepresentation() : "",
                        instr.commandDisplay(),
                        instr.getCycles(),architecture
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
            ExecutionBoardController ctrl = ExecutionBoardController.getInstance();

            int rowNumber = ctrl.getInstructionTable().getItems().size() + 1;
            InstructionRow row = new InstructionRow(
                    rowNumber,
                    currentInstr.getType().toString(),
                    currentInstr.getLabel() != null ? currentInstr.getLabel().getLabelRepresentation() : "",
                    currentInstr.commandDisplay(),
                    currentInstr.getCycles(),architecture
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
                            currentInstr.getCycles(),architecture
                    );
                    ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
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
                saveDebugHistory();   // ← עכשיו בטוח לא יקרוס
                generateSummary(debugInstructions);
            }
        }).start();
    }

    private static void saveDebugHistory() {
        long result = debugContext.getVariableState().getOrDefault(Variable.RESULT, -1L);

        Map<Variable, Long> inputs;
        if (debugHandleExecution != null) {
            inputs = debugHandleExecution.getInputsMap();
        } else {
            inputs = lastInputsMap;
        }

        RunHistoryEntry entry = new RunHistoryEntry(
                runCounter++,
                currentDegree,
                inputs != null ? new HashMap<>(inputs) : Collections.emptyMap(),
                result,
                executedCycles,
                true
        );
        history.add(entry);
        User uploader = null;
        if (expandedProgram.getUploaderName() != null) {
            uploader = User.getManager().getUser(expandedProgram.getUploaderName());
        }

        if (uploader != null) {
            uploader.trackExecution(expandedProgram.getName(), executedCycles);
        } else {
            System.out.println("⚠️ No uploader found for program '" + expandedProgram.getName() + "', skipping tracking.");
        }

        lastVariableState = new HashMap<>(debugContext.getVariableState());
    }

    public static void stop() {
        if (!debugMode){
            UiUtils.showError("Not on debug mode");
            return;
        }
        debugMode = false;
        saveDebugHistory();
        generateSummary(debugInstructions);
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


    // --- Prefill support ---
    private static Map<Variable, Long> prefilledInputs = null;


    private static boolean usePrefilledDegree = false;
    private static int prefilledDegree = 0;


    private static void applyInputsToContext(Program program, ExecutionContext context) {
        if (prefilledInputs != null) {
            program.getVars().stream()
                    .filter(v -> v.getType() == logic.Variable.VariableType.INPUT)
                    .forEach(v -> {
                        Long val = prefilledInputs.get(v);
                        if (val != null) context.updateVariable(v, val);
                    });
            lastInputsMap = new HashMap<>(prefilledInputs);
            prefilledInputs = null;
        } else {
            HandleExecution handleExecution = new HandleExecution(program);
            handleExecution.collectInputFromUserFX(program, context);
            lastInputsMap = new HashMap<>(handleExecution.getInputsMap());
        }
    }

}
