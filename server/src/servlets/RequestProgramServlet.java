
package servlets;

import com.google.gson.Gson;
import dto.ProgramStatsDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.program.Program;
import logic.xml.XmlLoader;
import serverProgram.GlobalProgramStore;
import user.UserManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@WebServlet("/request-program")
public class RequestProgramServlet extends HttpServlet {

    private static final ConcurrentHashMap<String, String> uploaderMap = new ConcurrentHashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String uploader = req.getParameter("uploader");
        if (uploader == null || uploader.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Missing uploader name.\"}");
            return;
        }

        String xmlContent = new BufferedReader(new InputStreamReader(req.getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        try {
            Program program = XmlLoader.fromXmlString(xmlContent, uploader);
            program.setUploaderName(uploader);
            String programName = program.getName();

            GlobalProgramStore.addProgram(programName, xmlContent, program);
            GlobalProgramStore.addXml(programName, xmlContent);
            uploaderMap.put(programName, uploader);

            System.out.println("Stored main program: " + programName + " (uploader=" + uploader + ")");

            int count = 0;
            for (Program func : program.getFunctionMap().values()) {
                if (func.isFunction()) {
                    GlobalProgramStore.addProgram(func.getName(), xmlContent, func);
                    GlobalProgramStore.addXml(func.getName(), xmlContent);
                    count++;
                    System.out.println("Added function: " + func.getName() +
                            " (parent=" + programName + ", uploader=" + uploader + ")");
                }
            }

            System.out.println("Total stored items: " + GlobalProgramStore.getProgramCache().size());
            System.out.println("Now in map: " + GlobalProgramStore.getProgramCache().keySet());

            UserManager userManager = UserManager.getInstance();
            userManager.incrementMainProgramsUploaded(uploader);
            for (int i = 0; i < count; i++) {
                userManager.incrementContributedFunctions(uploader);
            }

            ProgramStatsDTO dto = new ProgramStatsDTO(
                    programName,
                    uploader,
                    program.getInstructions().size(),
                    program.calculateMaxDegree(),
                    program.getRunCount(),
                    program.getAverageCredits()
            );

            String json = new Gson().toJson(dto);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(json);

        } catch (Exception e) {
            e.printStackTrace();

                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");

            String message = e.getMessage();
            if (message == null || message.isBlank()) {
                message = "Unknown validation error occurred.";
            }

            String jsonError = new Gson().toJson(
                    java.util.Map.of("error", message)
            );

            resp.getWriter().write(jsonError);
        }


    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String programName = req.getParameter("name");
        if (programName == null || programName.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing program name");
            return;
        }

        Program program = GlobalProgramStore.getProgram(programName, uploaderMap.get(programName));
        String xml = GlobalProgramStore.getXml(programName);


        if (program == null || xml == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Program/function not found on server.");
            System.out.println(programName + " not found in store");
            return;
        }

        resp.setContentType("application/xml; charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(xml);
        System.out.println("Sent XML for '" + programName + "' to client");
    }
}
