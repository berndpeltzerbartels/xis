package one.xis.spring.widget;


import one.xis.resource.ResourceFile;
import one.xis.spring.servlet.ResourceServlet;
import one.xis.widget.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/one/xis/widget/*")
class WidgetServlet extends ResourceServlet {

    @Autowired
    private WidgetService widgetService;

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        String key = request.getRequestURI();
        String widgetId = key.substring(key.lastIndexOf('/') + 1);
        return widgetService.getWidgetJavascript(widgetId);
    }

    @Override
    protected String getContentType() {
        return "text/javascript; charset=utf-8";
    }
}
