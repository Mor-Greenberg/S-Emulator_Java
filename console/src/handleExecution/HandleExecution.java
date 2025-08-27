package handleExecution;

import logic.Variable.Variable;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.program.Program;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HandleExecution {
    Program program;
    Map<Variable, Long> inputs =  new HashMap<Variable, Long>();
    // ExecutionContext executionContext = new ExecutionContextImpl();
    public HandleExecution(Program program)
    {
        this.program = program;
    }

    public Program getProgram()
    {
        return program;
    }
    public void setProgram(Program program)
    {
        this.program = program;
    }
//    public void collectInputFromUser(){
//        Scanner sc = new Scanner(System.in);
//        List<Variable> inputVars = program.getVars().stream()
//                .filter(v -> v.getType() == VariableType.INPUT)
//                .toList();
//
//
//        System.out.println("The variables of this program:");
//        for (Variable variable: inputVars){
//            System.out.print(variable.toString()+ " ,");
//        }
//        System.out.println("\nPlease insert values:");
//
//        String line = sc.nextLine();
//        String[] values = line.split(",");
//        for (int i = 0; i < inputVars.size(); i++) {
//            int value = 0;
//            if (i < values.length) {
//                try {
//                    value = Integer.parseInt(values[i].trim());
//                } catch (NumberFormatException e) {
//                    System.out.println("Invalid input at position " + (i + 1) + ", using 0.");
//                }
//            }
//            executionContext.updateVariable(inputVars.get(i), value);
//        }





   // }

    public void collectInputFromUser(Program program, ExecutionContext context) {
        if (program == null) {
            System.out.println("No program loaded yet.");
            return;
        }

        List<Variable> inputVars = program.getVars().stream()
                .filter(v -> v.getType() == VariableType.INPUT)
                .toList();

        if (inputVars.isEmpty()) {
            System.out.println("No input variables found.");
            return;
        }
        Scanner sc = new Scanner(System.in);

        System.out.println("The input variables of this program:");
        for (int i = 0; i < inputVars.size(); i++) {
            System.out.print(inputVars.get(i));
            if (i < inputVars.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println();

        System.out.println("\nPlease insert values (comma-separated):");
        String line = sc.nextLine();
        String[] values = line.split(",");

        for (int i = 0; i < inputVars.size(); i++) {
            long value = 0;
            if (i < values.length) {
                try {
                    value = Integer.parseInt(values[i].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input at position " + (i + 1) + ", using 0.");
                }
            }
            context.updateVariable(inputVars.get(i), value);
            inputs.put(inputVars.get(i), value);
        }

        System.out.println("Input values updated successfully.");
    }

    public Map<Variable,Long> getInputsMap(){
        return inputs;
    }


}
