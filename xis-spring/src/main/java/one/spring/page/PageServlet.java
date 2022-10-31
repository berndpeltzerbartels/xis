package one.spring.page;

import one.spring.servlet.ResourceServlet;
import one.xis.page.PageService;
import one.xis.resource.ResourceFile;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/xis/page/*")
class PageServlet extends ResourceServlet {

    @Autowired
    private PageService pageService;

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String key = uri.substring(uri.lastIndexOf('/') + 1);
        return pageService.getPage(key);
    }

    @Override
    protected String getContentType() {
        return "text/javascript; charset=utf-8";
    }
}
