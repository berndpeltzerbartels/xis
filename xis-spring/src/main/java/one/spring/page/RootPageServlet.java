package one.spring.page;

import one.xis.root.RootPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"*.html", ""})
class RootPageServlet extends HttpServlet {

    @Autowired
    private RootPageService pageService;

    @Override
    public void init() throws ServletException {
        pageService.createRootContent();
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(pageService.getRootPageHtml());
        out.flush();
    }
}
