package one.xis.idp;

import com.google.gson.Gson;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.auth.token.StateParameter;
import one.xis.auth.token.StateParameterPayload;
import one.xis.security.SecurityUtil;
import one.xis.server.LocalUrlHolder;
import one.xis.utils.lang.StringUtils;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static one.xis.utils.http.HttpUtils.parseQueryParameters;
import static one.xis.utils.lang.StringUtils.isNotEmpty;


@RequiredArgsConstructor
class ExternalIDPServiceImpl implements ExternalIDPService {

    private final ExternalIDPConfig providerConfiguration;
    private final ExternalIDPConnectionFactory connectionFactory;
    private final LocalUrlHolder localUrlHolder;
    private final Gson gson = new Gson();


    @Override
    public String createAuthorizationUrl() {
        return createLoginUrl(localUrlHolder.getUrl());
    }

    @Override
    public String createLoginUrl(String postLoginRedirectUrl) {
        String stateParameter = createStateParameter(postLoginRedirectUrl);
        StringBuilder urlBuilder = new StringBuilder(providerConfiguration.getLoginFormUrl())
                .append("?response_type=code")
                .append("&redirect_uri=").append(getAuthenticationCallbackUrl())
                .append("&state=").append(stateParameter)
                .append("&nonce=").append(SecurityUtil.createRandomKey(32));
        if (isNotEmpty(providerConfiguration.getClientId())) {
            urlBuilder.append("&client_id=").append(providerConfiguration.getClientId());
        }
        if (isNotEmpty(providerConfiguration.getClientSecret())) {
            urlBuilder.append("&client_secret=").append(providerConfiguration.getClientSecret());
        }
        return urlBuilder.toString();
    }


    @Override
    public ExternalIDPStateData verifyAndDecodeCodeAndStateQuery(@NonNull String queryString) {
        Map<String, String> queryParams = parseQueryParameters(queryString);
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
    public ExternalIDPTokens requestTokens(@NonNull String code, @NonNull String state) {
        String url = providerConfiguration.getTokenEndpoint();
        StringBuilder requestBody = new StringBuilder()
                .append("grant_type=authorization_code")
                .append("&code=").append(code)
                .append("&state=").append(state)
                .append("&redirect_uri=").append(providerConfiguration.getCallbackUrl());
        if (StringUtils.isNotEmpty(providerConfiguration.getClientId())) {
            requestBody.append("&client_id=").append(providerConfiguration.getClientId());
        }
        if (StringUtils.isNotEmpty(providerConfiguration.getClientSecret())) {
            requestBody.append("&client_secret=").append(providerConfiguration.getClientSecret());
        }

        HttpURLConnection connection = connectionFactory.createPostConnectionFormUrlEncoded(url, requestBody.toString());
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String body = readErrorStream(connection);
                throw new RuntimeException("Failed to request tokens: " + responseCode + " - " + body);
            }
            String responseBody = new String(connection.getInputStream().readAllBytes());
            return gson.fromJson(responseBody, ExternalIDPTokens.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to request tokens", e);
        } finally {
            connection.disconnect();
        }
    }

    @Override
    public String createStateParameter(String urlAfterLogin) {
        return StateParameter.create(urlAfterLogin);
    }

    @Override
    public String getProviderId() {
        return providerConfiguration.getIdpId();
    }

    private String getAuthenticationCallbackUrl() {
        return providerConfiguration.getCallbackUrl();
    }

    private String readErrorStream(HttpURLConnection connection) {
        try {
            return new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Failed to read error stream: " + e.getMessage();
        }
    }


}
