package one.xis.spring.page;

import one.xis.page.PageService;
import one.xis.path.PathUtils;
import one.xis.resource.ResourceFile;
import one.xis.spring.servlet.ResourceServlet;
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
        String jsClassname = PathUtils.stripSuffix(uri.substring(uri.lastIndexOf('/') + 1));
        return pageService.getPage(jsClassname);
    }

    @Override
    protected String getContentType() {
        return "text/javascript; charset=utf-8";
    }
}
