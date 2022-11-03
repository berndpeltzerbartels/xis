package one.xis.spring.js;

import one.xis.root.RootPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/one/xis/initializer.js")
class InitializerServlet extends HttpServlet {

    @Autowired
    private RootPageService rootPageService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/javascript; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(rootPageService.getInitializerScipt());
        out.flush();
    }


}
