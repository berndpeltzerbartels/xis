package one.xis.jsc;

import one.xis.resource.ResourceFile;
import one.xis.spring.servlet.ResourceServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/xis/page/*")
class PageServlet extends ResourceServlet {

    @Autowired
    private PageJavascripts pageJavascripts;

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String key = uri.substring(uri.lastIndexOf('/') + 1);
        return pageJavascripts.get(key);
    }

    @Override
    protected String getContentType() {
        return "text/javascript; charset=utf-8";
    }
}
