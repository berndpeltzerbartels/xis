package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.UserContextImpl;
import one.xis.auth.AuthenticationException;
import one.xis.auth.URLForbiddenException;
import one.xis.auth.token.TokenStatus;
import one.xis.auth.token.UserSecurityService;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.http.RequestContext;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import org.tinylog.Logger;

import java.time.ZoneId;
import java.util.Collection;
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
    private final UserSecurityService userSecurityService;
    private final Collection<RequestFilter> requestFilters;
    private Resource appJsResource;
    private Resource classesJsResource;
    private Resource mainJsResource;
    private Resource functionsJsResource;
    private Resource bundleJsResource;
    private Resource bundleJsMapResource;

    @XISInit
    void init() {
        appJsResource = resources.getByPath("app.js");
        classesJsResource = resources.getByPath("classes.js");
        mainJsResource = resources.getByPath("main.js");
        functionsJsResource = resources.getByPath("functions.js");
        bundleJsResource = resources.getByPath("bundle.min.js");
        bundleJsMapResource = resources.getByPath("bundle.min.js.map");
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
    public Resource getPageHead(String id) {
        var head = resourceService.getPageHead(id);
        Logger.debug(head);
        return head;
    }

    @Override
    public Resource getPageBody(String id) {
        return resourceService.getPageBody(id);
    }

    @Override
    public Resource getBodyAttributes(String id) {
        return resourceService.getBodyAttributes(id);
    }

    @Override
    public Resource getWidgetHtml(String id) {
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

    @Override
    public String getBundleJsMap() {
        return bundleJsMapResource.getContent();
    }

    private void addRequestAttributes(ClientRequest request) throws AuthenticationException {
        var tokenStatus = new TokenStatus(request.getAccessToken(), request.getRenewToken());
        var userContext = new UserContextImpl();
        userContext.setClientId(request.getClientId());
        userContext.setLocale(request.getLocale());
        userContext.setZoneId(ZoneId.of(request.getZoneId()));
        userContext.setSecurityAttributes(new SecurityAttributesImpl(tokenStatus, userSecurityService));
        RequestContext.getInstance().setAttribute(UserContext.CONTEXT_KEY, userContext);
        RequestContext.getInstance().setAttribute(TokenStatus.CONTEXT_KEY, tokenStatus);
    }

    private ServerResponse applyFilterChain(ClientRequest request, BiConsumer<ClientRequest, ServerResponse> requestHandler) {
        var response = new ServerResponse();
        var filterChain = new RequestFilterChain(requestHandler, requestFilters);
        filterChain.doFilter(request, response, filterChain);
        response = filterChain.getServerResponse();
        try {
            requestHandler.accept(request, response);
        } catch (AuthenticationException e) {
            throw new URLForbiddenException(request.getPageUrl());
        }
        return response;
    }

}
