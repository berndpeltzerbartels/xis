package one.xis.server;

import java.util.Locale;
import java.util.Map;

public interface FrameworkController<R> {

    ClientConfig getComponentConfig();

    R getPageModel(ClientRequest request, Locale locale);

    R getWidgetModel(ClientRequest request, Locale locale);

    R onPageAction(ClientRequest request, Locale locale);

    R onWidgetAction(ClientRequest request, Locale locale);

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
