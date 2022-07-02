package one.xis.spring.servlet;

import one.xis.resource.ResourceFile;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class ResourceServlet<R extends ResourceFile> extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResourceFile resource = getResource(request);
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (ifModifiedSince > -1 && !response.isCommitted() && ifModifiedSince == resource.getLastModified()) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else if (ifNoneMatch != null && ifNoneMatch.equals(Long.toString(resource.getLastModified()))) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.setContentType("application/javascript");
            response.setContentLength(resource.getLenght());
            response.setDateHeader("Last-Modified", resource.getLastModified());
            response.setHeader("ETag", Long.toString(resource.getLastModified()));
            response.getWriter().print(resource.getContent());
            response.getWriter().flush();
        }
    }

    protected abstract R getResource(HttpServletRequest request);
}
