package servlets;

import com.google.gson.Gson;
import dto.UserRunEntryDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import user.UserManager;

import java.io.IOException;
import java.util.List;

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

        List<UserRunEntryDTO> history = userManager.getUserHistory(username);

        System.out.println("🔎 Fetching history for " + username +
                " → " + history.size() + " runs");

        String json = gson.toJson(history);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(json);
    }
}
