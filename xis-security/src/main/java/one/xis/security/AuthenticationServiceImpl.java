package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.auth.token.ApiTokensAndUrl;
import one.xis.auth.token.StateParameter;
import one.xis.context.XISDefaultComponent;
import one.xis.context.XISInit;
import one.xis.ipdclient.IDPClient;
import one.xis.ipdclient.IDPClientConfigImpl;
import one.xis.ipdclient.IDPClientFactory;
import one.xis.ipdclient.IDPClientService;
import one.xis.server.LocalUrlHolder;
import one.xis.utils.http.HttpUtils;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@XISDefaultComponent
@RequiredArgsConstructor
class AuthenticationServiceImpl implements AuthenticationService {

    private final LocalUrlHolder localUrlHolder;
    private final IDPClientService idpClientService;
    private final IDPClientFactory idpClientFactory;
    private final AuthenticationConfig authenticationConfig;
    private IDPClient idpClient;

    @XISInit
    void init() {
        var idpClientConfig = new IDPClientConfigImpl();
        idpClientConfig.setClientSecret(authenticationConfig.getClientSecret());
        idpClientConfig.setClientId(authenticationConfig.getClientId());
        idpClientConfig.setIdpServerUrl(authenticationConfig.getIdpUrl());
        idpClientConfig.setIdpId("idp-local");
        idpClient = idpClientFactory.createConfiguredIDPClient(idpClientConfig);
    }

    @Override
    public String loginUrl(String redirectUri) {
        return idpClient.getOpenIdConfig().getAuthorizationEndpoint()
                + "?response_type=code"
                + "&client_id=" + authenticationConfig.getClientId()
                + "&redirect_uri=" + encode(redirectUri, UTF_8)
                + "&scope=openid"
                + "&state=" + StateParameter.create(redirectUri);
    }

    @Override // TODO: duplicated in IDPClientServiceImpl
    public ApiTokensAndUrl authenticationCallback(String query) {
        var parameters = HttpUtils.parseQueryParameters(query);
        var state = parameters.get("state");
        var code = parameters.get("code");
        var payload = StateParameter.decodeAndVerify(state);
        var tokens = idpClient.fetchNewTokens(code);
        var authenticationData = new ApiTokensAndUrl();
        authenticationData.setApiTokens(tokens);
        authenticationData.setUrl(payload.getRedirect());
        return authenticationData;
    }
}
