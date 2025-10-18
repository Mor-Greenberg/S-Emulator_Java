package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/get-program-xml")
public class GetProgramXmlServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String programName = req.getParameter("name");
        if (programName == null || programName.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing program name");
            return;
        }

        // כאן תחפשי את הקובץ או תשחזרי XML לפי איך שהוא נשמר בשרת
        // ⚠️ אם השרת שלך לא שומר את ה־XML (לפי דרישת הבודק) – צריך פתרון אחר (ראו שלב 2)
    }
}
