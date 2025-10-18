package servlets;

import com.google.gson.Gson;
import dto.ProgramStatsDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.program.Program;
import logic.xml.XmlLoader;
import serverProgram.ProgramManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@WebServlet("/load-program")
public class LoadProgramServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setCharacterEncoding("UTF-8");

        String xmlContent = request.getReader().lines().collect(Collectors.joining("\n"));
        Program program = null;
        try {
            program = XmlLoader.fromXmlString(xmlContent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String uploader = (String) request.getSession().getAttribute("username");

        // עדכון רשימת התוכניות (metadata בלבד)
        ServletContext context = getServletContext();
        List<ProgramStatsDTO> allPrograms =
                (List<ProgramStatsDTO>) context.getAttribute("programStatsList");
        if (allPrograms == null) {
            allPrograms = new ArrayList<>();
            context.setAttribute("programStatsList", allPrograms);
        }

        ProgramStatsDTO dto = new ProgramStatsDTO(
                program.getName(),
                uploader,
                program.getInstructions().size(),
                program.calculateMaxDegree(),
                program.getRunCount(),
                program.getAverageCredits()
        );

        synchronized (allPrograms) {
            Program finalProgram = program;
            allPrograms.removeIf(p -> p.getProgramName().equals(finalProgram.getName()));
            allPrograms.add(dto);
        }

        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(new Gson().toJson(dto));
    }
}
