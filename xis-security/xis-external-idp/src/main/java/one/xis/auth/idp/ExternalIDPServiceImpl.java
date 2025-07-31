package one.xis.auth.idp;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.auth.JsonWebKey;
import one.xis.auth.StateParameter;
import one.xis.auth.StateParameterPayload;
import one.xis.auth.ipdclient.IDPClient;
import one.xis.security.SecurityUtil;
import one.xis.server.LocalUrlHolder;
import one.xis.utils.http.HttpUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@RequiredArgsConstructor
class ExternalIDPServiceImpl implements ExternalIDPService {

    private final IDPClient idpClient;
    private final ExternalIDPConfig providerConfiguration;
    private final LocalUrlHolder localUrlHolder;

    @Override
    public String createLoginUrl(String postLoginRedirectUrl) {
        String stateParameter = createStateParameter(postLoginRedirectUrl);
        return idpClient.getOpenIdConfig().getAuthorizationEndpoint() +
                "?response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(getAuthenticationCallbackUrl(localUrlHolder.getUrl()), StandardCharsets.UTF_8) +
                "&state=" + stateParameter +
                "&nonce=" + SecurityUtil.createRandomKey(32) +
                "&client_id=" + providerConfiguration.getClientId();
    }


    @Override
    public ExternalIDPStateData verifyAndDecodeCodeAndStateQuery(@NonNull String queryString) {
        Map<String, String> queryParams = HttpUtils.parseQueryParameters(queryString);
        String state = queryParams.get("state");
        String code = queryParams.get("code");
        if (state == null || state.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty 'state' parameter in the query string");
        }
        String[] parts = state.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid state parameter format");
        }
        StateParameterPayload stateParameterPayload = StateParameter.decodeAndVerify(state);
        return new ExternalIDPStateData(code, state, stateParameterPayload);
    }

    @Override
    public StateParameterPayload verifyState(@NonNull String state) {
        return StateParameter.decodeAndVerify(state);
    }

    @Override
    public ExternalIDPTokens fetchTokens(@NonNull String code) {
        var apiTokens = idpClient.fetchNewTokens(code);
        var externalTokens = new ExternalIDPTokens();
        externalTokens.setAccessToken(apiTokens.getAccessToken());
        externalTokens.setRefreshToken(apiTokens.getRenewToken());
        externalTokens.setExpiresInSeconds(apiTokens.getAccessTokenExpiresIn().getSeconds());
        externalTokens.setRefreshExpiresInSeconds(apiTokens.getRenewTokenExpiresIn().getSeconds());
        return externalTokens;
    }

    @Override
    public ExternalIDPTokens fetchRenewedTokens(@NonNull String refreshToken) {
        var apiTokens = idpClient.fetchRenewedTokens(refreshToken);
        var externalTokens = new ExternalIDPTokens();
        externalTokens.setAccessToken(apiTokens.getAccessToken());
        externalTokens.setRefreshToken(apiTokens.getRenewToken());
        externalTokens.setExpiresInSeconds(apiTokens.getAccessTokenExpiresIn().getSeconds());
        externalTokens.setRefreshExpiresInSeconds(apiTokens.getRenewTokenExpiresIn().getSeconds());
        return externalTokens;
    }

    @Override
    public String createStateParameter(String urlAfterLogin) {
        return StateParameter.create(urlAfterLogin, getIssuer());
    }

    @Override
    public String getProviderId() {
        return providerConfiguration.getIdpId();
    }

    @Override
    public String getIssuer() {
        return idpClient.getOpenIdConfig().getIssuer();
    }

    @Override
    public JsonWebKey getJsonWebKey(String kid) {
        return idpClient.getPublicKeys().stream()
                .filter(key -> key.getKeyId().equals(kid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No JSON Web Key found with kid: " + kid));
    }

    private String getAuthenticationCallbackUrl(String localUrl) {
        return localUrl + "/xis/auth/callback/" + providerConfiguration.getIdpId();
    }

}
