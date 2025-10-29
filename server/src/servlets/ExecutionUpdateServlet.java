package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import user.User;
import user.UserManager;
import java.io.IOException;

@WebServlet("/execution-update")
public class ExecutionUpdateServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"No active session\"}");
            return;
        }

        String username = (String) session.getAttribute("username");
        String programName = req.getParameter("program");
        String creditsParam = req.getParameter("creditsUsed");

        if (username == null || programName == null || programName.isBlank() || creditsParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Missing or invalid parameters\"}");
            return;
        }

        int creditsUsed;
        try {
            creditsUsed = Integer.parseInt(creditsParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"creditsUsed must be an integer\"}");
            return;
        }

        UserManager userManager = UserManager.getInstance();
        User user = userManager.getUser(username);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"User not found\"}");
            return;
        }

        // עדכון הנתונים (קרדיטים וריצות)
        userManager.recordExecution(username, programName, creditsUsed);

        System.out.println("[DEBUG] Updated execution count for " + username +
                " (program=" + programName + ", creditsUsed=" + creditsUsed + ")");
        System.out.println("[DEBUG] Deducting " + creditsUsed + " credits from user " + username);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("{\"status\":\"ok\",\"remaining\":" + user.getCredits() + "}");
    }
}
