package one.xis.jscomponent;

import one.xis.resource.ResourceFile;
import one.xis.spring.servlet.ResourceServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/xis/page/*")
class PageServlet extends ResourceServlet {

    @Autowired
    private Pages pages;

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String urn = uri.substring(uri.lastIndexOf('/') + 1);
        return pages.get(urn);
    }

    @Override
    protected String getContentType() {
        return "text/javascript";
    }
}
