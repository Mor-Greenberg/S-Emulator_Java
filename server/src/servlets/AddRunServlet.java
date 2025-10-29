package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dto.UserRunEntryDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.program.Program;
import serverProgram.GlobalProgramStore;
import user.User;
import user.UserManager;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@WebServlet("/api/add-run")
public class AddRunServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject json = gson.fromJson(req.getReader(), JsonObject.class);

        if (json == null || !json.has("username") || !json.has("run")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing fields in JSON");
            return;
        }

        String username = json.get("username").getAsString();
        UserRunEntryDTO dto = gson.fromJson(json.get("run"), UserRunEntryDTO.class);

        UserManager userManager = UserManager.getInstance();
        userManager.addRun(username, dto);
        User user = userManager.getUser(username);
        if (user != null) {
            int usedCredits = dto.getCycles() + dto.getArchitecture().getCreditsCost();
            user.recordRun(usedCredits);
        }


        // ✅ חישוב קרדיטים לשם הסטטיסטיקה של התוכנית בלבד
        int usedCredits = dto.getCycles() + dto.getArchitecture().getCreditsCost();

        // ✅ עדכון ה־Program בלבד (לא המשתמש)
        Program program = GlobalProgramStore.getProgramCache().get(dto.getProgramName());
        if (program != null) {
            program.recordRun(usedCredits);
            System.out.println("Updated " + dto.getProgramName() + " | Runs=" + program.getRunCount() + " | Avg=" + program.getAverageCredits());
        }


        System.out.println("Added run for user: " + username +
                " | program=" + dto.getProgramName() +
                " | usedCredits=" + usedCredits);

        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
