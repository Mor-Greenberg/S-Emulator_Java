package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Set<String> connectedUsers = new HashSet<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getReader().readLine().trim();

        System.out.println("connectedUsers: " + connectedUsers);
        System.out.println("Username received: '" + username + "'");

        synchronized (connectedUsers) {
            if (connectedUsers.contains(username)) {
                response.getWriter().write("TAKEN");
            } else {
                connectedUsers.add(username);
                response.getWriter().write("OK");
                connectedUsers.add(username);
            }
        }
    }

    public static Set<String> getConnectedUsers() {
        return connectedUsers;
    }
}
