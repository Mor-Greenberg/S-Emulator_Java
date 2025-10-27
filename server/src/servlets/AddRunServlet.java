package servlets;

import com.google.gson.Gson;
import dto.UserRunEntryDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import user.UserManager;

import java.io.IOException;

@WebServlet("/api/add-run")
public class AddRunServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String programName = req.getParameter("programName");
        String usedCreditsStr = req.getParameter("usedCredits");

        if (username == null || programName == null || usedCreditsStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameters");
            return;
        }

        int usedCredits;
        try {
            usedCredits = Integer.parseInt(usedCreditsStr);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid usedCredits value");
            return;
        }

       // UserRunEntryDTO entry = new UserRunEntryDTO(username,programName, usedCredits);

       // UserManager.getInstance().addRun(username, entry);

       // System.out.println("Added run for user: " + username + " | program=" + programName + " | usedCredits=" + usedCredits);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
