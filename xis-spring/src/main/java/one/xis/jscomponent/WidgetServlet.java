package one.xis.jscomponent;


import one.xis.context.AppContext;
import one.xis.resource.ResourceFile;
import one.xis.spring.servlet.ResourceServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/xis/widget/*")
class WidgetServlet extends ResourceServlet {

    private Widgets widgets;

    @Override
    public void init() {
        widgets = AppContext.getInstance("one.xis").getSingleton(Widgets.class);
    }

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String urn = uri.substring(uri.lastIndexOf('/') + 1);
        return widgets.get(urn);
    }

    @Override
    protected String getContentType() {
        return "text/javascript";
    }
}
