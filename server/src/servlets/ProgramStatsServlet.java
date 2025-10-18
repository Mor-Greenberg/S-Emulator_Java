package servlets;

import com.google.gson.Gson;
import dto.ProgramStatsDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@WebServlet("/api/programs")
public class ProgramStatsServlet extends HttpServlet {
    private static final List<ProgramStatsDTO> sharedPrograms = new CopyOnWriteArrayList<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ProgramStatsDTO dto = new Gson().fromJson(req.getReader(), ProgramStatsDTO.class);
        sharedPrograms.removeIf(p -> p.getProgramName().equals(dto.getProgramName()));
        sharedPrograms.add(dto);
        System.out.println("✅ Added/Updated program stats: " + dto.getProgramName());
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = new Gson().toJson(sharedPrograms);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(json);
    }
}
