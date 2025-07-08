package one.xis.security;

import one.xis.auth.token.ApiTokensAndUrl;
import one.xis.auth.token.StateParameter;
import one.xis.ipdclient.IDPClient;
import one.xis.ipdclient.IDPClientConfigImpl;
import one.xis.ipdclient.IDPClientFactory;
import one.xis.server.LocalUrlHolder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static one.xis.server.FrontendService.AUTHENTICATION_PATH;


class AuthenticationServiceImpl implements AuthenticationService {

    private final IDPClientFactory idpClientFactory;
    private final AuthenticationConfig authenticationConfig;
    private final LocalUrlHolder localUrlHolder;
    private IDPClient idpClient;

    AuthenticationServiceImpl(IDPClientFactory idpClientFactory, AuthenticationConfig authenticationConfig, LocalUrlHolder localUrlHolder) {
        this.idpClientFactory = idpClientFactory;
        this.authenticationConfig = authenticationConfig;
        this.localUrlHolder = localUrlHolder;
    }

    private IDPClient createIdpClient() {
        var idpClientConfig = new IDPClientConfigImpl();
        idpClientConfig.setClientSecret(authenticationConfig.getClientSecret());
        idpClientConfig.setClientId(authenticationConfig.getClientId());
        idpClientConfig.setIdpServerUrl(authenticationConfig.getIdpUrl());
        idpClientConfig.setIdpId("idp-local");
        return idpClientFactory.createConfiguredIDPClient(idpClientConfig);
    }

    @Override
    public String loginUrl(String redirectUri) {
        return getIdpClient().getOpenIdConfig().getAuthorizationEndpoint()
                + "?response_type=code"
                + "&client_id=" + authenticationConfig.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(localUrlHolder.getUrl() + AUTHENTICATION_PATH, StandardCharsets.UTF_8)
                + "&scope=openid"
                + "&state=" + StateParameter.create(redirectUri);
    }

    @Override
    public ApiTokensAndUrl authenticate(String code, String state) {
        var payload = StateParameter.decodeAndVerify(state);
        var tokens = getIdpClient().fetchNewTokens(code);
        return new ApiTokensAndUrl(tokens, payload.getRedirect());
    }

    private synchronized IDPClient getIdpClient() {
        if (idpClient == null) {
            idpClient = createIdpClient();
        }
        return idpClient;
    }
}
