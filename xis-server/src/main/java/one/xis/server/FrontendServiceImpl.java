package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.UserContextImpl;
import one.xis.auth.AuthenticationException;
import one.xis.auth.token.TokenManager;
import one.xis.auth.token.TokenStatus;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.http.RequestContext;
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
public class FrontendServiceImpl implements FrontendService {

    private final ControllerService controllerService;
    private final ClientConfigService configService;
    private final ResourceService resourceService;
    private final Resources resources;
    private final TokenManager tokenManager;
    private final Collection<RequestFilter> requestFilters;
    private Resource appJsResource;
    private Resource classesJsResource;
    private Resource mainJsResource;
    private Resource functionsJsResource;
    private Resource bundleJsResource;

    @XISInit
    void init() {
        appJsResource = resources.getByPath("app.js");
        classesJsResource = resources.getByPath("classes.js");
        mainJsResource = resources.getByPath("main.js");
        functionsJsResource = resources.getByPath("functions.js");
        bundleJsResource = resources.getByPath("bundle.min.js");
    }

    @Override
    public ClientConfig getConfig() {
        return configService.getConfig();
    }

    @Override
    public ServerResponse processActionRequest(ClientRequest request) {
        addRequestAttributes(request);
        return applyFilterChain(request, controllerService::processActionRequest);
    }

    @Override
    public ServerResponse processModelDataRequest(ClientRequest request) {
        addRequestAttributes(request);
        return applyFilterChain(request, controllerService::processModelDataRequest);
    }

    @Override
    public ServerResponse processFormDataRequest(ClientRequest request) {
        addRequestAttributes(request);
        return applyFilterChain(request, controllerService::processFormDataRequest);
    }

    @Override
    public String getPageHead(String id) {
        var head = resourceService.getPageHead(id);
        Logger.debug(head);
        return head;
    }

    @Override
    public String getPageBody(String id) {
        return resourceService.getPageBody(id);
    }

    @Override
    public Map<String, String> getBodyAttributes(String id) {
        return resourceService.getBodyAttributes(id);
    }

    @Override
    public String getWidgetHtml(String id) {
        return resourceService.getWidgetHtml(id);
    }

    @Override
    public String getRootPageHtml() {
        return resourceService.getRootPageHtml();
    }


    @Override
    public String getAppJs() {
        return appJsResource.getContent();
    }

    @Override
    public String getClassesJs() {
        return classesJsResource.getContent();
    }

    @Override
    public String getMainJs() {
        return mainJsResource.getContent();
    }

    @Override
    public String getFunctionsJs() {
        return functionsJsResource.getContent();
    }


    @Override
    public String getBundleJs() {
        return bundleJsResource.getContent();
    }

    private void addRequestAttributes(ClientRequest request) throws AuthenticationException {
        var userContext = new UserContextImpl();
        userContext.setClientId(request.getClientId());
        userContext.setLocale(request.getLocale());
        userContext.setZoneId(ZoneId.of(request.getZoneId()));
        var tokenStatus = new TokenStatus(request.getAccessToken(), request.getRenewToken());
        tokenManager.updateUserContext(tokenStatus, userContext);
        RequestContext.getInstance().setAttribute(UserContext.CONTEXT_KEY, userContext);
        RequestContext.getInstance().setAttribute(TokenStatus.CONTEXT_KEY, tokenStatus);
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
