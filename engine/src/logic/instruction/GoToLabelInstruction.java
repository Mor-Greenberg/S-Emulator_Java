package logic.instruction;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.program.Program;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoToLabelInstruction extends AbstractInstruction {

    private Label goToLabel;


    public GoToLabelInstruction(Variable variable, Label goToLabel) {
        this(variable, goToLabel, FixedLabel.EMPTY);
        this.degree=1;
    }

    public GoToLabelInstruction(Variable variable, Label goToLabel, Label label) {
        super(InstructionData.GOTO_LABEL, variable, label,InstructionType.S);
        this.goToLabel = goToLabel;
        this.degree=1;
    }

    public Label getGotoLabel() {
        return goToLabel;
    }
    @Override
    public InstructionType getType() {
        return type;
    }
    @Override
    public Label execute(ExecutionContext context) {
        return goToLabel;
    }

    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "GOTO " + goToLabel.toString();
        return output;
    }
    public List<AbstractInstruction> expand(ExecutionContext context) {
        // create new variable
        Variable z = context.findAvailableVariable();

       //basic instructions
        AbstractInstruction inc = new IncreaseInstruction(z);
        AbstractInstruction jnz = new JumpNotZeroInstruction(z, goToLabel);

        // keep the label
        if (getLabel() != FixedLabel.EMPTY) {
            inc.setLabel(getLabel());
            this.setLabel(FixedLabel.EMPTY);
        }


        markAsDerivedFrom(inc, this);
        markAsDerivedFrom(jnz, this);


        return Arrays.asList(inc, jnz);
    }

}
