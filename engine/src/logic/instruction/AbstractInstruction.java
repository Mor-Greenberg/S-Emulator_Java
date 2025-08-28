package logic.instruction;

import logic.Variable.Variable;
import logic.label.FixedLabel;
import logic.label.Label;

public abstract class AbstractInstruction implements Instruction {
    private final InstructionData instructionData;
    private Label label;
    private final Variable variable;
    public InstructionType type;
    protected int degree;
    protected AbstractInstruction origin = null;


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
    public int getCycles() {
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
    public int getDegree() {
        return degree;
    }
    public void setDegree(int d) {
        degree = d;
    }

    public void setLabel(Label l) {
        this.label=l;
    }
    public void setOrigin(AbstractInstruction origin) {
        this.origin = origin;
    }

    public AbstractInstruction getOrigin() {
        return origin;
    }

    public boolean hasOrigin() {
        return origin != null;
    }
    protected void markAsDerivedFrom(AbstractInstruction derived, AbstractInstruction source) {
        derived.setOrigin(source);
    }




}
