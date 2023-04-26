package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import org.tinylog.Logger;

import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class FrontendService {

    private final ControllerService controllerService;
    private final ConfigService configService;
    private final HtmlResourceService htmlResourceService;
    private final Resources resources;
    private Resource apiJsResource;

    @XISInit
    void init() {
        apiJsResource = resources.getByPath("xis.js");
    }

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
        var head = htmlResourceService.getPageHead(id);
        Logger.info(head);
        return head;
    }

    public String getPageBody(String id) {
        var body = htmlResourceService.getPageBody(id);
        Logger.info(body);
        return body;
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


    public String getApiJs() {
        return apiJsResource.getContent();
    }
}
