package menuHandling;

import logic.execution.ProgramExecutorImpl;
import logic.jaxb.schema.generated.SProgram;
import logic.menu.Menu;
import logic.program.Program;
import logic.xml.XmlLoader;
import programDisplay.programDisplayImpl;

import java.util.Scanner;

public class PrintMenu {
    private Program program;

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
        switch (choice) {
            case 1:
                System.out.println("Please enter full XML path:");

                String path = sc.nextLine();
                path = path.replace("\"", "").trim(); // מסיר גרשיים מיותרים


                SProgram sProgram = XmlLoader.loadFromFile(path);
                if (sProgram != null) {
                    this.program = new XmlLoader().SprogramToProgram(sProgram);
                    System.out.println("XML loaded successfully!");
                } else {
                    System.out.println("Failed to load XML.");
                }
                break;
            case 2:
                programDisplayImpl programDisplayImpl = new programDisplayImpl(program);
                programDisplayImpl.printProgram();
                break;
            case 3:
                // expand
                System.out.println("Expanding...");
                break;
            case 4:
                ProgramExecutorImpl programExecutor = new ProgramExecutorImpl(program);

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
