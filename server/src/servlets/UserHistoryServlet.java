package servlets;

import com.google.gson.Gson;
import dto.UserRunEntryDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.program.Program;
import serverProgram.GlobalProgramStore;
import user.User;
import user.UserManager;

import java.io.IOException;

@WebServlet("/api/user-history")
public class UserHistoryServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        if (username == null || username.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        UserManager userManager = (UserManager) getServletContext().getAttribute("userManager");
        if (userManager == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        var runs = userManager.getUserHistory(username);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(runs));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserRunEntryDTO dto = gson.fromJson(req.getReader(), UserRunEntryDTO.class);

        if (dto == null || dto.getUsername() == null || dto.getUsername().isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing username in DTO");
            return;
        }

        UserManager userManager = (UserManager) getServletContext().getAttribute("userManager");
        if (userManager == null) {
            userManager = UserManager.getInstance();
            getServletContext().setAttribute("userManager", userManager);
        }

        userManager.addRun(dto.getUsername(), dto);

        Program program = GlobalProgramStore.getProgram(dto.getUsername(), dto.getProgramName());
        if (program != null) {
            int usedCredits = dto.getCycles() + dto.getArchitecture().getCreditsCost();
            program.recordRun(usedCredits); // מעדכן RunCount ו־Avg.Credits
            System.out.println("Updated program stats for " + dto.getProgramName()
                    + " | Runs=" + program.getRunCount()
                    + " | Used credits=" + usedCredits);
        } else {
            System.out.println("Program not found in GlobalProgramStore: " + dto.getProgramName());
        }

        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
