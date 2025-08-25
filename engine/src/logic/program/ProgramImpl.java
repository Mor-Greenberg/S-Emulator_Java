package logic.program;

import logic.Variable.Variable;
import logic.instruction.Instruction;
import logic.label.Label;

import java.util.ArrayList;
import java.util.List;

public class ProgramImpl implements Program {
    private final String name;
    private final List<Instruction> instructions;
    private final List<Variable> variables;
    private final List<Label> labels;

    public ProgramImpl(String name, List<Variable> variables, List<Label> labels) {
        this.name = name;
        this.variables = variables;
        this.labels = labels;
        instructions = new ArrayList<>();
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
    public List<Variable> getVars(){
        return variables;

    }
    @Override
    public List<Label> getLabels(){
        return labels;
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
