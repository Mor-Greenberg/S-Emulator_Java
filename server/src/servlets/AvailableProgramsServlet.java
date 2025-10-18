package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.program.Program;
import logic.execution.ExecutionContextImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/available-programs")
public class AvailableProgramsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        // אוספים את שמות התוכניות בלבד
        List<String> programNames = new ArrayList<>(ExecutionContextImpl.getGlobalProgramMap().keySet());

        // מחזירים JSON
        String json = new Gson().toJson(programNames);
        response.getWriter().write(json);
    }
}
