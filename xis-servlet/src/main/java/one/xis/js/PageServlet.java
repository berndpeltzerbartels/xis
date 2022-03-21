package one.xis.js;

import one.xis.utils.io.IOUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "*.html")
class PageServlet extends HttpServlet {

    private String html;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println(config.getServletContext().getRealPath("/"));
        html = IOUtils.getResourceAsString("main.html");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setContentLength(html.length());
        try (PrintWriter out = response.getWriter()) {
            out.println(html);
        }
    }
}
