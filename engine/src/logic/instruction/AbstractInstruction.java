package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.List;

public abstract class AbstractInstruction implements Instruction {
    private final InstructionData instructionData;
    private Label label;
    private final Variable variable;
    protected InstructionType type;
    protected int degree;
    protected AbstractInstruction origin = null;


    public AbstractInstruction(InstructionData instructionData, Variable variable,InstructionType type) {
        this(instructionData, variable, FixedLabel.EMPTY, type);
    }

    public AbstractInstruction(InstructionData instructionData, Variable variable, Label label,InstructionType type) {
        this.instructionData = instructionData;
        this.label = label;
        this.variable = variable;
        this.type=type;
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
    @Override
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
    public List<AbstractInstruction> expand(ExecutionContext context){
        return null;
    }




}
