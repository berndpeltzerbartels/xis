package one.xis.server;

import java.util.Map;

public interface FrameworkController {

    Config getComponentConfig();

    Response getPageModel(Request request);

    Response getWidgetModel(Request request);

    Response onPageAction(Request request);

    Response onWidgetAction(Request request);

    String getPageHead(String id);

    String getPageBody(String id);

    Map<String, String> getBodyAttributes(String id);

    String getWidgetHtml(String id);

}
