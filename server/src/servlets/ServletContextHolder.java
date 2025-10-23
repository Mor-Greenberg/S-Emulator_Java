package servlets;

import jakarta.servlet.ServletContext;

public class ServletContextHolder {

    private static ServletContext context;

    public static void setContext(ServletContext servletContext) {
        context = servletContext;
    }

    public static ServletContext getContext() {
        return context;
    }
}
