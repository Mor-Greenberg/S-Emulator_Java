package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import user.UserManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Set<String> connectedUsers = new HashSet<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getReader().readLine().trim();


        synchronized (connectedUsers) {
            if (connectedUsers.contains(username)) {
                response.getWriter().write("TAKEN");
            } else {
                connectedUsers.add(username);

                HttpSession session = request.getSession(true);
                session.setAttribute("username", username);

                UserManager userManager = (UserManager) getServletContext().getAttribute("userManager");
                if (userManager == null) {
                    userManager = new UserManager();
                    getServletContext().setAttribute("userManager", userManager);
                }

                if (!userManager.userExists(username)) {
                    userManager.addUser(username);
                }

                response.getWriter().write("OK");
            }
        }
    }

    public static Set<String> getConnectedUsers() {
        return connectedUsers;
    }
}
