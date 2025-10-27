package logic.execution;

import logic.architecture.ArchitectureData;
import logic.program.Program;
import session.UserSession;
import utils.UiUtils;

import static session.UserSession.updateCreditsLabel;

public class HandleCredits {

    public static int prepareExecution(Program program, ArchitectureData architecture) {
        int architectureCost = architecture.getCreditsCost();
        int current = UserSession.getUserCredits();

        if (current < architectureCost) {
            UiUtils.showError("Not enough credits! Required: " + architectureCost + ", Available: " + current);
            return -1;
        }

        return architectureCost;
    }



    public static boolean consumeCycles(String programName, int cycles) {
        int current = UserSession.getUserCredits();
        if (current < cycles) {
            UiUtils.showError("Out of credits while running " + programName);
            return false;
        }

        return true;
    }
    public static void finalizeExecution(String programName, int programCost, int architectureCost) {
        int total = programCost + architectureCost;
        UserSession.deductCredits(total);
        updateCreditsLabel();
        System.out.println("Total deducted: " + total);
    }



}
