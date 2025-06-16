package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.UserContextAccess;
import one.xis.UserContextImpl;
import one.xis.context.AppContext;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import one.xis.security.*;
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
    private final ApiTokenManager tokenManager;
    private final AppContext appContext;
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
            return authenticationErrorResponse(request.getPageUrl());
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
            return authenticationErrorResponse(request.getPageId());
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
            return authenticationErrorResponse(request.getPageId());
        } finally {
            removeUserContext();
        }
    }

    @Override
    public ApiTokens processRenewApiTokenRequest(String renewToken) {
        try {
            var result = tokenManager.renew(renewToken);
            return new ApiTokens(result.accessToken(),
                    result.accessTokenExpiresIn(),
                    result.renewToken(),
                    result.renewTokenExpiresIn());
        } catch (InvalidTokenException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthenticationData authenticationCallback(String provider, String queryString) {
        var service = Objects.requireNonNull(authenticationProviderServices.getAuthenticationProviderService(provider));
        var authenticationProviderData = service.verifyAndDecodeCodeAndStateQuery(queryString);
        var tokenResponse = service.requestTokens(authenticationProviderData.getCode(), authenticationProviderData.getState());
        return getAuthenticationData(tokenResponse, authenticationProviderData);
    }
    

    @Override
    public BearerTokens localTokenProviderGetTokens(String code, String state) throws AuthenticationException {
        var authenticationProviderService = authenticationProviderService();
        var tokenResponse = authenticationProviderService.issueToken(code, state);
        var bearerTokens = new BearerTokens();
        bearerTokens.setAccessToken(tokenResponse.getAccessToken());
        bearerTokens.setAccessTokenExpiresIn(tokenResponse.getExpiresIn());
        bearerTokens.setRenewToken(tokenResponse.getRefreshToken());
        bearerTokens.setRenewTokenExpiresIn(tokenResponse.getRefreshTokenExpiresIn());
        return bearerTokens;
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
    public ServerResponse processLoginRequest(ClientRequest request) {
        return null; // TODO Status ist 201 - Created, wenn Login erfolgreich ist
    }

    private void addUserContext(ClientRequest request) {
        var userContext = new UserContextImpl();
        userContext.setClientId(request.getClientId());
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


    private IDPService authenticationProviderService() {
        return appContext.getOptionalSingleton(IDPService.class)
                .orElseThrow(() -> new UnsupportedOperationException("Local authentication is not activated"));
    }


    private static AuthenticationData getAuthenticationData(AuthenticationProviderTokens tokenResponse, AuthenticationProviderStateData authenticationProviderData) {
        var apiTokens = new ApiTokens();
        apiTokens.setAccessToken(tokenResponse.getAccessToken());
        apiTokens.setRenewTokenExpiresIn(tokenResponse.getRefreshExpiresIn());
        apiTokens.setRenewToken(tokenResponse.getRefreshToken());
        apiTokens.setRenewTokenExpiresIn(tokenResponse.getRefreshExpiresIn());
        var authenticationData = new AuthenticationData();
        authenticationData.setApiTokens(apiTokens);
        authenticationData.setUrl(authenticationProviderData.getStateParameterPayload().getRedirect());
        return authenticationData;
    }

    private ServerResponse authenticationErrorResponse(String uri) {
        var state = StateParameter.create(uri);
        var response = new ServerResponse();
        response.setStatus(401);
        response.setNextURL("/login.html?state=" + state);
        response.getValidatorMessages().getMessages().put("username", "Invalid username or password"); // TODO: i18n
        response.getValidatorMessages().getMessages().put("password", "Invalid username or password"); // TODO: i18n
        response.getValidatorMessages().getGlobalMessages().add("Invalid username or password"); // TODO: i18n
        return response;
    }
}
