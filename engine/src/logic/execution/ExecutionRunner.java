package logic.execution;

import dto.UserRunEntryDTO;
import handleExecution.HandleExecution;
import javafx.scene.control.ChoiceDialog;
import logic.architecture.ArchitectureData;
import logic.architecture.ArchitectureRules;
import logic.history.RunHistoryEntry;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.program.Program;
import logic.Variable.Variable;
import printExpand.expansion.PrintExpansion;
import session.UserSession;
import ui.dashboard.DashboardController;
import ui.dashboard.UserHistory;
import ui.executionBoard.ExecutionBoardController;
import ui.executionBoard.instructionTable.InstructionRow;
import ui.guiUtils.DegreeDialog;
import user.User;
import utils.UiUtils;

import javafx.application.Platform;
import javafx.scene.control.TableView;
import java.util.*;

import static logic.architecture.HandleArch.ensureArchitectureSelected;
import static logic.blaxBox.BlackBox.blackBoxStepDegree0;
import static logic.blaxBox.BlackBox.executeBlackBox;
import static utils.Utils.generateSummary;

public class ExecutionRunner {

    private static RunCompletionListener runCompletionListener;
    public static void setRunCompletionListener(RunCompletionListener l) { runCompletionListener = l; }

    private static Map<Variable, Long> lastInputsMap = new HashMap<>();
    private static HandleExecution debugHandleExecution;
    private static Map<Variable, Long> lastVariableState = new HashMap<>();
    private static int currentDegree = 0;
    private static int runCounter = 1;
    private static final List<RunHistoryEntry> history = new ArrayList<>();

    public static ArchitectureData architecture;
    private static List<Instruction> debugInstructions;
    private static int currentIndex;
    private static ExecutionContext debugContext;
    private static boolean debugMode = false;
    private static int executedCycles = 0;
    private static Program expandedProgram;
    // --- Debug internals for degree 0 black-box mode ---
    private static int bbPc = -1;
    private static final Map<String, Integer> bbLabelToIndex = new HashMap<>();


    // === Run completion DTO ===
    private static UserRunEntryDTO buildDto(Program program, RunHistoryEntry entry) {
        String runType = (program != null && program.isMain()) ? "Program" : "Function";
        return new UserRunEntryDTO(
                UserSession.getUsername(),
                entry.getRunId(),
                runType,
                (program != null ? program.getName() : "UNKNOWN"),
                architecture,
                entry.getDegree(),
                entry.getResult(),
                entry.getCycles()
        );
    }

    private static final Set<Integer> notifiedRuns = new HashSet<>();

    private static int sumCycles(List<Instruction> instrs) {
        int sum = 0;
        for (Instruction i : instrs) sum += i.getCycles();
        return sum;
    }


    private static void notifyRunCompleted(Program program, RunHistoryEntry entry) {
        if (notifiedRuns.contains(entry.getRunId())) return;
        notifiedRuns.add(entry.getRunId());

        if (runCompletionListener != null)
            runCompletionListener.onRunCompleted(buildDto(program, entry));

        UserHistory.sendRunToServer(buildDto(program, entry));


        System.out.println("notifyRunCompleted called for " + program.getName());
        Platform.runLater(DashboardController::refreshProgramsFromServer);

    }


    private static List<Instruction> filterIllegalInstructions(Program program, List<Instruction> instrs) {
        if (architecture == null) {
            UiUtils.showError("Please select an architecture before running.");
            return instrs;
        }

        List<Instruction> allowed = new ArrayList<>();
        List<String> removed = new ArrayList<>();

        for (Instruction i : instrs) {
            InstructionData id = i.getData();
            if (ArchitectureRules.isAllowed(architecture, id)) {
                allowed.add(i);
            } else {
                String lbl = (i.getLabel() != null) ? i.getLabel().getLabelRepresentation() : "";
                removed.add(id.name() + (lbl.isEmpty() ? "" : " @" + lbl));
            }
        }

        if (!removed.isEmpty()) {
            UiUtils.showAlert(
                    "Some instructions were removed because they are not supported by " + architecture.name() + ":\n" +
                            String.join("\n", removed)
            );
        }

        return allowed;
    }




