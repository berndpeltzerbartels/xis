package one.xis.js;


import one.xis.context.Inj;
import one.xis.resource.Resource;
import one.xis.servlet.ResourceServlet;
import one.xis.widget.Widgets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/widget.js")
class WidgetServlet extends ResourceServlet {

    @Inj
    private Widgets widgets;

    @Override
    protected Resource<String> getResource(HttpServletRequest request) {
        return widgets.getWidgetJs(request.getParameter("urn"));
    }
}
