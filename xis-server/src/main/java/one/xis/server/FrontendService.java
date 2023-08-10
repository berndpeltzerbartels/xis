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
    private Resource appJsResource;
    private Resource classesJsResource;
    private Resource mainJsResource;
    private Resource functionsJsResource;

    @XISInit
    void init() {
        appJsResource = resources.getByPath("app.js");
        classesJsResource = resources.getByPath("classes.js");
        mainJsResource = resources.getByPath("main.js");
        functionsJsResource = resources.getByPath("functions.js");
    }

    public Config getConfig() {
        return configService.getConfig();
    }

    public ServerResponse invokePageActionMethod(ClientRequest request) {
        return applyFilterChain(request, controllerService::invokePageActionMethod);
    }

    public ServerResponse invokeWidgetActionMethod(ClientRequest request) {
        return applyFilterChain(request, controllerService::invokeWidgetActionMethod);
    }

    public ServerResponse invokePageModelMethods(ClientRequest request) {
        return applyFilterChain(request, controllerService::invokePageModelMethods);
    }

    public ServerResponse invokeWidgetModelMethods(ClientRequest request) {
        return applyFilterChain(request, controllerService::invokeWidgetModelMethods);
    }

    public String getPage(String id) {
        return htmlResourceService.getPage(id);
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


    public String getAppJs() {
        return appJsResource.getContent();
    }

    public String getClassesJs() {
        return classesJsResource.getContent();
    }

    public String getMainJs() {
        return mainJsResource.getContent();
    }

    public String getFunctionsJs() {
        return functionsJsResource.getContent();
    }

    private ServerResponse applyFilterChain(ClientRequest request, Function<ClientRequest, ServerResponse> responder) {
        var chain = requestFilterChain.apply(request);
        if (chain.isInterrupt()) {
            return new ServerResponse(chain.getHttpStatus(), chain.getData(), null, null);
        }
        return responder.apply(request);
    }
}
