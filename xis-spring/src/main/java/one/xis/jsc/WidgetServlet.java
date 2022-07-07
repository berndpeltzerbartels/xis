package one.xis.jsc;


import one.xis.resource.ResourceFile;
import one.xis.spring.servlet.ResourceServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/xis/widget/*")
class WidgetServlet extends ResourceServlet {

    @Autowired
    private Widgets widgets;

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        String key = request.getRequestURI();
        String urn = key.substring(key.lastIndexOf('/') + 1);
        return widgets.get(urn);
    }

    @Override
    protected String getContentType() {
        return "text/javascript; charset=utf-8";
    }
}
