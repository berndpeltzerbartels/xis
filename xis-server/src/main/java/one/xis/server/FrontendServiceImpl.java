package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.UserContext;
import one.xis.UserContextAccess;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import one.xis.security.AuthenticationProviderServices;
import one.xis.security.InvalidTokenException;
import one.xis.security.TokenManager;
import org.tinylog.Logger;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
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
    private final AuthenticationProviderServices authenticationProviderServices;
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
        try {
            addUserContext(request);
            return applyFilterChain(request, controllerService::processActionRequest);
        } finally {
            removeUserContext();
        }
    }

    @Override
    public ServerResponse processModelDataRequest(ClientRequest request) {
        try {
            addUserContext(request);
            return applyFilterChain(request, controllerService::processModelDataRequest);
        } finally {
            removeUserContext();
        }
    }

    @Override
    public ServerResponse processFormDataRequest(ClientRequest request) {
        try {
            addUserContext(request);
            return applyFilterChain(request, controllerService::processFormDataRequest);
        } finally {
            removeUserContext();
        }
    }

    @Override
    public RenewTokenResponse processRenewTokenRequest(String renewToken) {
        try {
            var result = tokenManager.renew(renewToken);
            return new RenewTokenResponse(result.accessToken(),
                    result.accessTokenExpiresAt().toEpochMilli(),
                    result.renewToken(),
                    result.renewTokenExpiresAt().toEpochMilli());
        } catch (InvalidTokenException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthenticationData authenticationCallback(String provider, String queryString) {
        var service = Objects.requireNonNull(authenticationProviderServices.getAuthenticationProviderService(provider));
        var authenticationProviderData = service.verifyStateAndExtractCode(queryString);
        var tokenResponse = service.requestTokens(authenticationProviderData.getCode());
        var now = System.currentTimeMillis();
        var authenticationData = new AuthenticationData();
        authenticationData.setAccessToken(tokenResponse.getAccessToken());
        authenticationData.setAccessTokenExpiresAt(now + tokenResponse.getExpiresInSeconds() * 1000L);
        authenticationData.setRenewToken(tokenResponse.getRefreshToken());
        authenticationData.setUrl(authenticationProviderData.getStateParameterPayload().getRedirect());
        return authenticationData;
    }


    @Override
    public String getPage(String id) {
        return resourceService.getPage(id);
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
    public String getPageJavascript(String javascriptPath) {
        return resourceService.getJavascript(javascriptPath);
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

    boolean isRunningFromJar() {
        String path = Page.class.getResource(Page.class.getSimpleName() + ".class").toString();
        return path.startsWith("jar:");
    }


}
