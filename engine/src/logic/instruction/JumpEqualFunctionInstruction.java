package logic.instruction;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static logic.blaxBox.BlackBox.executeBlackBox;

public class JumpEqualFunctionInstruction extends AbstractInstruction {
    private Label targetLabel;         // JEFunctionLabel
    private final String functionName; // Q
    private final List<Variable> arguments;

    public JumpEqualFunctionInstruction(Variable v,
                                        Label targetLabel,
                                        String functionName,
                                        List<Variable> arguments,
                                        Label label) {
        super(InstructionData.JUMP_EQUAL_FUNCTION, v, label, InstructionType.S);
        this.targetLabel = targetLabel;
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public JumpEqualFunctionInstruction(Variable v,
                                        Label targetLabel,
                                        String functionName,
                                        List<Variable> arguments) {
        this(v, targetLabel, functionName, arguments, FixedLabel.EMPTY);
    }

    public Label getTargetLabel() {
        return targetLabel;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Variable> getArguments() {
        return arguments;
    }
    @Override
    public Label execute(ExecutionContext context) {
        Program func = context.getProgramMap(functionName);
        if (func == null) {
            return FixedLabel.EMPTY;
        }

        ExecutionContext subContext =
                new ExecutionContextImpl(new HashMap<>(),
                        context instanceof ExecutionContextImpl ec ? ec.programMap : new HashMap<>());

        List<Variable> funcInputs = func.getVars().stream()
                .filter(v -> v.getType() == logic.Variable.VariableType.INPUT)
                .toList();

        for (int i = 0; i < Math.min(arguments.size(), funcInputs.size()); i++) {
            long argVal = context.getVariableValue(arguments.get(i));
            subContext.updateVariable(funcInputs.get(i), argVal);
        }

        long qValue = executeBlackBox(subContext, context.getProgramMap(functionName));
        long vValue = context.getVariableValue(getVariable());

        return (vValue == qValue) ? targetLabel : FixedLabel.EMPTY;
    }

    @Override
    public List<AbstractInstruction> expand(ExecutionContext context) {
        return buildExpansion(context, targetLabel);
    }

    private List<AbstractInstruction> buildExpansion(ExecutionContext context, Label jumpTarget) {
        List<AbstractInstruction> result = new ArrayList<>();

        Variable v = getVariable();
        Variable z1 = context.findAvailableVariable();

        // 1. QUOTE Q(...) → z1
        QuoteInstruction quoteInstr = new QuoteInstruction(functionName, arguments, z1);
        result.add(quoteInstr);

        // 2. JUMP_EQUAL_VARIABLE (v, z1, targetLabel)
        JumpEqualVariableInstruction cmp = new JumpEqualVariableInstruction(v, z1, jumpTarget);
        result.add(cmp);

        for (AbstractInstruction instr : result) {
            markAsDerivedFrom(instr, this);
        }

        return result;
    }

    @Override
    public String commandDisplay() {
        return "IF " + getVariable().toString() +
                " = (" + functionName + "," +
                arguments.stream().map(Variable::getRepresentation).reduce((a,b)->a+","+b).orElse("") +
                ") GOTO " + targetLabel.toString();
    }


    @Override
    public AbstractInstruction clone() {
        return new JumpEqualFunctionInstruction(this.getVariable(),
                this.targetLabel,
                this.functionName,
                new ArrayList<>(this.arguments),
                this.getLabel());
    }

    @Override
    public void replaceVariables(Map<String, Variable> variableMap) {
        String vName = getVariable().getRepresentation();
        if (variableMap.containsKey(vName)) {
            this.setVariable(variableMap.get(vName));
        }
        for (int i = 0; i < arguments.size(); i++) {
            String argName = arguments.get(i).getRepresentation();
            if (variableMap.containsKey(argName)) {
                arguments.set(i, variableMap.get(argName));
            }
        }
    }

    @Override
    public void replaceJumpLabel(Label from, Label to) {
        if (this.targetLabel.equals(from)) {
            this.targetLabel = to;
        }
    }

    @Override
    public boolean jumpsTo(Label label) {
        return this.targetLabel.equals(label);
    }
}
