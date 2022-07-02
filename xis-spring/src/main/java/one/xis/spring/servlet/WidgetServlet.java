package one.xis.spring.servlet;


import one.xis.context.AppContext;
import one.xis.widget.Widget;
import one.xis.widget.Widgets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/xis/widget/*")
class WidgetServlet extends ResourceServlet<Widget> {

    private Widgets widgets;

    @Override
    public void init() {
        widgets = AppContext.getInstance("one.xis").getSingleton(Widgets.class);
    }

    @Override
    protected Widget getResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String urn = uri.substring(uri.lastIndexOf('/') + 1);
        return widgets.getWidget(urn);
    }
}
