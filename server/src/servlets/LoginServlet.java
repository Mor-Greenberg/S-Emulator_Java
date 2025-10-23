package servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import user.UserManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Set<String> connectedUsers = new HashSet<>();

    @Override
    public void init() {
        getServletContext().setAttribute("userManager", UserManager.getInstance());
    }



    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getReader().readLine();
        if (username == null || username.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Username required");
            return;
        }
        username = username.trim();

        synchronized (connectedUsers) {
            if (connectedUsers.contains(username)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("TAKEN");
                return;
            }

            connectedUsers.add(username);

            HttpSession session = request.getSession(true);
            session.setAttribute("username", username);

            UserManager userManager = UserManager.getInstance();
            userManager.addUser(username);

            System.out.println("User logged in: " + username);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("OK");
        }
    }

    public static Set<String> getConnectedUsers() {
        return connectedUsers;
    }
}
