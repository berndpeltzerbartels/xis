package one.xis.server;

import java.util.Locale;
import java.util.Map;

public interface FrameworkController<FRESP, REQ, RESP> {

    ClientConfig getComponentConfig();

    FRESP getPageModel(ClientRequest request, Locale locale);

    FRESP getFormModel(ClientRequest request, Locale locale);

    FRESP getWidgetModel(ClientRequest request, Locale locale);

    FRESP onPageLinkAction(ClientRequest request, Locale locale);

    FRESP onWidgetLinkAction(ClientRequest request, Locale locale);

    FRESP onFormAction(ClientRequest request, Locale locale);

    RESP auth(REQ request, String provider);

    RenewTokenResponse renewToken(RenewTokenRequest request);

    String getPageJavascript(String path);

    String getPage(String id);

    String getPageHead(String id);

    String getPageBody(String id);

    Map<String, String> getBodyAttributes(String id);

    String getWidgetHtml(String id);

    String getAppJs();

    String getClassesJs();

    String getMainJs();

    String getFunctionsJs();

    String getBundleJs();
}
