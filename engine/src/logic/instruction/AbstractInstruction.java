package logic.instruction;

import logic.Variable.Variable;
import logic.label.FixedLabel;
import logic.label.Label;

public abstract class AbstractInstruction implements Instruction {
    private final InstructionData instructionData;
    private final Label label;
    private final Variable variable;
    public InstructionType type;


    public AbstractInstruction(InstructionData instructionData, Variable variable) {
        this(instructionData, variable, FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData instructionData, Variable variable, Label label) {
        this.instructionData = instructionData;
        this.label = label;
        this.variable = variable;
    }

    @Override
    public String getName() {
        return instructionData.getName();
    }

    @Override
    public int cycles() {
        return instructionData.getCycles();
    }

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public Variable getVariable() {
        return variable;
    }
    @Override
    public InstructionType getType() {
        return type;
    }
    @Override
    public String commandDisplay(){
       return "";
    }
}
