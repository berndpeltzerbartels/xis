package one.xis.auth.idp;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.auth.ipdclient.IDPClient;
import one.xis.auth.token.StateParameter;
import one.xis.auth.token.StateParameterPayload;
import one.xis.security.SecurityUtil;
import one.xis.server.LocalUrlHolder;
import one.xis.utils.http.HttpUtils;
import one.xis.utils.lang.StringUtils;

import java.net.HttpURLConnection;
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
        StringBuilder urlBuilder = new StringBuilder(idpClient.getOpenIdConfig().getAuthorizationEndpoint())
                .append("?response_type=code")
                .append("&redirect_uri=").append(URLEncoder.encode(getAuthenticationCallbackUrl(localUrlHolder.getUrl()), StandardCharsets.UTF_8))
                .append("&state=").append(stateParameter)
                .append("&nonce=").append(SecurityUtil.createRandomKey(32))
                .append("&client_id=").append(providerConfiguration.getClientId());
        if (StringUtils.isNotEmpty(providerConfiguration.getClientSecret())) {
            urlBuilder.append("&client_secret=").append(providerConfiguration.getClientSecret());
        }
        return urlBuilder.toString();
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
    public String createStateParameter(String urlAfterLogin) {
        return StateParameter.create(urlAfterLogin, providerConfiguration.getIdpId());
    }

    @Override
    public String getProviderId() {
        return providerConfiguration.getIdpId();
    }

    private String getAuthenticationCallbackUrl(String localUrl) {
        return localUrl + "/xis/auth/callback/" + providerConfiguration.getIdpId();
    }

    private String readErrorStream(HttpURLConnection connection) {
        try {
            return new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Failed to read error stream: " + e.getMessage();
        }
    }


}
