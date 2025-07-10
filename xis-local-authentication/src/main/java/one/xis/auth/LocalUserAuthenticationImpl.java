package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.auth.token.ApiTokensAndUrl;
import one.xis.auth.token.StateParameter;
import one.xis.context.XISDefaultComponent;
import one.xis.ipdclient.IDPClient;
import one.xis.ipdclient.IDPClientConfigImpl;
import one.xis.ipdclient.IDPClientFactory;
import one.xis.security.SecurityUtil;
import one.xis.server.LocalUrlHolder;

@XISDefaultComponent
@RequiredArgsConstructor
class LocalUserAuthenticationImpl implements LocalAuthenticationService {

    private final IDPClientFactory idpClientFactory;
    private final LocalUrlHolder localUrlHolder;
    private IDPClient idpClient;
    private final String secret = SecurityUtil.createRandomKey(32);

    @Override
    public String loginUrl(String redirectUrl) {
        return "";
    }

    @Override
    public ApiTokensAndUrl authenticationCallback(String code, String state) {
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
        idpClientConfig.setClientId("local-idp-client");
        idpClientConfig.setClientSecret(secret);
        idpClientConfig.setIdpId("local-idp");
        idpClientConfig.setIdpServerUrl(localUrlHolder);
        return idpClientFactory.createConfiguredIDPClient(idpClientConfig);
    }
}
