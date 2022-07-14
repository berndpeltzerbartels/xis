package one.xis.jsc;

import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/xis/initializer.js")
class InitializerServlet extends HttpServlet {

    @Autowired
    private Initializer initializer;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/javascript; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(initializer.getContent());
        out.flush();
    }


}
