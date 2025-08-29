package logic.program;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
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
           maxDegree = Math.max(maxDegree, instruction.getDegree());
       }
       return maxDegree;
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
    @Override
    public void expandToDegree(int maxDegree, ExecutionContext context) {
        List<AbstractInstruction> result = new ArrayList<>();

        for (Instruction inst : instructions) {
            if (inst instanceof AbstractInstruction abs) {
                // אם זו פקודה סינתטית והדרגה שלה פחות מהמקסימום - נרחיב
                if (abs.getType() == InstructionType.S && abs.getDegree() < maxDegree) {
                    List<AbstractInstruction> expandedList = abs.expand(context);

                    for (AbstractInstruction derived : expandedList) {
                        derived.setDegree(abs.getDegree() + 1);
                        derived.setOrigin(abs);
                        result.add(derived);
                    }

                } else {
                    // בסיסית או סינתטית בדרגה מספקת – נשאיר אותה כמות שהיא
                    result.add(abs);
                }

            } else {
                // אם זו פקודה שלא יורשת מ־AbstractInstruction – נתעלם או נטפל אחרת
                throw new IllegalStateException("Instruction does not extend AbstractInstruction: " + inst.getClass());
            }
        }

        expandedInstructions = result;
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
    @Override
    public Program expandOnce() {
        ProgramImpl expandedProgram = new ProgramImpl(this.name);

        for (Instruction instr : instructions) {
            if (instr.getType() == InstructionType.B) {
                expandedProgram.addInstruction(instr);
            } else if (instr instanceof AbstractInstruction absInstr) {
                List<AbstractInstruction> expandedInstructions = absInstr.expand(null);
                for (AbstractInstruction expanded : expandedInstructions) {
                    expanded.setOrigin(absInstr);
                    expanded.setDegree(absInstr.getDegree() + 1);
                    expandedProgram.addInstruction(expanded);
                }
            } else {
                throw new IllegalStateException("Instruction is not instance of AbstractInstruction: " + instr.getClass());
            }
        }


        return expandedProgram;
    }

    @Override
    public Program expandToDegree(int maxDegree) {
        Program current = this;

        while (current.hasSyntheticInstructions() && current.calculateMaxDegree() < maxDegree) {
            current = current.expandOnce();
        }

        return current;
    }









}
