package logic.program;

import logic.Variable.Variable;
import logic.instruction.Instruction;
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

    public ProgramImpl(String name) {
        this.name = name;
        instructions = new ArrayList<>();
        variables = new HashSet<>();
        labels = new HashSet<>();
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
        // traverse all commands and find maximum degree
        return 0;
    }

    @Override
    public int calculateCycles() {
        // traverse all commands and calculate cycles
        return 0;
    }
    @Override
    public Set<Variable> getVars(){
        return variables;

    }
    @Override
    public Set<Label> getLabels(){
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





}
