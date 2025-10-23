package servlets;

import com.google.gson.Gson;
import dto.UserRunEntryDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import user.UserManager;

import java.io.IOException;

@WebServlet("/api/add-run")
public class AddRunServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        UserRunEntryDTO run = gson.fromJson(req.getReader(), UserRunEntryDTO.class);
        userManager.addRun(username, run);
        userManager.incrementExecutionCount(username);

        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
