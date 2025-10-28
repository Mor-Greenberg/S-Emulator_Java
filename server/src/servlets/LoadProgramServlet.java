package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import serverProgram.GlobalProgramStore;
import java.io.IOException;

@WebServlet("/load-program")
public class LoadProgramServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/xml; charset=UTF-8");

        String programName = req.getParameter("name");
        if (programName == null || programName.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("<error>Missing program name</error>");
            return;
        }

        String xml = GlobalProgramStore.getXml(programName);
        if (xml == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("<error>Program not found on server</error>");
            System.out.println("Program '" + programName + "' not found on server");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(xml);

        System.out.println("Sent XML for '" + programName + "' to client");
    }
}
