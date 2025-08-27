package menuHandling;

import handleExecution.HandleExecution;
import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.execution.ProgramExecutorImpl;
import logic.instruction.Instruction;
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
    public boolean handleChoice(int choice, Program program) {
        Scanner sc = new Scanner(System.in);
        programDisplayImpl programDisplayImpl = new programDisplayImpl(program);

        switch (choice) {
            case 1:
                System.out.println("Please enter full XML path:");

                String path = sc.nextLine();
                path = path.replace("\"", "").trim(); // מסיר גרשיים מיותרים


                SProgram sProgram = XmlLoader.loadFromFile(path);
                if (sProgram != null) {
                    this.program = new XmlLoader().SprogramToProgram(sProgram);
                    System.out.println("XML loaded successfully!");
                    xmlLoded=true;
                } else {
                    System.out.println("Failed to load XML.");
                }
                break;
            case 2:
                programDisplayImpl.printProgram();
                break;
            case 3:
                // expand
                System.out.println("Expanding...");
                break;
            case 4:
                if (program == null) {
                    System.out.println("No program loaded yet.");
                    break;
                }

                Map<Variable, Long> variableState = new HashMap<>();

                ExecutionContext context = new ExecutionContextImpl(variableState);

                HandleExecution handleExecution = new HandleExecution(program);
                handleExecution.collectInputFromUser(program, context);

                ProgramExecutorImpl executor = new ProgramExecutorImpl(program);
                long result = executor.run();
                System.out.println("Instructions activated:");
                programDisplayImpl.printInstructions(executor.getInstructionsActivated());

                System.out.println("Program result(y): " + result);

                System.out.println("Variable values:");
                for (Map.Entry<Variable, Long> entry : variableState.entrySet()) {
                    System.out.println(entry.getKey() + " = " + entry.getValue());
                }

                break;

            case 5:
                System.out.println("Showing stats...");
                break;
            case 6:
                System.out.println("Exiting...");
                return false;
            default:
                System.out.println("Invalid choice.");
        }
        return true;
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
