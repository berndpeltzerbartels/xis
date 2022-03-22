package one.xis.js;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/__xis.js")
class JavascriptServlet extends HttpServlet {

    private JavascriptResource resource;

    JavascriptServlet() {
        resource = JavascriptResources.getResource();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (ifModifiedSince > -1 && !response.isCommitted() && ifModifiedSince == resource.getLastModified()) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else if (ifNoneMatch != null && ifNoneMatch.equals(Long.toString(resource.getLastModified()))) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.setContentType("application/javascript");
            response.setContentLength(resource.getJavascript().length());
            response.setDateHeader("Last-Modified", resource.getLastModified());
            response.setHeader("ETag", Long.toString(resource.getLastModified()));
            response.getWriter().print(resource.getJavascript());
            response.getWriter().flush();
        }
    }
}
