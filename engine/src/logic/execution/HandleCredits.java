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

        UserSession.deductCredits(architectureCost);
       updateCreditsLabel();
        System.out.println("💰 Deducted architecture cost: " + architectureCost);
        return architectureCost; // ✅ נחזיר את עלות הארכיטקטורה
    }



    public static boolean consumeCycles(String programName, int cycles) {
        int current = UserSession.getUserCredits();
        if (current < cycles) {
            UiUtils.showError("⚠️ Out of credits while running " + programName);
            return false;
        }

        UserSession.deductCredits(cycles);
        updateCreditsLabel();
        return true;
    }


}
