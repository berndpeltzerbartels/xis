package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.UserContextAccess;
import one.xis.UserContextImpl;
import one.xis.auth.AuthenticationException;
import one.xis.auth.token.AccessTokenCache;
import one.xis.auth.token.AccessTokenWrapper;
import one.xis.auth.token.ApiTokensAndUrl;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import one.xis.security.AuthenticationService;
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
    private final AccessTokenCache accessTokenCache;

    @XISInject(optional = true)
    private AuthenticationService authenticationService;
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
        try {
            addUserContext(request);
            return applyFilterChain(request, controllerService::processActionRequest);
        } catch (AuthenticationException e) {
            Logger.error("Authentication error: {}", e.getMessage());
            return authenticationErrorResponse(request);
        } finally {
            removeUserContext();
        }
    }

    @Override
    public ServerResponse processModelDataRequest(ClientRequest request) {
        try {
            addUserContext(request);
            return applyFilterChain(request, controllerService::processModelDataRequest);
        } catch (AuthenticationException e) {
            Logger.error("Authentication error: {}", e.getMessage());
            return authenticationErrorResponse(request);
        } finally {
            removeUserContext();
        }
    }

    @Override
    public ServerResponse processFormDataRequest(ClientRequest request) {
        try {
            addUserContext(request);
            return applyFilterChain(request, controllerService::processFormDataRequest);
        } catch (AuthenticationException e) {
            Logger.error("Authentication error: {}", e.getMessage());
            return authenticationErrorResponse(request);
        } finally {
            removeUserContext();
        }
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

    @Override
    public ApiTokensAndUrl authenticationCallback(String code, String state) {
        return authenticationService.authenticate(code, state);
    }

    private void addUserContext(ClientRequest request) throws AuthenticationException {
        var userContext = new UserContextImpl();
        userContext.setClientId(request.getClientId());
        userContext.setLocale(request.getLocale());
        userContext.setZoneId(ZoneId.of(request.getZoneId()));
        userContext.setAccessToken(new AccessTokenWrapper(request.getAccessToken(), accessTokenCache));
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


    private ServerResponse authenticationErrorResponse(ClientRequest request) {
        var response = new ServerResponse();
        response.setStatus(412);
        response.setRedirectUrl(authenticationService.loginUrl(request.getPageUrl()));
        return response;
    }
}
