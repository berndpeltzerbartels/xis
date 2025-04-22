package one.xis.server;

import java.util.Locale;
import java.util.Map;

public interface FrameworkController<RES, REQ> {

    ClientConfig getComponentConfig();

    RES getPageModel(ClientRequest request, Locale locale);

    RES getFormModel(ClientRequest request, Locale locale);

    RES getWidgetModel(ClientRequest request, Locale locale);

    RES onPageLinkAction(ClientRequest request, Locale locale);

    RES onWidgetLinkAction(ClientRequest request, Locale locale);

    RES onFormAction(ClientRequest request, Locale locale);

    String getPageJavascript(REQ request);

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