    // =====================================================
    // NORMAL RUN
    // =====================================================
    public static void runProgram(Program program) {
        ArchitectureData selected = ensureArchitectureSelected(architecture);
        if (selected == null) return;
        architecture = selected;

        Map<Variable, Long> variableState = new HashMap<>();
        ExecutionContextImpl context = new ExecutionContextImpl(variableState);
        context.setFunctionMap(program.getFunctionMap());
        currentDegree = resolveDegree(program, context);
        applyInputsToContext(program, context);

        int architectureCost = HandleCredits.prepareExecution(program, architecture);
        if (architectureCost < 0)
            return;


        if (currentDegree == 0) {
            List<Instruction> filtered = filterIllegalInstructions(program, program.getInstructions());
            long result = executeBlackBox(context, program);
            executedCycles = sumCycles(filtered);

            updateUIAfterExecution(program, context.getVariableState(), filtered);

            saveRunHistory(program, context, result, executedCycles, false);

            int totalUsedCredits = architectureCost + executedCycles;

            ExecutionBoardController.getInstance()
                    .notifyExecutionCompleted(program.getName(), totalUsedCredits);

            return;
        }


        program.expandToDegree(currentDegree, context);
        expandedProgram = program;
        List<Instruction> activeInstr = program.getActiveInstructions();
        activeInstr = filterIllegalInstructions(program, activeInstr);



        int totalCyclesUsed = 0;

        for (Instruction instr : activeInstr) {
            instr.execute(context);
            totalCyclesUsed += instr.getCycles();

            if (!HandleCredits.consumeCycles(program.getName(), instr.getCycles()))
                return;
        }

        updateUIAfterExecution(program, variableState, activeInstr);

        int totalUsedCredits = architectureCost + totalCyclesUsed;
        long result = context.getVariableState().getOrDefault(Variable.RESULT, 0L);

        saveRunHistory(program, context, result, totalCyclesUsed, false);
        UserSession.refreshCreditsFromServerAsync();

        ExecutionBoardController.getInstance()
                .notifyExecutionCompleted(program.getName(), totalUsedCredits);

    }


    // =====================================================
    // DEBUG MODE
    // =====================================================
    public static void startDebug(Program program) {
        debugMode = true;
        currentIndex = 0;
        executedCycles = 0;
        bbPc = 0;
        bbLabelToIndex.clear();

        ArchitectureData selected = ensureArchitectureSelected(architecture);
        if (selected == null) return;
        architecture = selected;

        Platform.runLater(() -> ExecutionBoardController.getInstance().clearInstructionTable());
        debugContext = new ExecutionContextImpl(new HashMap<>());
        debugContext.setFunctionMap(program.getFunctionMap());

        currentDegree = resolveDegree(program, debugContext);
        applyInputsToContext(program, debugContext);

        int architectureCost = HandleCredits.prepareExecution(program, architecture);
        if (architectureCost < 0)
            return;

        if (currentDegree == 0) {
            long result = executeBlackBox(debugContext, program);
            expandedProgram = program;
            debugInstructions = new ArrayList<>(filterIllegalInstructions(program, program.getInstructions()));
            setupDebugUI(debugInstructions);

            ExecutionBoardController.getInstance().updateCyclesView(executedCycles);
            return;
        }

        program.expandToDegree(currentDegree, debugContext);
        expandedProgram = program;
        debugInstructions = new ArrayList<>(filterIllegalInstructions(program, program.getInstructions()));
        setupDebugUI(debugInstructions);
    }


