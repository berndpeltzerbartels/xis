package one.xis.js;

import one.xis.resource.ResourceFile;
import one.xis.servlet.ResourceServlet;

import javax.servlet.http.HttpServletRequest;

class PageServlet extends ResourceServlet {

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        return null;
    }
}
