package one.xis.js;


import one.xis.resource.Resource;
import one.xis.servlet.ResourceServlet;
import one.xis.widget.Widgets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/widget.js")
class WidgetServlet extends ResourceServlet {

    private Widgets widgets = new Widgets();
    
    @Override
    protected Resource getResource(HttpServletRequest request) {
        return widgets.getWidgetJs(request.getParameter("urn"));
    }
}
