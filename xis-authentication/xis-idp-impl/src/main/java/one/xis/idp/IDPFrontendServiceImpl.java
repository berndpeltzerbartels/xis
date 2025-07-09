package one.xis.idp;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import one.xis.auth.AuthenticationException;
import one.xis.auth.IDPWellKnownOpenIdConfig;
import one.xis.auth.JsonWebKey;
import one.xis.auth.token.ApiTokensAndUrl;
import one.xis.auth.token.StateParameter;
import one.xis.auth.token.TokenService;
import one.xis.context.XISComponent;
import one.xis.ipdclient.IDPClientService;
import one.xis.server.LocalUrlHolder;

import java.util.Map;

import static one.xis.utils.http.HttpUtils.parseQueryParameters;


@XISComponent
@RequiredArgsConstructor
class IDPFrontendServiceImpl implements IDPFrontendService {

    private final IDPClientService idpClientService;
    private final IDPService idpService;
    private final LocalUrlHolder localUrlHolder;
    private final TokenService tokenService;
    private final IDPAuthenticationService idpAuthenticationService;
    private final Gson gson = new Gson();

    @Override
    public ApiTokensAndUrl authenticationCallback(String provider, String queryString) {
        Map<String, String> queryParams = parseQueryParameters(queryString);
        String state = queryParams.get("state");
        String code = queryParams.get("code");
        if (state == null || state.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty 'state' parameter in the query string");
        }
        var stateParameterPayload = StateParameter.decodeAndVerify(state);
        // Local authentication is handled by a controller, instead of the IDP service.
        var tokens = idpClientService.fetchNewTokens(provider, code, state);
        var authenticationData = new ApiTokensAndUrl();
        authenticationData.setApiTokens(tokens);
        authenticationData.setUrl(stateParameterPayload.getRedirect());
        return authenticationData;
    }

    @Override
    public String createLoginFormUrl(String provider, String redirectUri) {
        return idpClientService.loginFormUrl(provider, redirectUri);
    }

    @Override
    public String getOpenIdConfigJson() {
        var config = new IDPWellKnownOpenIdConfig();
        config.setIssuer(localUrlHolder.getUrl());
        config.setJwksUri(localUrlHolder.getUrl() + "/.well-known/jwks.json"); // TODO create constants for these URLs
        config.setAuthorizationEndpoint(localUrlHolder.getUrl() + XisIDPConfig.IDP_LOGIN_URL);
        config.setTokenEndpoint(localUrlHolder.getUrl() + "/xis/auth/tokens");
        config.setUserInfoEndpoint(localUrlHolder.getUrl() + "/xis/auth/userinfo");
        return gson.toJson(config);
    }

    @Override
    public JsonWebKey getPublicKey() {
        return tokenService.getPublicJsonWebKey();
    }

    @Override
    public IDPResponse provideTokens(String tokenRequestPayload) throws AuthenticationException {
        var parameters = parseQueryParameters(tokenRequestPayload);
        var request = gson.fromJson(gson.toJson(parameters), IDPTokenRequest.class);
        if (!request.getRedirectUri().startsWith("http")) {
            throw new AuthenticationException("Invalid redirect URI: " + request.getRedirectUri() + ". It must start with 'http(s)'.");
        }
        var clientInfo = idpService.findClientInfo(request.getClientId()).orElseThrow(() -> new AuthenticationException("Client not found: " + request.getClientId()));
        if (!clientInfo.getClientSecret().equals(request.getClientSecret())) {
            throw new AuthenticationException("Invalid client secret for client: " + request.getClientId());
        }
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new AuthenticationException("Missing or empty 'code' parameter in the request");
        }
        if (!clientInfo.getPermittedRedirectUrls().contains(request.getRedirectUri())) {
            throw new AuthenticationException("Invalid redirect URI: " + request.getRedirectUri());
        }
        return new IDPResponse(idpAuthenticationService.issueToken(request.getCode()));
    }

}
