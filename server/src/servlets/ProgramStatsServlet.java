package servlets;

import com.google.gson.Gson;
import dto.ProgramStatsDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.program.Program;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
@WebServlet("/api/programs")
public class ProgramStatsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Program> programs = serverProgram.GlobalProgramsManager.getAllPrograms();
        List<ProgramStatsDTO> list = new ArrayList<>();

        for (Program p : programs.values()) {
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

        System.out.println("📤 Sending " + list.size() + " programs to all clients.");
        resp.getWriter().write(new Gson().toJson(list));
    }
}
