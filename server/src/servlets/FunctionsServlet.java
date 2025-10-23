package servlets;

import com.google.gson.Gson;
import dto.FunctionDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.program.Program;
import java.io.IOException;
import java.util.*;

@WebServlet("/functions")
public class FunctionsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Program> funcs = serverProgram.GlobalFunctionsManager.getAllFunctions();
        List<FunctionDTO> list = new ArrayList<>();

        for (Program func : funcs.values()) {
            FunctionDTO dto = new FunctionDTO(
                    func.getName(),
                    func.getParentProgramName(),
                    func.getUploaderName(),
                    func.getInstructions().size(),
                    func.calculateMaxDegree()
            );
            list.add(dto);
        }

        resp.getWriter().write(new Gson().toJson(list));
    }
}
