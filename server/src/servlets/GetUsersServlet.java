package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import user.User;
import user.UserManager;
import dto.UserStatsDTO;

import java.io.IOException;
import java.util.*;

@WebServlet("/get-users")
public class GetUsersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        // ודא ש-UserManager מוכן
        UserManager userManager = UserManager.getInstance();

        // העתק מוגן של רשימת המשתמשים המחוברים
        Set<String> connectedUsersSnapshot;
        synchronized (LoginServlet.getConnectedUsers()) {
            connectedUsersSnapshot = new HashSet<>(LoginServlet.getConnectedUsers());
        }

        List<UserStatsDTO> statsList = new ArrayList<>();

        for (String username : connectedUsersSnapshot) {
            User user = userManager.getUser(username);
            if (user != null) {
                statsList.add(new UserStatsDTO(user));
            } else {
                // אם משום מה המשתמש לא קיים, נוסיף אותו
                userManager.addUser(username);
                statsList.add(new UserStatsDTO(userManager.getUser(username)));
                System.out.println("⚠️ Added missing user to manager: " + username);
            }
        }

        String json = new Gson().toJson(statsList);
        response.getWriter().write(json);
    }
}
