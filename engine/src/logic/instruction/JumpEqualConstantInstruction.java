package logic.instruction;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.label.LabelImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JumpEqualConstantInstruction extends AbstractInstruction {

    public InstructionType type = InstructionType.S;

    private Label JEConstantLabel;
    public long constantValue ;

    public JumpEqualConstantInstruction(Variable variable, Label JEConstantLabel, long constantValue) {
        this(variable, JEConstantLabel, FixedLabel.EMPTY, constantValue);
        this.degree = 3;

    }

    public JumpEqualConstantInstruction(Variable variable, Label JEConstantLabel, Label label, long constantValue) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, variable, label);
        this.JEConstantLabel = JEConstantLabel;
        this.constantValue = constantValue;
        this.degree = 3;
    }
    @Override
    public Label execute(ExecutionContext context) {
        if(context.getVariableValue(getVariable()) == constantValue){
            return JEConstantLabel;
        }

        return FixedLabel.EMPTY;
    }
    @Override
    public String commandDisplay(){
        Variable variable = getVariable();
        String output = "IF " + variable.toString() + " = " + constantValue+ "GOTO"+JEConstantLabel.toString();
        return output;
    }


    public List<AbstractInstruction> expand(ExecutionContext context) {
        List<AbstractInstruction> result = new ArrayList<>();

        Variable originalVar = getVariable();          // The variable to compare
        Label jumpTo = this.JEConstantLabel;           // Label to jump to if condition holds
        long constant = this.constantValue;            // The constant value to compare against

        // Temporary variable z1 ← originalVar
        Variable z1 = context.findAvailableVariable();
        result.add(new AssignmentInstruction(z1, originalVar));

        // Internal exit label: jump here if the condition is not met
        Label exitLabel = context.findAvailableLabel();

        // Repeat K times: if z1 == 0 → jump to exitLabel, else decrease z1 by 1
        for (int i = 0; i < constant; i++) {
            result.add(new JumpZeroInstruction(z1, exitLabel));   // If we reach 0 too early → V ≠ K
            result.add(new DecreaseInstruction(z1));              // Decrease z1
        }

        // After K decrements, if z1 ≠ 0 → V ≠ K → jump to exit
        result.add(new JumpNotZeroInstruction(z1, exitLabel));

        // If we passed all checks → V == K → jump to target label
        result.add(new GoToLabelInstruction(z1, jumpTo));

        // Exit point: if the condition was not met
        AbstractInstruction noOp = new NoOpInstruction(z1);
        noOp.setLabel(exitLabel);
        result.add(noOp);

        return result;
    }

    public static void main(String[] args) {
        Map<Variable, Long> variableMap = new HashMap<>();
        ExecutionContext context = new ExecutionContextImpl(variableMap);

        // Create variable V (x1)
        Variable v = new VariableImpl(VariableType.INPUT, 1);

        // Set value to V
        context.updateVariable(v, 3); // Try 3 for match, or another number for mismatch

        // Define constant to compare against
        long constant = 2;

        // Define target label
        Label targetLabel = new LabelImpl(99);

        // Create instruction
        JumpEqualConstantInstruction instr =
                new JumpEqualConstantInstruction(v, targetLabel, constant);

        // Run execute() test
        Label resultLabel = instr.execute(context);
        System.out.println("Result from execute(): " +
                (resultLabel == targetLabel ? "JUMPED to " + resultLabel : "NO JUMP"));

        // Run expand() and print the expanded instructions
        List<AbstractInstruction> expanded = instr.expand(context);
        System.out.println("\nExpanded instructions:");
        for (AbstractInstruction ins : expanded) {
            String labelStr = ins.getLabel() != FixedLabel.EMPTY ? " [Label: " + ins.getLabel() + "]" : "";
            System.out.println(ins.commandDisplay() + labelStr);
        }
    }


}





