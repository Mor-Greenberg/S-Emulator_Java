package logic.program;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionType;
import logic.label.Label;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgramImpl implements Program {
    private final String name;
    public List<Instruction> instructions;
    public Set<Variable> variables;
    public Set<Label> labels;
    private List<AbstractInstruction> expandedInstructions = null;


    public ProgramImpl(String name) {
        this.name = name;
        instructions = new ArrayList<>();
        variables = new HashSet<>();
        labels = new HashSet<>();

    }

    @Override
    public List <AbstractInstruction> getExpandedInstructions(){
        return expandedInstructions;
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

    @Override
    public int calculateMaxDegree() {
        int maxDegree = 0;
        for (Instruction instruction : instructions) {
            instruction.getDegree();
            maxDegree = Math.max(maxDegree, instruction.getDegree()+1);
        }
        return maxDegree;
    }

    @Override
    public int calculateCycles() {
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
                if (abs.getType() == InstructionType.S && abs.getDegree() < maxDegree) {
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



}
