package menuHandling;

import gui.ExecutionRunner;
import handleExecution.HandleExecution;
import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.execution.ProgramExecutorImpl;
import logic.instruction.AbstractInstruction;
import logic.xml.XmlValidation;
import printExpand.expansion.PrintExpansion;
import logic.history.RunHistoryEntry;
import logic.instruction.Instruction;
import logic.jaxb.schema.generated.SProgram;
import logic.menu.Menu;
import logic.program.Program;
import logic.xml.XmlLoader;
import programDisplay.ProgramDisplayImpl;

import java.util.*;
import java.util.stream.Collectors;

public class PrintMenu {
    private Program program;
    boolean xmlLoded = false;
    private final List<RunHistoryEntry> history = new ArrayList<>();
    private int runCounter = 1;



    public void printMenu() {
        Menu menu = new Menu();
        int i = 1;
        System.out.println("*Menu*");
        for (String command: menu.menuCommands){
            System.out.println(i+". "+command);

            i++;
        }
        System.out.print("Please choose one: ");
    }

    public int usersChoice() {
        Scanner sc = new Scanner(System.in);
        while (!sc.hasNextInt()) {
            System.out.println("Error: please enter a number");
            sc.next();
        }
        return sc.nextInt();
    }


//    public boolean handleChoice(int choice, Program program) {
//        Scanner sc = new Scanner(System.in);
//        //ProgramDisplayImpl programDisplay = new ProgramDisplayImpl(program);
//
//        switch (choice) {
//            case 1 -> loadXml(sc);
//            case 2 -> programDisplay.printProgram(xmlLoded);
//            case 3 -> expandProgram(program,programDisplay);
//            case 4 -> runProgram(program, programDisplay);
//            case 5 -> showStats();
//            case 6 -> {
//                System.out.println("Exiting...");
//                return false;
//            }
//            default -> System.out.println("Invalid choice.");
//        }
//        return true;
//    }
    private void loadXml(Scanner sc) {
        System.out.println("Please enter full XML path:");
        String path = sc.nextLine().replace("\"", "").trim();

        SProgram sProgram = XmlLoader.loadFromFile(path);

        if (sProgram != null) {
            this.history.clear();
            this.runCounter = 1;
            AbstractInstruction.resetIdCounter();
            this.program = new XmlLoader().SprogramToProgram(sProgram);
            boolean validXML = XmlValidation.validateLabels(program.getInstructions());
            if(!validXML){
                System.out.println("Invalid label reference");
                return;
            }

            System.out.println("XML loaded successfully!");
            xmlLoded = true;
        } else {
            System.out.println("Failed to load XML.");
        }
    }

    private void expandProgram(Program program, ProgramDisplayImpl display) {
        if (!xmlLoded) {
            System.out.println("XML is not loaded, returning");
            return;
        }

        int maxDegree = program.calculateMaxDegree();
        int chosenDegree = program.askForDegree();

        if (chosenDegree == 0) {
            display.printProgram(xmlLoded);
            return;
        }

        Map<Variable, Long> variableState = program.getVars().stream()
                .collect(Collectors.toMap(v -> v, v -> 0L));

        ExecutionContext context = new ExecutionContextImpl(variableState);

        PrintExpansion pE=new PrintExpansion(program);

        program.expandToDegree(chosenDegree, context);
        pE.printProgramWithOrigins(program);
    }






    private void showStats() {
        if (!xmlLoded) {
            System.out.println("XML is not loaded, returning");
            return;
        }
        System.out.println("--- Run History ---");

        for (RunHistoryEntry entry : history) {
            System.out.println("Run #" + entry.getRunNumber());
            System.out.println("Expansion degree: " + entry.getExpansionDegree());
            System.out.println("Inputs:");
            for (Map.Entry<Variable, Long> input : entry.getInputs().entrySet()) {
                System.out.println(input.getKey().getRepresentation() + " = " + input.getValue());
            }
            System.out.println("Result (y): " + entry.getResultY());
            System.out.println("Total Cycles: " + entry.getTotalCycles());

        }

    }

//    private void runProgram(Program program, ProgramDisplayImpl programDisplay) {
//       if (!xmlLoded) {
//           System.out.println("XML is not loaded, returning");
//           return;
//       }
//
//        int degree = program.askForDegree();
//
//        Map<Variable, Long> variableState = new HashMap<>();
//        ExecutionContext context = new ExecutionContextImpl(variableState);
//        program.expandToDegree(degree, context);
//        Program expandedProgram = program;
//
//
//        HandleExecution handleExecution = new HandleExecution(expandedProgram);
//        handleExecution.collectInputFromUser(expandedProgram, context);
//
//        ProgramExecutorImpl executor = new ProgramExecutorImpl(expandedProgram);
//        long result = executor.run(context);
//
//
//        System.out.println("Instructions activated:");
//        programDisplay.printInstructions(executor.getInstructionsActivated());
//
//        if(degree!=0){
//            System.out.println("Instructions expanded:");
//            PrintExpansion expansion = new PrintExpansion(expandedProgram);
//            AbstractInstruction.resetIdCounter();
//
//            expansion.printProgramWithOrigins(expandedProgram);
//        }
//
//        System.out.println("\nProgram result (y): " + result);
//
//        System.out.println("Variable values:");
//
//        variableState.entrySet().stream()
//                .sorted((e1, e2) -> {
//                    String v1 = e1.getKey().getRepresentation();
//                    String v2 = e2.getKey().getRepresentation();
//
//                    if (v1.equals("y")) return -1;
//                    if (v2.equals("y")) return 1;
//
//
//                    if (v1.startsWith("x") && v2.startsWith("z")) return -1;
//                    if (v1.startsWith("z") && v2.startsWith("x")) return 1;
//
//                    int num1 = Integer.parseInt(v1.substring(1));
//                    int num2 = Integer.parseInt(v2.substring(1));
//                    return Integer.compare(num1, num2);
//                })
//                .forEach(entry -> System.out.println(entry.getKey().getRepresentation() + " = " + entry.getValue()));
//
//        int sumCycles = executor.getInstructionsActivated().stream()
//                .mapToInt(Instruction::getCycles)
//                .sum();
//        System.out.println("Number of cycles: " + sumCycles);
//
//        RunHistoryEntry entry = new RunHistoryEntry(runCounter++, degree,
//                handleExecution.getInputsMap(), result, sumCycles);
//        history.add(entry);
//    }

        private void runProgram(Program program, ProgramDisplayImpl programDisplay) {
            ExecutionRunner.runProgram(program, programDisplay);
        }



//    public void handleMenu() {
//        boolean continueRunning = true;
//        while (continueRunning) {
//            printMenu();
//            int choice = usersChoice();
//            continueRunning = handleChoice(choice, program);
//        }
//    }

}
