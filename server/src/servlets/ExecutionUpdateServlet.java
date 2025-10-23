package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/execution-update")
public class ExecutionUpdateServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = (String) req.getSession().getAttribute("username");
        String programName = req.getParameter("program");
        int creditsUsed = Integer.parseInt(req.getParameter("creditsUsed"));

        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        user.UserManager userManager = user.UserManager.getInstance();
        userManager.trackExecution(username, programName, creditsUsed);

        System.out.println("Updated execution count for " + username +
                " (program=" + programName + ", creditsUsed=" + creditsUsed + ")");

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("{\"status\":\"ok\"}");
    }
}
