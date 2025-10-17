package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.execution.ExecutionContextImpl;
import logic.program.Program;
import serverProgram.ProgramStats;
import user.User;
import user.UserManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/programs")
public class ProgramStatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<ProgramStats> result = new ArrayList<>();

        for (Program p : ExecutionContextImpl.getGlobalProgramMap().values()) {
            if (!p.isFunction()) {
                String programName = p.getName();
                String uploaderName = p.getUploaderName();

                User uploader = UserManager.getInstance().getUser(uploaderName);
                if (uploader == null) continue;

                ProgramStats stats = new ProgramStats(
                        programName,
                        uploaderName,
                        p.getInstructions().size(),
                        p.calculateMaxDegree(),
                        uploader.getRunCountForProgram(programName),
                        uploader.getAverageCreditsForProgram(programName)
                );

                result.add(stats);
            }
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String json = new Gson().toJson(result);
        resp.getWriter().write(json);
    }
}
