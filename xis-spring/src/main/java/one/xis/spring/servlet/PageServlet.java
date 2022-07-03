package one.xis.spring.servlet;

import one.xis.resource.ResourceFile;

import javax.servlet.http.HttpServletRequest;

class PageServlet extends ResourceServlet {

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        return null;
    }

    @Override
    protected String getContentType() {
        return "text/javascript";
    }
}
