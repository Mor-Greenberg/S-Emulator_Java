package menuHandling;

import handleExecution.HandleExecution;
import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.execution.ProgramExecutorImpl;
import logic.history.RunHistoryEntry;
import logic.instruction.Instruction;
import logic.jaxb.schema.generated.SProgram;
import logic.menu.Menu;
import logic.program.Program;
import logic.xml.XmlLoader;
import programDisplay.programDisplayImpl;

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


    public boolean handleChoice(int choice, Program program) {
        Scanner sc = new Scanner(System.in);
        programDisplayImpl programDisplay = new programDisplayImpl(program);

        switch (choice) {
            case 1 -> loadXml(sc);
            case 2 -> programDisplay.printProgram();
            case 3 -> expandProgram(program,programDisplay); // כרגע רק פלט
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
    private void expandProgram(Program program, programDisplayImpl display) {
        if (program == null) {
            System.out.println("No valid program loaded.");
            return;
        }

        int maxDegree = program.calculateMaxDegree();
        int chosenDegree = askForDegree(program);

        if (chosenDegree == 0) {
            display.printProgram();
            return;
        }

        Map<Variable, Long> variableState = program.getVars().stream()
                .collect(Collectors.toMap(v -> v, v -> 0L));

        ExecutionContext context = new ExecutionContextImpl(variableState);


        program.expandToDegree(chosenDegree, context);
        display.printProgramWithOrigins(program);
    }


    private int askForDegree(Program program) {
        int maxDegree = program.calculateMaxDegree();
        System.out.println("Max degree:" + maxDegree);
        System.out.println("Choose degree (0 to" + maxDegree + "):");


        Scanner scanner = new Scanner(System.in);

        int degree;
        while (true) {
            try {
                degree = Integer.parseInt(scanner.nextLine());
                if (degree < 0 || degree > maxDegree) {
                    System.out.print("Invalid degree.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.print("Insert valid degree. ");
            }

        }
        return degree;
    }


    private void showStats() {
        System.out.println("--- Run History ---");

        for (RunHistoryEntry entry : history) {
            System.out.println("Run #" + entry.getRunNumber());
            System.out.println("Expansion degree: " + entry.getExpansionDegree());
            System.out.println("Inputs:");
            for (Map.Entry<Variable, Long> input : entry.getInputs().entrySet()) {
                System.out.println("  " + input.getKey().getRepresentation() + " = " + input.getValue());
            }
            System.out.println("Result (y): " + entry.getResultY());
            System.out.println("Total Cycles: " + entry.getTotalCycles());

        }

    }

    private void runProgram(Program program, programDisplayImpl programDisplay) {
        if (program == null) {
            System.out.println("No program loaded yet.");
            return;
        }
            int degree=askForDegree(program);

            // ניצור את מפת המשתנים ונעביר אותה להקשר
            Map<Variable, Long> variableState = new HashMap<>();
            ExecutionContext context = new ExecutionContextImpl(variableState);

            HandleExecution handleExecution = new HandleExecution(program);
            handleExecution.collectInputFromUser(program, context);

            // נריץ את התוכנית
            ProgramExecutorImpl executor = new ProgramExecutorImpl(program);
            long result = executor.run(context); // נעביר את ההקשר לפונקציה run

            // הצגת תוצאות
            System.out.println("Instructions activated:");
            programDisplay.printInstructions(executor.getInstructionsActivated());

            System.out.println("Instructions expanded:");
            for (Instruction instr : executor.getInstructionsActivated()) {
                if (instr.hasOrigin()) {
                    System.out.print("  <<<  " + instr.getOrigin().commandDisplay());
                }
            }


            System.out.println("Program result (y): " + result);

            System.out.println("Variable values:");
            variableState.forEach((var, val) -> System.out.println(var + " = " + val));

            System.out.println("Number of cycles:");
            int sumCycles = 0;
            for (Instruction instruction : executor.getInstructionsActivated()) {
                int cycles = instruction.getCycles(); // ← מפעיל את getter מה־Abstract

                System.out.println("Instruction: " + instruction.getName() + ", Cycles: " + cycles);
                sumCycles += cycles;
            }
            System.out.println("Number of cycles: " + sumCycles);
            List<Instruction> instructions = executor.getInstructionsActivated();
            int totalCycles = instructions.stream()
                    .mapToInt(Instruction::getCycles)
                    .sum();


            RunHistoryEntry entry = new RunHistoryEntry(runCounter++,degree ,handleExecution.getInputsMap(), result, totalCycles);
            history.add(entry);


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
