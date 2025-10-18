package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@WebServlet("/request-program")
public class RequestProgramServlet extends HttpServlet {

    // מפה גלובלית בזיכרון בלבד — לא נשמרת בדיסק ולא נכתבת לשום קובץ
    private static final Map<String, String> programXmlMap = new ConcurrentHashMap<>();

    // =============== שלב 1: משתמש מעלה תוכנית ====================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uploader = (String) req.getSession().getAttribute("username");

        // קריאת תוכן ה־XML מהגוף של הבקשה
        String xmlContent = new BufferedReader(new InputStreamReader(req.getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        String programName = req.getParameter("name");
        if (programName == null || programName.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing program name.");
            return;
        }

        // שמירה זמנית בזיכרון בלבד (לא בקובץ!)
        programXmlMap.put(programName.trim(), xmlContent);
        System.out.println("📦 Received XML for program '" + programName + "' from user: " + uploader);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("XML stored in memory for " + programName);
    }

    // =============== שלב 2: משתמש אחר מבקש את התוכנית ====================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String programName = req.getParameter("name");
        if (programName == null || programName.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing program name");
            return;
        }

        String xml = programXmlMap.get(programName.trim());
        if (xml == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Program not available in memory");
            System.out.println("❌ Program '" + programName + "' not found in memory map");
            return;
        }

        resp.setContentType("application/xml; charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(xml);
        System.out.println("📤 Sent XML for '" + programName + "' to another client");
    }
}
