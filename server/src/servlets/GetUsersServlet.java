package servlets;

import com.google.gson.Gson;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import user.User;
import user.UserManager;
import user.UserStatsDTO;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
@WebServlet("/get-users")
public class GetUsersServlet extends HttpServlet {

    private final UserManager userManager = UserManager.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Set<String> connectedUsers;
        synchronized (LoginServlet.getConnectedUsers()) {
            connectedUsers = Set.copyOf(LoginServlet.getConnectedUsers());
        }

        List<UserStatsDTO> statsList = new ArrayList<>();

        for (String username : connectedUsers) {
            User user = userManager.getUser(username);
            if (user != null) {
                statsList.add(new UserStatsDTO(user));
            }
        }

        String json = new Gson().toJson(statsList);
        PrintWriter out = response.getWriter();
        out.write(json);
        out.flush();
    }
}

