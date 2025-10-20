package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.program.Program;
import logic.xml.XmlLoader;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@WebServlet("/request-program")
public class RequestProgramServlet extends HttpServlet {

    private static final Map<String, String> programXmlMap = new ConcurrentHashMap<>();
    private static final Map<String, String> uploaderMap = new ConcurrentHashMap<>();

    public static Map<String, String> getProgramXmlMap() { return programXmlMap; }
    public static Map<String, String> getUploaderMap() { return uploaderMap; }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uploader = (String) req.getSession().getAttribute("username");

        String xmlContent = new BufferedReader(new InputStreamReader(req.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        String programName = req.getParameter("name");

        if (programName == null || programName.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing program name.");
            return;
        }

        programXmlMap.put(programName.trim(), xmlContent);

        try {
            Program program = XmlLoader.fromXmlString(xmlContent);
            program.setUploaderName(uploader);

            // --- Save main program ---
            serverProgram.GlobalProgramsManager.addProgram(program);
            System.out.println("📦 Added main program: " + program.getName() + " (uploader=" + uploader + ")");

            // --- Save all inner functions ---
            int count = 0;
            for (Program func : program.getFunctionMap().values()) {
                if (func.isFunction()) {
                    func.setUploaderName(uploader);
                    serverProgram.GlobalFunctionsManager.addFunction(func);
                    count++;
                    System.out.println("🧩 Added function: " + func.getName() +
                            " (parent=" + program.getName() + ", uploader=" + uploader + ")");
                }
            }

            System.out.println("✅ Total functions saved: " + count);
            System.out.println("📊 Current GlobalFunctionsManager keys: " +
                    serverProgram.GlobalFunctionsManager.getAllFunctions().keySet());

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

        // נחפש קודם בתוכניות ובפונקציות הגלובליות
        String xml = programXmlMap.get(programName.trim());

        if (xml == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Program/function not found on server.");
            System.out.println("❌ '" + programName + "' not found in memory maps");
            return;
        }

        resp.setContentType("application/xml; charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(xml);
        System.out.println("📤 Sent XML for '" + programName + "' to client");
    }
}
