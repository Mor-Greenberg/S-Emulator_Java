package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.program.Program;
import logic.xml.XmlLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@WebServlet("/load-program")
public class LoadProgramServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");

        System.out.println("🌐 Incoming POST /load-program");

        // בדיקה בסיסית לסוג תוכן
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/xml")) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            response.getWriter().write("❌ Error: Content-Type must be application/xml");
            return;
        }

        try {
            System.out.println("📥 Start reading XML");
            String xmlContent = new BufferedReader(new InputStreamReader(request.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            System.out.println("✅ XML content received:\n" + xmlContent);

            // ניסיון לפענוח ה־XML
            System.out.println("🚀 Calling XmlLoader...");
            Program program = XmlLoader.fromXmlString(xmlContent);
            System.out.println("✅ XmlLoader success");

            // הגנה נוספת סביב getName
            try {
                String name = program.getName();
                System.out.println("📛 Program name: " + name);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("✅ File loaded successfully! Program name: " + name);
            } catch (Exception nameEx) {
                System.err.println("❌ Error when accessing program.getName():");
                nameEx.printStackTrace();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("⚠️ Loaded program but failed to read name: " + nameEx.getMessage());
            }

        } catch (Exception e) {
            Throwable root = findRootCause(e);

            if (root instanceof IllegalArgumentException iae) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("❌ XML validation error:\n" + iae.getMessage());
                System.err.println("Validation Error: " + iae.getMessage());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("❌ Server error: " + root.getMessage());
                System.err.println("❌ Unhandled exception in servlet:");
                root.printStackTrace();
            }
        }
    }

    private Throwable findRootCause(Throwable t) {
        System.err.println("SERVER ERROR");
        t.printStackTrace(); // מדפיס את כל השרשור – חובה לדיבוג אמיתי
        Throwable result = t;
        while (result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }



}
