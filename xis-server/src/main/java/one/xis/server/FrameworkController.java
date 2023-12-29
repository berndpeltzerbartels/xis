package one.xis.server;

import java.util.Locale;
import java.util.Map;

public interface FrameworkController {

    ClientConfig getComponentConfig();

    ServerResponse getPageModel(ClientRequest request, Locale locale);

    ServerResponse getWidgetModel(ClientRequest request, Locale locale);

    ServerResponse onPageAction(ClientRequest request, Locale locale);

    ServerResponse onWidgetAction(ClientRequest request, Locale locale);

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
