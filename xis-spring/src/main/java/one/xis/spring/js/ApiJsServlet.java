package one.xis.spring.js;

import one.xis.resource.ResourceFile;
import one.xis.root.RootPageService;
import one.xis.spring.servlet.ResourceServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet("/xis/api/*")
class ApiJsServlet extends ResourceServlet {

    @Autowired
    private RootPageService rootPageService;

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String file = uri.substring(uri.lastIndexOf('/') + 1);
        return rootPageService.getJavascriptResource(file);
    }

    @Override
    protected String getContentType() {
        return "text/javascript; charset=utf-8";
    }
}
