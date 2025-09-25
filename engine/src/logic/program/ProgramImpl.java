package logic.program;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import logic.Variable.Variable;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.instruction.*;
import logic.label.FixedLabel;
import logic.label.Label;
import utils.Utils;

import java.util.*;


public class ProgramImpl implements Program {
    private final String name;
    public List<Instruction> instructions;
    public Set<Variable> variables;
    public Set<Label> labels;
    private List<AbstractInstruction> expandedInstructions = new ArrayList<>();


    public ProgramImpl(String name) {
        this.name = name;
        instructions = new ArrayList<>();
        variables = new HashSet<>();
        labels = new HashSet<>();

    }

    @Override
    public List <AbstractInstruction> getExpandedInstructions(){
        return expandedInstructions;
    }
    private Map<String, Program> functionMap = new HashMap<>();

    public void setFunctionMap(Map<String, Program> functionMap) {
        this.functionMap = functionMap;
    }

    public Map<String, Program> getFunctionMap() {
        return functionMap;
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

    public int calculateMaxDegree(ExecutionContext context) {
        int maxDegree = 0;

        for (Instruction instr : instructions) {
            if (instr instanceof QuoteInstruction q) {
                q.computeDegree(context);
                maxDegree = Math.max(maxDegree, q.getDegree());
            } else if (instr instanceof AbstractInstruction ai) {
                maxDegree = Math.max(maxDegree, ai.getDegree());
            }
        }

        return maxDegree;
    }



    @Override
    public int calculateCycles() {
        if (expandedInstructions == null || expandedInstructions.isEmpty()) {
            return 0;
        }
        int cycles = 0;
        for (Instruction instruction : expandedInstructions) {
            cycles += instruction.getCycles();
        }
        return cycles;
    }


    @Override
    public Set<Variable> getVars() {
        return variables;

    }

    @Override
    public Set<Label> getLabels() {
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

    public void expandToDegree(int maxDegree, ExecutionContext context) {
        List<AbstractInstruction> result = new ArrayList<>();

        for (Instruction inst : instructions) {
            if (inst instanceof AbstractInstruction abs) {
                if (abs.getType() == InstructionType.S && abs.getDegree() <= maxDegree) {
                    List<AbstractInstruction> expandedList = abs.expand(context);
                    for (AbstractInstruction derived : expandedList) {
                        derived.setDegree(abs.getDegree() + 1);
                        derived.setOrigin(abs);
                        result.add(derived);
                    }
                } else {
                    result.add(abs);
                }

            } else {
                throw new IllegalStateException("Instruction does not extend AbstractInstruction: " + inst.getClass());
            }
        }

        this.expandedInstructions = result;
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

    public int askForDegree(ExecutionContext context) {
        int maxDegree = Utils.computeProgramDegree(this, context);

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Choose Expansion Degree");
        dialog.setHeaderText("Select a degree between 0 and " + maxDegree);

        // OK button
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Spinner for degree selection
        Spinner<Integer> degreeSpinner = new Spinner<>(0, maxDegree, 0);
        degreeSpinner.setEditable(true);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().add(degreeSpinner);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return degreeSpinner.getValue();
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();
        return result.orElse(-1);
    }


    public long executeBlackBox(ExecutionContext context) {
        List<Instruction> instrs = this.getInstructions();

        // Map של לייבלים → אינדקס ברשימת ההוראות
        Map<String, Integer> labelToIndex = new HashMap<>();
        for (int i = 0; i < instrs.size(); i++) {
            Label lbl = instrs.get(i).getLabel();
            if (lbl != null && lbl != FixedLabel.EMPTY) {
                labelToIndex.put(lbl.toString(), i);
            }
        }

        int pc = 0; // Program Counter
        while (pc < instrs.size()) {
            Instruction instr = instrs.get(pc);

            switch (instr.getName()) {
                case "ASSIGNMENT" -> {
                    AssignmentInstruction a = (AssignmentInstruction) instr;
                    long val = context.getVariableValue(a.getSource());
                    context.updateVariable(a.getDestination(), val);
                    pc++;
                }

                case "CONSTANT_ASSIGNMENT" -> {
                    ConstantAssignmentInstruction c = (ConstantAssignmentInstruction) instr;
                    context.updateVariable(c.getVariable(), c.getConstantValue());
                    pc++;
                }
                case "INCREASE" -> {
                    long val = context.getVariableValue(instr.getVariable());
                    context.updateVariable(instr.getVariable(), val + 1);
                    pc++;
                }
                case "DECREASE" -> {
                    long val = context.getVariableValue(instr.getVariable());
                    context.updateVariable(instr.getVariable(), val - 1);
                    pc++;
                }
                case "ZERO_VARIABLE" -> {
                    context.updateVariable(instr.getVariable(), 0);
                    pc++;
                }
                case "JUMP_NOT_ZERO", "JNZ" -> {
                    JumpNotZeroInstruction jnz = (JumpNotZeroInstruction) instr;
                    long val = context.getVariableValue(jnz.getVariable());
                    if (val != 0 && jnz.getJnzLabel() != FixedLabel.EMPTY) {
                        pc = labelToIndex.getOrDefault(jnz.getJnzLabel().getLabelRepresentation(), pc + 1);
                    } else {
                        pc++;
                    }
                }

                case "JUMP_ZERO" -> {
                    long val = context.getVariableValue(instr.getVariable());
                    JumpZeroInstruction jz = (JumpZeroInstruction) instr;
                    if (val == 0 && jz.getJZLabel() != FixedLabel.EMPTY) {
                        pc = labelToIndex.getOrDefault(jz.getJZLabel().getLabelRepresentation(), pc + 1);
                    } else {
                        pc++;
                    }
                }
                case "JUMP_EQUAL_CONSTANT" -> {
                    JumpEqualConstantInstruction jec = (JumpEqualConstantInstruction) instr;
                    long val = context.getVariableValue(jec.getVariable());
                    if (val == jec.getConstantValue() && jec.getJumpToLabel() != FixedLabel.EMPTY) {
                        pc = labelToIndex.getOrDefault(jec.getJumpToLabel().getLabelRepresentation(), pc + 1);
                    } else {
                        pc++;
                    }
                }
                case "JUMP_EQUAL_VARIABLE" -> {
                    JumpEqualVariableInstruction jev = (JumpEqualVariableInstruction) instr;
                    long v1 = context.getVariableValue(jev.getVariable());
                    long v2 = context.getVariableValue(jev.getVariableName());
                    if (v1 == v2 && jev.getTargetLabel() != FixedLabel.EMPTY) {
                        pc = labelToIndex.getOrDefault(jev.getTargetLabel().getLabelRepresentation(), pc + 1);
                    } else {
                        pc++;
                    }
                }
                case "GOTO_LABEL" -> {
                    GoToLabelInstruction g = (GoToLabelInstruction) instr;
                    if (g.getGoToLabel() == FixedLabel.EXIT) {
                        pc = instrs.size(); // יציאה
                    } else {
                        pc = labelToIndex.getOrDefault(g.getGoToLabel().toString(), pc + 1);
                    }
                }
                case "QUOTE" -> {
                    QuoteInstruction q = (QuoteInstruction) instr;

                    // הפעלת הפונקציה שמצוטטת
                    Program func = this.functionMap.get(q.getQuotedFunctionName());
                    if (func == null) {
                        System.out.println("⚠ Unknown function in QUOTE: " + q.getQuotedFunctionName());
                        pc++;
                        continue;
                    }

                    // קונטקסט חדש עבור הפונקציה המצוטטת
                    ExecutionContext subContext = new ExecutionContextImpl(new HashMap<>(), this.functionMap);

                    // העברת ערכים של הארגומנטים
                    List<Variable> args = q.getArguments();
                    List<Variable> funcInputs = func.getVars().stream()
                            .filter(v -> v.getType() == VariableType.INPUT)
                            .toList();

                    for (int i = 0; i < Math.min(args.size(), funcInputs.size()); i++) {
                        long argVal = context.getVariableValue(args.get(i));
                        subContext.updateVariable(funcInputs.get(i), argVal);
                    }

                    // הרצת הפונקציה כ־black box
                    long subResult = func.executeBlackBox(subContext);

                    // שמירת התוצאה במשתנה היעד של ה־QUOTE
                    context.updateVariable(q.getVariable(), subResult);

                    pc++;
                }

                default -> {
                    System.out.println("⚠ Unsupported instruction in black-box mode: " + instr.getName());
                    pc++;
                }
            }
        }

        Variable resultVar = this.getVars().stream()
                .filter(v -> v.getType() == VariableType.RESULT)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No result variable found"));
        return context.getVariableValue(resultVar);
    }

}
