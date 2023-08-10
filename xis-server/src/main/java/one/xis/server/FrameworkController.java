package one.xis.server;

import java.util.Map;

public interface FrameworkController {

    ClientConfig getComponentConfig();

    ServerResponse getPageModel(ClientRequest request);

    ServerResponse getWidgetModel(ClientRequest request);

    ServerResponse onPageAction(ClientRequest request);

    ServerResponse onWidgetAction(ClientRequest request);

    String getPage(String id);

    String getPageHead(String id);

    String getPageBody(String id);

    Map<String, String> getBodyAttributes(String id);

    String getWidgetHtml(String id);

    String getAppJs();

    String getClassesJs();

    String getMainJs();

    String getFunctionsJs();
}
