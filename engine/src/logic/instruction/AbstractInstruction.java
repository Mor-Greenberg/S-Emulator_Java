package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;

import java.util.List;
import java.util.Map;

public abstract class AbstractInstruction implements Instruction {
    private final InstructionData instructionData;
    private Label label;
    Variable variable;
    protected InstructionType type;
    protected int degree;
    protected AbstractInstruction origin = null;
    private static int nextId = 1;
    private int uniqueId;



    public AbstractInstruction(InstructionData instructionData, Variable variable,InstructionType type) {
        this(instructionData, variable, FixedLabel.EMPTY, type);
    }

    public AbstractInstruction(InstructionData instructionData, Variable variable, Label label,InstructionType type) {
        this.instructionData = instructionData;
        this.label = label;
        this.variable = variable;
        this.type=type;
        this.uniqueId = nextId++;
    }



    @Override
    public abstract AbstractInstruction clone();

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
    public int getUniqueId() {
        return uniqueId;
    }

    public static void resetIdCounter() {
        nextId = 1;
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
    public List<AbstractInstruction> expand(ExecutionContext context){
        return null;
    }

    @Override
    public void  setUniqueId (int id){
        this.uniqueId=id;
    }

    @Override
    public void replaceVariables(Map<String, Variable> variableMap) {
    }

    @Override
    public boolean jumpsTo(Label label) {
        return false;
    }

    @Override
    public void replaceJumpLabel(Label from, Label to) {
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }



}
