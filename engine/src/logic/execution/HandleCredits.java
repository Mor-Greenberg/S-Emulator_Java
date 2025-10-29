package logic.execution;

import logic.architecture.ArchitectureData;
import logic.program.Program;
import session.UserSession;

import static ui.guiUtils.UiUtils.showError;


public class HandleCredits {


    public static int prepareExecution(Program program, ArchitectureData architecture, UserSession userSession) {
        int architectureCost = architecture.getCreditsCost();
        int current = userSession.getUserCredits();

        if (current < architectureCost) {
            showError("Not enough credits! Required: " + architectureCost + ", Available: " + current);
            return -1;
        }

        userSession.deductCredits(architectureCost);


        return architectureCost;
    }


    public static boolean consumeCycles(String programName, int cycles,UserSession userSession) {
        int current = userSession.getUserCredits();
        if (current < cycles) {
           showError("Out of credits while running " + programName);
            return false;
        }

        userSession.deductCredits(cycles);

        return true;
    }


}