    public static void stepOver() {
        if (!debugMode) {
            UiUtils.showError("Not on debug mode");
            return;
        }

        // -------- Degree 0: black-box single step --------
        if (currentDegree == 0) {
            if (bbPc < 0 || bbPc >= debugInstructions.size()) return;

            Instruction instr = debugInstructions.get(bbPc);

            bbPc = blackBoxStepDegree0(instr, bbPc, bbLabelToIndex, debugInstructions, debugContext, expandedProgram);
            executedCycles += instr.getCycles();
            currentIndex = bbPc;

            boolean hasCredits = HandleCredits.consumeCycles(expandedProgram.getName(), instr.getCycles());
            if (!hasCredits) return; // stop if credits ran out

            Platform.runLater(() -> {
                ExecutionBoardController ctrl = ExecutionBoardController.getInstance();

                Platform.runLater(() -> {
                    int remaining = UserSession.getUserCredits();
                   ctrl.updateCreditsLabel(remaining);

                });

                int rowNumber = ctrl.getInstructionTable().getItems().size() + 1;

                InstructionRow row = new InstructionRow(
                        rowNumber,
                        instr.getType().toString(),
                        instr.getLabel() != null ? instr.getLabel().getLabelRepresentation() : "",
                        instr.commandDisplay(),
                        instr.getCycles(),
                        architecture.toString(),false
                );
                ctrl.addInstructionRow(row);
                ctrl.highlightCurrentInstruction(rowNumber - 1);
                ctrl.updateVariablesView();
                ctrl.updateCyclesView(executedCycles);
                //generateSummary(debugInstructions);


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
                saveRunHistory(expandedProgram, debugContext, result, executedCycles, true);
                generateSummary(debugInstructions);
                ExecutionBoardController.getInstance().updateCyclesView(executedCycles);
                UserSession.refreshCreditsFromServerAsync();



            }

            return;
        }

        //  Degree > 0: regular step over on expanded instructions
        if (currentIndex >= debugInstructions.size()) return;

        Instruction currentInstr = debugInstructions.get(currentIndex);
        currentInstr.execute(debugContext);
        executedCycles += currentInstr.getCycles();

        // Deduct credits here as well
        boolean hasCredits = HandleCredits.consumeCycles(expandedProgram.getName(), currentInstr.getCycles());
        if (!hasCredits) return; // if no credits left, stop execution

        Platform.runLater(() -> {
            ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
            Platform.runLater(() -> {
                int remaining = UserSession.getUserCredits();
               ctrl.updateCreditsLabel(remaining);

            });


            int rowNumber = ctrl.getInstructionTable().getItems().size() + 1;
            InstructionRow row = new InstructionRow(
                    rowNumber,
                    currentInstr.getType().toString(),
                    currentInstr.getLabel() != null ? currentInstr.getLabel().getLabelRepresentation() : "",
                    currentInstr.commandDisplay(),
                    currentInstr.getCycles(),
                    architecture.name(),false
            );
            ctrl.addInstructionRow(row);
            ctrl.highlightCurrentInstruction(rowNumber - 1);
            ctrl.updateVariablesView();
            ctrl.updateCyclesView(executedCycles);

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

                if (!HandleCredits.consumeCycles(expandedProgram.getName(), currentInstr.getCycles())) {
                    debugMode = false;
                    return;
                }

                int rowIndex = currentIndex;
                Platform.runLater(() -> {
                    ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
                    InstructionRow row = new InstructionRow(
                            rowIndex + 1,
                            currentInstr.getType().toString(),
                            currentInstr.getLabel() != null ? currentInstr.getLabel().getLabelRepresentation() : "",
                            currentInstr.commandDisplay(),
                            currentInstr.getCycles(),
                            architecture.name(),false
                    );
                    ctrl.addInstructionRow(row);
                    ctrl.highlightCurrentInstruction(rowIndex);
                    ctrl.updateVariablesView();
                    ctrl.updateCyclesView(executedCycles);

                    Platform.runLater(() -> {
                        int remaining = UserSession.getUserCredits();
                        ExecutionBoardController.getInstance().updateCreditsLabel(remaining);

                    });


                });

                currentIndex++;
                try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
            if (currentIndex >= debugInstructions.size()) {
                saveDebugHistory();
                UserSession.refreshCreditsFromServerAsync();
                generateSummary(debugInstructions);
                ExecutionBoardController.getInstance().updateSummaryLine(utils.Utils.generateSummary(debugInstructions));
                ExecutionBoardController.getInstance().updateCyclesView(executedCycles);


            }
        }).start();
    }
    public static void stop() {
        if (!debugMode) {
            UiUtils.showError("Not on debug mode");
            return;
        }
        debugMode = false;
        saveDebugHistory();
        UserSession.refreshCreditsFromServerAsync();
        generateSummary(debugInstructions);
        ExecutionBoardController.getInstance().updateSummaryLine(utils.Utils.generateSummary(debugInstructions));

    }


    // =====================================================
    // Helpers
    // =====================================================
    private static void updateUIAfterExecution(Program program, Map<Variable, Long> vars, List<Instruction> instructions) {
        Platform.runLater(() -> {
            ExecutionBoardController ctrl = ExecutionBoardController.getInstance();

            List<Instruction> filtered = new ArrayList<>(instructions);

            ctrl.setOriginalInstructions(filtered);
            ctrl.clearInstructionTable();

            int counter = 1;
            for (Instruction instr : filtered) {
                InstructionRow row = new InstructionRow(
                        counter++,
                        instr.getType().toString(),
                        instr.getLabel() != null ? instr.getLabel().getLabelRepresentation() : "",
                        instr.commandDisplay(),
                        instr.getCycles(),
                        architecture.name(),false
                );

                if (!ArchitectureRules.isAllowed(architecture, instr.getData())) {
                    row.setUnsupported(true);
                }
                row.setData(instr.getData());


                ctrl.addInstructionRow(row);
                ExecutionBoardController.getInstance().updateSummaryLine(utils.Utils.generateSummary(instructions));

            }



            ctrl.updateVariablesView();
            // Calculate how many instructions are supported by the selected architecture
            long supportedCount = 0;
            if (architecture != null) {
                supportedCount = instructions.stream()
                        .filter(instr -> {
                            var minArch = ArchitectureRules.getMinArchitectureFor(instr.getData());
                            return minArch != null && minArch.ordinal() <= architecture.ordinal();
                        })
                        .count();
            }

            generateSummary(instructions);
        });
    }

    private static void saveRunHistory(Program program, ExecutionContext context, long result, int cycles, boolean debug) {
        generateSummary(program.getInstructions());
        RunHistoryEntry entry = new RunHistoryEntry(runCounter++, currentDegree, lastInputsMap, result, cycles, debug);
        history.add(entry);

        notifyRunCompleted(program, entry);
        ExecutionBoardController.getInstance().updateCyclesView(executedCycles);


        int totalUsedCredits = architecture.getCreditsCost() + cycles;
        program.recordRun(totalUsedCredits);
        UserSession.getInstance().setLastArchitecture(architecture);
        lastVariableState = new HashMap<>(context.getVariableState());
    }


    private static void saveDebugHistory() {
        long result = debugContext.getVariableState().getOrDefault(Variable.RESULT, 0L);
        saveRunHistory(expandedProgram, debugContext, result, executedCycles, true);
        generateSummary(debugInstructions);
        ExecutionBoardController.getInstance().updateSummaryLine(utils.Utils.generateSummary(debugInstructions));
        ExecutionBoardController.getInstance().updateCyclesView(executedCycles);
        UserSession.getInstance().setLastArchitecture(architecture);
        User.incrementExecutionCount();
    }

    private static void setupDebugUI(List<Instruction> instructions) {
        Platform.runLater(() -> {
            ExecutionBoardController ctrl = ExecutionBoardController.getInstance();
            ctrl.setOriginalInstructions(instructions);
            ctrl.clearInstructionTable();
            ctrl.updateVariablesView();
            ctrl.updateCyclesView(0);
        });
    }




    private static int resolveDegree(Program program, ExecutionContext context) {
        return usePrefilledDegree ? prefilledDegree : DegreeDialog.askForDegree(context, program);
    }

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

    // === getters ===
    public static List<RunHistoryEntry> getHistory() { return history; }
    public static Map<Variable, Long> getExecutionContextMap() { return lastVariableState; }
    public static ExecutionContext getDebugContext() { return debugContext; }
    public static boolean isDebugMode() { return debugMode; }
    public static int getCurrentIndex() { return currentIndex; }
    public static int getCurrentDegree() {
        return currentDegree;
    }
    // =====================================================
// Prefill support for ReRunService
// =====================================================
    public static void setPrefilledDegree(int degree) {
        usePrefilledDegree = true;
        prefilledDegree = degree;
    }

    public static void setPrefilledInputs(Map<Variable, Long> inputs) {
        prefilledInputs = (inputs == null) ? null : new HashMap<>(inputs);
    }



}
