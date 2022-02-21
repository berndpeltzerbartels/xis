package one.xis.js;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/__xis")
class JSServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSResource jsResource = JSResourceLoader.getJsResource();
        long ifModifiedSince = req.getDateHeader("If-Modified-Since");
        String ifNoneMatch = req.getHeader("If-None-Match");
        if (ifModifiedSince > -1 && !resp.isCommitted() && ifModifiedSince == jsResource.getLastModified()) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED); // TODO testen
        } else if (ifNoneMatch != null && ifNoneMatch.equals(Long.toString(jsResource.getLastModified()))) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED); // TODO  testen
        } else {
            resp.setContentType("application/javascript");
            resp.setContentLength(jsResource.getJavascript().length());
            resp.setDateHeader("Last-Modified", jsResource.getLastModified());
            resp.setHeader("ETag", Long.toString(jsResource.getLastModified()));
            resp.getWriter().print(jsResource.getJavascript());
            resp.getWriter().flush();
        }
    }
}
