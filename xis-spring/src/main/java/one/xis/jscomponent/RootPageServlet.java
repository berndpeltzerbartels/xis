package one.xis.jscomponent;

import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"*.html"})
class RootPageServlet extends HttpServlet {

    @Autowired
    private RootPage rootPage;

    @Override
    public void init() {
        rootPage.createContent();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(rootPage.getContent());
        out.flush();
    }
}
