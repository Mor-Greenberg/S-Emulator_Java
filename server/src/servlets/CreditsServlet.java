package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import user.User;
import user.UserManager;

import java.io.IOException;
import java.util.Map;

@WebServlet("/credits")
public class CreditsServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String username = (session != null) ? (String) session.getAttribute("username") : null;

        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        UserManager userManager = (UserManager) getServletContext().getAttribute("userManager");
        if (userManager == null) {
            userManager = UserManager.getInstance();
            getServletContext().setAttribute("userManager", userManager);
        }

        User user = userManager.getUser(username);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        resp.setContentType("application/json");
        resp.getWriter().write(new Gson().toJson(Map.of("credits", user.getCredits())));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String username = (session != null) ? (String) session.getAttribute("username") : null;

        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int delta;
        try {
            delta = Integer.parseInt(req.getReader().readLine());
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid credit value");
            return;
        }

        UserManager userManager = (UserManager) getServletContext().getAttribute("userManager");
        if (userManager == null) {
            userManager = UserManager.getInstance();
            getServletContext().setAttribute("userManager", userManager);
        }

        User user = userManager.getUser(username);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (delta >= 0)
            user.addCredits(delta);

        else
            user.tryDeductCredits(-delta);

        resp.setContentType("application/json");
        resp.getWriter().write(new Gson().toJson(Map.of("credits", user.getCredits())));
    }
}
