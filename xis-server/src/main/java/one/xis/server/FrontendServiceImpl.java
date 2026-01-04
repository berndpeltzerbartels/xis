package one.xis.server;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.UserContext;
import one.xis.UserContextCreatedEvent;
import one.xis.UserContextImpl;
import one.xis.auth.AuthenticationException;
import one.xis.auth.URLForbiddenException;
import one.xis.auth.token.TokenStatus;
import one.xis.auth.token.UserSecurityService;
import one.xis.context.EventEmitter;
import one.xis.context.Component;
import one.xis.http.RequestContext;
import one.xis.js.JavascriptProvider;
import one.xis.resource.Resource;

import java.time.ZoneId;
import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * Encapsulates all methods, required by the framework's controller.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FrontendServiceImpl implements FrontendService {

    private final ControllerService controllerService;
    private final ClientConfigService configService;
    private final ResourceService resourceService;
    private final JavascriptProvider javascriptProvider;
    private final UserSecurityService userSecurityService;
    private final Collection<RequestFilter> requestFilters;
    private final EventEmitter eventEmitter;

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
        log.debug("Page head: {}", head);
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
    public Resource getIncludeHtml(String key) {
        return resourceService.getIncludeHtml(key);
    }

    @Override
    public String getRootPageHtml() {
        return resourceService.getRootPageHtml();
    }


    @Override
    public Resource getBundleJs() {
        return javascriptProvider.getCompressedJavascript();
    }

    @Override
    public Resource getBundleJsMap() {
        return javascriptProvider.getSourceMap();
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
        eventEmitter.emitEvent(new UserContextCreatedEvent(userContext));
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
