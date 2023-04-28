package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import org.tinylog.Logger;

import java.util.Map;
import java.util.function.Function;

@XISComponent
@RequiredArgsConstructor
public class FrontendService {

    private final ControllerService controllerService;
    private final ConfigService configService;
    private final HtmlResourceService htmlResourceService;
    private final Resources resources;
    private final RequestFilters requestFilterChain;
    private Resource apiJsResource;

    @XISInit
    void init() {
        apiJsResource = resources.getByPath("xis.js");
    }

    public Config getConfig() {
        return configService.getConfig();
    }

    public Response invokePageActionMethod(Request request) {
        return applyFilterChain(request, controllerService::invokePageActionMethod);
    }

    public Response invokeWidgetActionMethod(Request request) {
        return applyFilterChain(request, controllerService::invokeWidgetActionMethod);
    }

    public Response invokePageModelMethods(Request request) {
        return applyFilterChain(request, controllerService::invokePageModelMethods);
    }

    public Response invokeWidgetModelMethods(Request request) {
        return applyFilterChain(request, controllerService::invokeWidgetModelMethods);
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
    
    private Response applyFilterChain(Request request, Function<Request, Response> responder) {
        var chain = requestFilterChain.apply(request);
        if (chain.isInterrupt()) {
            return new Response(chain.getHttpStatus(), chain.getData(), null, null);
        }
        return responder.apply(request);
    }
}
