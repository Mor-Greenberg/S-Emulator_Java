package logic.instruction;

import logic.Variable.QuoteVariable;
import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;
import utils.QuoteProcessor;

import java.util.*;
import java.util.stream.Collectors;

public class QuoteInstruction extends AbstractInstruction {
    private final String functionName;       // Q
    private final List<Variable> arguments;  // V1, V2, ...
    private final Variable destination;// V
    public Program quotedProgram;

    public QuoteInstruction(String functionName, List<Variable> arguments, Variable destination) {
        super(InstructionData.QUOTE, destination, InstructionType.S);
        this.functionName = functionName;
        this.arguments = arguments;
        this.destination = destination;
        this.degree = -1;
    }
    public QuoteInstruction(Label label, String functionName, List<Variable> arguments, Variable destination) {
        super(InstructionData.QUOTE, destination, label, InstructionType.S);
        this.functionName = functionName;
        this.arguments = arguments;
        this.destination = destination;
        this.degree = -1;
    }
    public QuoteInstruction(Label label, String functionName, List<Variable> arguments, Variable destination, ExecutionContext context) {
        super(InstructionData.QUOTE, destination, label, InstructionType.S);
        this.functionName = functionName;
        this.arguments = arguments;
        this.destination = destination;

        this.quotedProgram = context.getProgramMap(functionName);


        int maxDegreeInQ = this.quotedProgram.getInstructions().stream()
                .mapToInt(instr -> ((AbstractInstruction) instr).getDegree())
                .max()
                .orElse(0);

        this.degree = maxDegreeInQ + 1;
    }


    public Variable getDestination() {
        return destination;
    }


    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;
    }

    @Override
    public String commandDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append(destination).append(" ← (").append(functionName);
        for (Variable v : arguments) {
            sb.append(",");
            if (v instanceof QuoteVariable qv) {
                sb.append(qv.getQuote().getQuotedProgramName());
                if (!qv.getQuote().getArguments().isEmpty()) {
                    sb.append("(");
                    sb.append(
                            qv.getQuote().getArguments().stream()
                                    .map(Variable::getRepresentation)
                                    .collect(Collectors.joining(","))
                    );
                    sb.append(")");
                }
            } else {
                sb.append(v.getRepresentation());
            }
        }
        sb.append(")");
        return sb.toString();
    }


    @Override
    public List<AbstractInstruction> expand(ExecutionContext context) {
        if (this.getDegree() <= 0) {
            return Collections.singletonList(this);
        }

        List<AbstractInstruction> result = new ArrayList<>();

        Program quoted = context.getProgramMap(functionName);
        if (quoted == null) {
            throw new RuntimeException("Function not found: " + functionName);
        }

        Map<String, Variable> varMap = new HashMap<>();

        for (int i = 0; i < arguments.size(); i++) {
            String formal = "x" + (i + 1);
            Variable zi = context.findAvailableVariable();
            varMap.put(formal, zi);

            Variable actual = arguments.get(i);

            if (actual instanceof QuoteVariable qv) {
                List<AbstractInstruction> innerExpansion = qv.getQuoteInstruction().expand(context);
                result.addAll(innerExpansion);
                result.add(new AssignmentInstruction(zi, qv.getQuoteInstruction().getDestination()));
            } else {
                result.add(new AssignmentInstruction(zi, actual));
            }
        }

        Variable zy = context.findAvailableVariable();
        varMap.put("y", zy);

        Label lend = context.findAvailableLabel();

        List<AbstractInstruction> expandedQ =
                QuoteProcessor.rewriteInstructions(quoted, varMap, lend, context);

        if (getLabel() != FixedLabel.EMPTY) {
            if (!expandedQ.isEmpty() && !expandedQ.get(0).getLabel().equals(FixedLabel.EMPTY)) {
                AbstractInstruction neutral = new NoOpInstruction(destination);
                neutral.setLabel(getLabel());
                result.add(neutral);
            } else if (!expandedQ.isEmpty()) {
                expandedQ.get(0).setLabel(getLabel());
            }
        }

        result.addAll(expandedQ);

        AssignmentInstruction assignBack = new AssignmentInstruction(lend, destination, zy);
        result.add(assignBack);

        for (AbstractInstruction instr : result) {
            markAsDerivedFrom(instr, this);
        }
        return result;
    }

    @Override
    public AbstractInstruction clone() {
        List<Variable> copiedArgs = new ArrayList<>(this.arguments);
        return new QuoteInstruction(this.functionName, copiedArgs, this.destination);
    }

    @Override
    public void replaceVariables(Map<String, Variable> variableMap) {
        String destName = destination.getRepresentation();
        if (variableMap.containsKey(destName)) {
            this.setVariable(variableMap.get(destName));
        }

        for (int i = 0; i < arguments.size(); i++) {
            Variable arg = arguments.get(i);
            String argName = arg.getRepresentation();
            if (variableMap.containsKey(argName)) {
                arguments.set(i, variableMap.get(argName));
            }
        }
    }


    public String getQuotedProgramName() {
        return functionName;
    }
    public List<Variable> getArguments() {
        return arguments;
    }

    public void computeDegree(ExecutionContext context) {
        Program quoted = context.getProgramMap(functionName);
        if (quoted == null) {
            throw new RuntimeException("Function not found: " + functionName);
        }

        int programDepth = 0;
        for (Instruction instr : quoted.getInstructions()) {
            if (instr instanceof QuoteInstruction qi) {
                qi.computeDegree(context);                 // רקורסיבי פנימה
                programDepth = Math.max(programDepth, qi.getDegree());
            } else if (instr instanceof AbstractInstruction ai) {
                programDepth = Math.max(programDepth, ai.getDegree());
            }
        }

        int argsDepth = 0;
        for (Variable arg : arguments) {
            if (arg instanceof logic.Variable.QuoteVariable qv) {
                QuoteInstruction inner = qv.getQuote();
                inner.computeDegree(context);
                argsDepth = Math.max(argsDepth, inner.getDegree());
            }
        }

        this.degree = 1 + Math.max(programDepth, argsDepth);

    }


    public String getQuotedFunctionName() {
        return functionName;
    }
}
