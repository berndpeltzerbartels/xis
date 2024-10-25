package one.xis.server;

import java.util.Map;

public interface FrontendService {
    ClientConfig getConfig();

    ServerResponse processActionRequest(ClientRequest request);

    ServerResponse processModelDataRequest(ClientRequest request);

    ServerResponse processFormDataRequest(ClientRequest request);

    String getPage(String id);

    String getPageHead(String id);

    String getPageBody(String id);

    Map<String, String> getBodyAttributes(String id);

    String getWidgetHtml(String id);

    String getRootPageHtml();

    String getAppJs();

    String getClassesJs();

    String getMainJs();

    String getFunctionsJs();
}
