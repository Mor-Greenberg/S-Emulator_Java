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

        UserManager userManager = UserManager.getInstance();

        List<UserStatsDTO> statsList = new ArrayList<>();

        for (User user : userManager.getAllUsers().values()) {
            statsList.add(new UserStatsDTO(user));
        }

        synchronized (LoginServlet.getConnectedUsers()) {
            for (String username : LoginServlet.getConnectedUsers()) {
                if (!userManager.userExists(username)) {
                    userManager.addUser(username);
                    statsList.add(new UserStatsDTO(userManager.getUser(username)));
                }
            }
        }

        String json = new Gson().toJson(statsList);
        response.getWriter().write(json);
    }
}
