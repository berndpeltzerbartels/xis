package one.xis.spring.widget;


import one.xis.path.PathUtils;
import one.xis.resource.Resource;
import one.xis.spring.servlet.ResourceServlet;
import one.xis.widget.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(urlPatterns = "/xis/widget/*")
class WidgetServlet extends ResourceServlet {

    @Autowired
    private WidgetService widgetService;

    @Override
    protected Resource getResource(HttpServletRequest request) {
        String key = request.getRequestURI();
        String jsClassname = PathUtils.stripSuffix(key.substring(key.lastIndexOf('/') + 1));
        return widgetService.getWidgetComponent(jsClassname);
    }

    @Override
    protected String getContentType() {
        return "text/javascript; charset=utf-8";
    }
}
