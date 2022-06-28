package one.xis.js;


import one.xis.XISContext;
import one.xis.servlet.ResourceServlet;
import one.xis.widget.Widget;
import one.xis.widget.Widgets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/widget.js")
class WidgetServlet extends ResourceServlet<Widget> {

    private Widgets widgets;

    @Override
    public void init() {
        widgets = XISContext.getInstance().getSingleton(Widgets.class);
    }

    @Override
    protected Widget getResource(HttpServletRequest request) {
        return widgets.getWidget(request.getParameter("id"));
    }
}
