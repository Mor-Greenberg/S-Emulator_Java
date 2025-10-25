package logic.program;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.instruction.*;
import logic.label.Label;
import serverProgram.ProgramStats;
import user.User;
import utils.Utils;

import java.util.*;


public class ProgramImpl implements Program {
    private final String name;
    public List<Instruction> instructions;
    public Set<Variable> variables;
    public Set<Label> labels;
    private List<AbstractInstruction> expandedInstructions = new ArrayList<>();


    public ProgramImpl(String name) {
        this.name = name;
        instructions = new ArrayList<>();
        variables = new HashSet<>();
        labels = new HashSet<>();

    }
    private String parentProgramName;

    @Override
    public void setParentProgramName(String parentProgramName) {
        this.parentProgramName = parentProgramName;
    }

    @Override
    public String getParentProgramName() {
        return parentProgramName != null ? parentProgramName : "N/A";
    }

    @Override
    public boolean isMain() {
        return parentProgramName != null && parentProgramName.equals("MAIN");
    }



    @Override
    public List <AbstractInstruction> getExpandedInstructions(){
        return expandedInstructions;
    }
    private Map<String, Program> functionMap = new HashMap<>();

    public void setFunctionMap(Map<String, Program> functionMap) {
        this.functionMap = functionMap;
    }

    public Map<String, Program> getFunctionMap() {
        return functionMap;
    }


    private boolean isFunction = false;

    @Override
    public boolean isFunction() {
        return isFunction;
    }

    @Override
    public void setFunction(boolean isFunction) {
        this.isFunction = isFunction;
    }
    private String uploaderName;

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }

    @Override
    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public boolean validate() {
        return false;
    }

    public int calculateMaxDegree() {
        int maxDegree = 0;

        for (Instruction instr : instructions) {
            if (instr instanceof QuoteInstruction q) {
                q.computeDegree();
                maxDegree = Math.max(maxDegree, q.getDegree());
            } else if (instr instanceof AbstractInstruction ai) {
                maxDegree = Math.max(maxDegree, ai.getDegree());
            }
        }

        return maxDegree;
    }

    public List<Instruction> getInstructionsLevel0() {
        return instructions.stream()
                .filter(instr -> instr instanceof AbstractInstruction ai && ai.getDegree() == 0)
                .toList();
    }


    public boolean isMainProgram() {
        return !isFunction;
    }


    @Override
    public ProgramStats toStats(User uploader) {
        ProgramStats stats = new ProgramStats();
        stats.setProgramName(this.name);
        stats.setUploaderName(uploader.getUsername());
        stats.setInstructionCount(this.instructions.size());
        stats.setMaxExpansionLevel(this.calculateMaxDegree());
        stats.setRunCount(uploader.getRunCountForProgram(this.name));
        stats.setAverageCredits(uploader.getAverageCreditsForProgram(this.name));
        return stats;
    }

    @Override
    public int calculateCycles() {
        if (expandedInstructions == null || expandedInstructions.isEmpty()) {
            return 0;
        }
        int cycles = 0;
        for (Instruction instruction : expandedInstructions) {
            cycles += instruction.getCycles();
        }
        return cycles;
    }


    @Override
    public Set<Variable> getVars() {
        return variables;

    }

    @Override
    public Set<Label> getLabels() {
        return labels;
    }

    public void setVariables(Set<Variable> variables) {
        this.variables = variables;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    @Override
    public void addVar(Variable variable) {
        this.variables.add(variable);
    }

    @Override
    public void addLabel(Label label) {
        this.labels.add(label);
    }

    @Override
    public int getNextIndexByLabel(Label nextLabel) {
        List<Instruction> instructions = getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            if (nextLabel.equals(instructions.get(i).getLabel())) {
                return i;
            }
        }
        throw new IllegalArgumentException("Label not found: " + nextLabel);

    }

    public void expandToDegree(int maxDegree, ExecutionContext context) {
        List<AbstractInstruction> result = new ArrayList<>();

        for (Instruction inst : instructions) {
            if (inst instanceof AbstractInstruction abs) {
                if (abs.getType() == InstructionType.S && abs.getDegree() <= maxDegree) {
                    List<AbstractInstruction> expandedList = abs.expand(context);
                    for (AbstractInstruction derived : expandedList) {
                        derived.setDegree(abs.getDegree() + 1);
                        derived.setOrigin(abs);
                        result.add(derived);
                    }
                } else {
                    result.add(abs);
                }

            } else {
                throw new IllegalStateException("Instruction does not extend AbstractInstruction: " + inst.getClass());
            }
        }

        this.expandedInstructions = result;
    }


    @Override
    public List<Instruction> getActiveInstructions() {
        return (expandedInstructions != null) ? new ArrayList<>(expandedInstructions) : instructions;
    }

    @Override
    public boolean hasSyntheticInstructions() {
        for (Instruction instr : instructions) {
            if (instr.getType() == InstructionType.S)
                return true;
        }
        return false;
    }




    public Map<Variable, Long> getVarsAsMapWithZeroes() {
        Map<Variable, Long> map = new HashMap<>();
        for (Variable var : getVars()) {
            map.put(var, 0L);
        }
        return map;
    }




    public Set<String> getFunctionRefs() {
        Set<String> refs = new HashSet<>();

        for (Instruction instr : getInstructions()) {
            if (instr instanceof QuoteInstruction qi) {
                refs.add(qi.getQuotedFunctionName());
            }
        }

        return refs;
    }
    private int runCount = 0;
    private double averageCredits = 0.0;

    public void recordRun(int usedCredits) {
        runCount++;
        averageCredits = ((averageCredits * (runCount - 1)) + usedCredits) / runCount;
    }

    public int getRunCount() {
        return runCount;
    }

    public double getAverageCredits() {
        return averageCredits;
    }





}
