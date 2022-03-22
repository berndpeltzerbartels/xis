package one.xis.js;

import one.xis.utils.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet
class ConnectorServlet extends HttpServlet {

    private String json;

    ConnectorServlet() {
        json = IOUtils.getResourceAsString("categories.json");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setContentLength(json.getBytes("utf-8").length);
        try (PrintWriter out = response.getWriter()) {
            out.println(json);
        }
    }
}
