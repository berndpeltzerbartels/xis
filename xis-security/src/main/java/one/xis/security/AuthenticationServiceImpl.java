package one.xis.security;

import com.google.gson.Gson;
import lombok.NonNull;
import one.xis.utils.lang.StringUtils;

import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static one.xis.utils.lang.StringUtils.isNotEmpty;

class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationProviderConnectionFactory connectionFactory;
    private final AuthenticationProviderConfig providerConfiguration;
    private final Gson gson = new Gson();

    AuthenticationServiceImpl(AuthenticationProviderConfig providerConfiguration,
                              AuthenticationProviderConnectionFactory connectionFactory) {
        this.providerConfiguration = providerConfiguration;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public String createAuthorizationUrl() {
        return createLoginUrl(providerConfiguration.getApplicationRootEndpoint());
    }

    @Override
    public String createLoginUrl(String providerLoginFormUrl) {
        String stateParameter = createStateParameter(providerLoginFormUrl);
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
    public AuthenticationProviderStateData verifyAndDecodeCodeAndStateQuery(@NonNull String queryString) {
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
        return new AuthenticationProviderStateData(code, state, stateParameterPayload);
    }

    @Override
    public StateParameterPayload verifyState(@NonNull String state) {
        return StateParameter.decodeAndVerify(state);
    }

    @Override
    public AuthenticationProviderTokens requestTokens(@NonNull String code, @NonNull String state) {
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
            return gson.fromJson(responseBody, AuthenticationProviderTokens.class);
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
        return providerConfiguration.getAuthenticationProviderId();
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

    private Map<String, String> parseQueryParameters(@NonNull String url) {
        int queryStart = url.indexOf('?');
        String query = url.substring(queryStart + 1);
        String[] pairs = query.split("&");
        Map<String, String> params = new java.util.HashMap<>();
        for (String pair : pairs) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex > 0) {
                String key = pair.substring(0, equalsIndex);
                String value = URLDecoder.decode(pair.substring(equalsIndex + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }


}
