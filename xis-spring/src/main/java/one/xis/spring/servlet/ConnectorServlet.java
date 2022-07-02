package one.xis.spring.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "*.connector.json")
class ConnectorServlet extends HttpServlet {

    private String json;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setContentLength(json.getBytes("utf-8").length);
        try (PrintWriter out = response.getWriter()) {
            out.println(json);
        }
    }
}
