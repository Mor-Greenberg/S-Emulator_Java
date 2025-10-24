package servlets;

import com.google.gson.Gson;
import dto.ProgramStatsDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.program.Program;
import logic.xml.XmlLoader;
import serverProgram.GlobalProgramsManager;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/load-program")
public class LoadProgramServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setCharacterEncoding("UTF-8");

        String xmlContent = request.getReader().lines().collect(Collectors.joining("\n"));
        Program program;

        try {
            program = XmlLoader.fromXmlString(xmlContent);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid XML: " + e.getMessage());
            return;
        }

        String uploader = (String) request.getSession().getAttribute("username");
        if (uploader == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User not logged in");
            return;
        }

        // שמירה במפה הגלובלית (כולל פונקציות פנימיות)
        GlobalProgramsManager.addProgram(program);
        for (Program func : program.getFunctionMap().values()) {
            if (func.isFunction()) {
                func.setUploaderName(uploader);
                GlobalProgramsManager.addProgram(func);
            }
        }

        System.out.println("Loaded program: " + program.getName() +
                " and all functions into GlobalProgramsManager");
        System.out.println("Current programs in memory: " +
                GlobalProgramsManager.getAllPrograms().keySet());

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
            allPrograms.removeIf(p -> p.getProgramName().equals(program.getName()));
            allPrograms.add(dto);
        }

        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(new Gson().toJson(dto));
    }
}
