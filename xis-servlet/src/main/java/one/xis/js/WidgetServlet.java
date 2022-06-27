package one.xis.js;


import one.xis.context.XISInject;
import one.xis.servlet.ResourceServlet;
import one.xis.widget.Widget;
import one.xis.widget.Widgets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/widget.js")
class WidgetServlet extends ResourceServlet<Widget> {

    @XISInject
    private Widgets widgets;

    @Override
    protected Widget getResource(HttpServletRequest request) {
        return widgets.getWidget(request.getParameter("id"));
    }
}
