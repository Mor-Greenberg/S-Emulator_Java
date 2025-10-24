package servlets;

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
        String uploader = (String) req.getSession().getAttribute("username");
        if (uploader == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("User not logged in.");
            return;
        }

        String xmlContent = new BufferedReader(new InputStreamReader(req.getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        String programName = req.getParameter("name");
        if (programName == null || programName.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing program name.");
            return;
        }

        try {
            Program program = XmlLoader.fromXmlString(xmlContent);
            program.setUploaderName(uploader);

            // Save program and its XML in the hybrid store
            GlobalProgramStore.addProgram(programName, xmlContent, program);
            uploaderMap.put(programName, uploader);
            System.out.println("Stored main program: " + programName + " (uploader=" + uploader + ")");

            // Save all inner functions too
            int count = 0;
            for (Program func : program.getFunctionMap().values()) {
                if (func.isFunction()) {
                    GlobalProgramStore.addProgram(func.getName(), xmlContent, func);
                    count++;
                    System.out.println("Added function: " + func.getName() +
                            " (parent=" + program.getName() + ", uploader=" + uploader + ")");
                }
            }

            System.out.println("Total stored items: " + GlobalProgramStore.getProgramCache().size());
            System.out.println("XML map keys: " + GlobalProgramStore.getXmlMap().keySet());

            // Update user stats
            UserManager userManager = UserManager.getInstance();
            userManager.incrementMainProgramsUploaded(uploader);
            for (int i = 0; i < count; i++) {
                userManager.incrementContributedFunctions(uploader);
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Program and functions saved successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error parsing XML: " + e.getMessage());
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

        // Try cache or parse from XML automatically
        Program program = GlobalProgramStore.getProgram(programName);
        if (program == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Program/function not found on server.");
            System.out.println(programName + " not found in store");
            return;
        }

        // Send the raw XML (what XmlLoader can read)
        String xml = GlobalProgramStore.getXml(programName);
        if (xml == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("No XML found for " + programName);
            return;
        }

        resp.setContentType("application/xml; charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(xml);
        System.out.println("Sent XML for '" + programName + "' to client");
    }
}
