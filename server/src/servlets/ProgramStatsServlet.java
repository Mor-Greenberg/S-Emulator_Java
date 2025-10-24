package servlets;

import com.google.gson.Gson;
import dto.ProgramStatsDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.program.Program;
import serverProgram.GlobalProgramStore;

import java.io.IOException;
import java.util.*;

@WebServlet("/api/programs")
public class ProgramStatsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        Map<String, Program> programs = GlobalProgramStore.getProgramCache();
        List<ProgramStatsDTO> list = new ArrayList<>();

        for (Program p : programs.values()) {
            if (p.isFunction()) continue;

            ProgramStatsDTO dto = new ProgramStatsDTO(
                    p.getName(),
                    p.getUploaderName(),
                    p.getInstructions().size(),
                    p.calculateMaxDegree(),
                    p.getRunCount(),
                    p.getAverageCredits()
            );
            list.add(dto);
        }

        String json = new Gson().toJson(list);
        resp.getWriter().write(json);
    }
}
