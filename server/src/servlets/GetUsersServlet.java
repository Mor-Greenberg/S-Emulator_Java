package servlets;

import com.google.gson.Gson;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@WebServlet("/get-users")
public class GetUsersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");

        Set<String> users;
        synchronized (LoginServlet.getConnectedUsers()) {
            users = LoginServlet.getConnectedUsers();
        }

        String json = new Gson().toJson(users);
        PrintWriter out = response.getWriter();
        out.write(json);
        out.flush();
    }
}
