package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class FrontendService {

    private final ControllerService controllerService;
    private final ConfigService configService;
    private final HtmlResourceService htmlResourceService;

    public Config getConfig() {
        return configService.getConfig();
    }

    public Response invokePageActionMethod(Request request) {
        return controllerService.invokePageActionMethod(request);
    }

    public Response invokeWidgetActionMethod(Request request) {
        return controllerService.invokeWidgetActionMethod(request);
    }

    public Response invokePageModelMethods(Request request) {
        return controllerService.invokePageModelMethods(request);
    }

    public Response invokeWidgetModelMethods(Request request) {
        return controllerService.invokeWidgetModelMethods(request);
    }

    public String getPageHead(String id) {
        return htmlResourceService.getPageHead(id);
    }

    public String getPageBody(String id) {
        return htmlResourceService.getPageBody(id);
    }

    public Map<String, String> getBodyAttributes(String id) {
        return htmlResourceService.getBodyAttributes(id);
    }

    public String getWidgetHtml(String id) {
        return htmlResourceService.getWidgetHtml(id);
    }

    public String getRootPageHtml() {
        return htmlResourceService.getRootPageHtml();
    }


}
