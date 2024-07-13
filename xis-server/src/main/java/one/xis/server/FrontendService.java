package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.UserContextAccess;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import org.tinylog.Logger;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Encapsulates all methods, required by the framework's controller.
 */
@XISComponent
@RequiredArgsConstructor
public class FrontendService {

    private final ControllerService controllerService;
    private final ClientConfigService configService;
    private final HtmlResourceService htmlResourceService;
    private final Resources resources;
    private final Collection<RequestFilter> requestFilters;
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

    public ClientConfig getConfig() {
        return configService.getConfig();
    }

    public ServerResponse processActionRequest(ClientRequest request) {
        try {
            addUserContext(request);
            return applyFilterChain(request, controllerService::processActionRequest);
        } finally {
            removeUserContext();
        }
    }


    public ServerResponse processModelDataRequest(ClientRequest request) {
        try {
            addUserContext(request);
            return applyFilterChain(request, controllerService::processModelDataRequest);
        } finally {
            removeUserContext();
        }
    }


    public String getPage(String id) {
        return htmlResourceService.getPage(id);
    }

    public String getPageHead(String id) {
        var head = htmlResourceService.getPageHead(id);
        Logger.debug(head);
        return head;
    }

    public String getPageBody(String id) {
        var body = htmlResourceService.getPageBody(id);
        Logger.debug(body);
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

    private void addUserContext(ClientRequest request) {
        var userContext = new UserContext();
        userContext.setClientId(request.getClientId());
        userContext.setUserId(request.getUserId());
        userContext.setLocale(request.getLocale());
        userContext.setZoneId(ZoneId.of(request.getZoneId()));
        UserContextAccess.setInstance(userContext);
    }

    private void removeUserContext() {
        UserContextAccess.removeInstance();
    }

    private ServerResponse applyFilterChain(ClientRequest request, BiConsumer<ClientRequest, ServerResponse> requestHandler) {
        var response = new ServerResponse();
        var filterChain = new RequestFilterChain(requestHandler, requestFilters);
        filterChain.doFilter(request, response, filterChain);
        response = filterChain.getServerResponse();
        requestHandler.accept(request, response);
        return response;
    }

}
