package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.auth.token.ApiTokensAndUrl;
import one.xis.auth.token.StateParameter;
import one.xis.ipdclient.IDPClient;
import one.xis.ipdclient.IDPClientConfigImpl;
import one.xis.ipdclient.IDPClientFactory;
import one.xis.server.LocalUrlHolder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static one.xis.server.FrontendService.AUTHENTICATION_PATH;

@RequiredArgsConstructor
class AuthenticationServiceImpl implements AuthenticationService {

    private final IDPClientFactory idpClientFactory;
    private final AuthenticationConfig authenticationConfig;
    private final LocalUrlHolder localUrlHolder;
    private IDPClient idpClient;


    @Override
    public String loginUrl(String redirectUri) {
        var idpClient = getIdpClient(); // must be first !
        var callbackUrl = (localUrlHolder.getUrl() + AUTHENTICATION_PATH).replace("{idpId}", idpClient.getIdpId());
        return idpClient.getOpenIdConfig().getAuthorizationEndpoint()
                + "?response_type=code"
                + "&client_id=" + authenticationConfig.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8)
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
    
    private IDPClient createIdpClient() {
        var idpClientConfig = new IDPClientConfigImpl();
        idpClientConfig.setClientSecret(authenticationConfig.getClientSecret());
        idpClientConfig.setClientId(authenticationConfig.getClientId());
        idpClientConfig.setIdpServerUrl(authenticationConfig.getIdpUrl());
        idpClientConfig.setIdpId("local-idp"); // TODO constant
        return idpClientFactory.createConfiguredIDPClient(idpClientConfig);
    }
}
