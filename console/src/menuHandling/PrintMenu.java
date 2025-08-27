package menuHandling;

import handleExecution.HandleExecution;
import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.execution.ProgramExecutorImpl;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.jaxb.schema.generated.SProgram;
import logic.menu.Menu;
import logic.program.Program;
import logic.xml.XmlLoader;
import programDisplay.programDisplayImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PrintMenu {
    private Program program;
    boolean xmlLoded = false;

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
//        programDisplayImpl programDisplayImpl = new programDisplayImpl(program);
//
//        switch (choice) {
//            case 1:
//                System.out.println("Please enter full XML path:");
//
//                String path = sc.nextLine();
//                path = path.replace("\"", "").trim(); // מסיר גרשיים מיותרים
//
//
//                SProgram sProgram = XmlLoader.loadFromFile(path);
//                if (sProgram != null) {
//                    this.program = new XmlLoader().SprogramToProgram(sProgram);
//                    System.out.println("XML loaded successfully!");
//                    xmlLoded=true;
//                } else {
//                    System.out.println("Failed to load XML.");
//                }
//                break;
//            case 2:
//                programDisplayImpl.printProgram();
//                break;
//            case 3:
//                // expand
//                System.out.println("Expanding...");
//                break;
//            case 4:
//                if (program == null) {
//                    System.out.println("No program loaded yet.");
//                    break;
//                }
//
//                Map<Variable, Long> variableState = new HashMap<>();
//
//                ExecutionContext context = new ExecutionContextImpl(variableState);
//
//                HandleExecution handleExecution = new HandleExecution(program);
//                handleExecution.collectInputFromUser(program, context);
//
//                ProgramExecutorImpl executor = new ProgramExecutorImpl(program);
//                long result = executor.run();
//                System.out.println("Instructions activated:");
//                programDisplayImpl.printInstructions(executor.getInstructionsActivated());
//
//                System.out.println("Program result(y): " + result);
//
//                System.out.println("Variable values:");
//                for (Map.Entry<Variable, Long> entry : variableState.entrySet()) {
//                    System.out.println(entry.getKey() + " = " + entry.getValue());
//                }
//
//                break;
//
//            case 5:
//                System.out.println("Showing stats...");
//                break;
//            case 6:
//                System.out.println("Exiting...");
//                return false;
//            default:
//                System.out.println("Invalid choice.");
//        }
//        return true;
//    }

    public boolean handleChoice(int choice, Program program) {
        Scanner sc = new Scanner(System.in);
        programDisplayImpl programDisplay = new programDisplayImpl(program);

        switch (choice) {
            case 1 -> loadXml(sc);
            case 2 -> programDisplay.printProgram();
            case 3 -> expandProgram(); // כרגע רק פלט
            case 4 -> runProgram(program, programDisplay);
            case 5 -> showStats(); // כרגע רק פלט
            case 6 -> {
                System.out.println("Exiting...");
                return false;
            }
            default -> System.out.println("Invalid choice.");
        }
        return true;
    }
    private void loadXml(Scanner sc) {
        System.out.println("Please enter full XML path:");
        String path = sc.nextLine().replace("\"", "").trim();

        SProgram sProgram = XmlLoader.loadFromFile(path);
        if (sProgram != null) {
            this.program = new XmlLoader().SprogramToProgram(sProgram);
            System.out.println("XML loaded successfully!");
            xmlLoded = true;
        } else {
            System.out.println("Failed to load XML.");
        }
    }
    private void expandProgram() {
        System.out.println("Expanding...");
    }
    private void showStats() {
        System.out.println("Showing stats...");
    }

    private void runProgram(Program program, programDisplayImpl programDisplay) {
        if (program == null) {
            System.out.println("No program loaded yet.");
            return;
        }

        // ניצור את מפת המשתנים ונעביר אותה להקשר
        Map<Variable, Long> variableState = new HashMap<>();
        ExecutionContext context = new ExecutionContextImpl(variableState);

        // נאסוף קלט מהמשתמש
        HandleExecution handleExecution = new HandleExecution(program);
        handleExecution.collectInputFromUser(program, context);

        // נריץ את התוכנית
        ProgramExecutorImpl executor = new ProgramExecutorImpl(program);
        long result = executor.run(context); // נעביר את ההקשר לפונקציה run

        // הצגת תוצאות
        System.out.println("Instructions activated:");
        programDisplay.printInstructions(executor.getInstructionsActivated());

        System.out.println("Program result (y): " + result);

        System.out.println("Variable values:");
        variableState.forEach((var, val) -> System.out.println(var + " = " + val));

        System.out.println("Number of cycles:");
        int sumCycles = 0;
        for (Instruction instruction : executor.getInstructionsActivated()) {
            int cycles = instruction.cycles(); // ← מפעיל את getter מה־Abstract

            System.out.println("Instruction: " + instruction.getName() + ", Cycles: " + cycles);
            sumCycles += cycles;
        }
        System.out.println("Number of cycles: " + sumCycles);



    }


    public void handleMenu() {
        boolean continueRunning = true;
        while (continueRunning) {
            printMenu();
            int choice = usersChoice();
            continueRunning = handleChoice(choice, program);
        }
    }

}
