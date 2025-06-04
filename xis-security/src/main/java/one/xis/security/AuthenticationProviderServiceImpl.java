package one.xis.security;

import com.google.gson.Gson;
import lombok.NonNull;
import one.xis.utils.lang.StringUtils;

import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MINUTES;
import static one.xis.utils.lang.StringUtils.isNotEmpty;

class AuthenticationProviderServiceImpl implements AuthenticationProviderService {

    private static final Duration STATE_PARAMETER_EXPIRATION = Duration.of(15, MINUTES);

    private final AuthenticationProviderConnectionFactory connectionFactory;
    private final AuthenticationProviderConfiguration providerConfiguration;
    private final String stateSignatureKey;
    private final Gson gson = new Gson();

    AuthenticationProviderServiceImpl(AuthenticationProviderConfiguration providerConfiguration,
                                      AuthenticationProviderConnectionFactory connectionFactory) {
        this.providerConfiguration = providerConfiguration;
        this.connectionFactory = connectionFactory;
        this.stateSignatureKey = SecurityUtil.createRandomKey(32);
    }

    @Override
    public String createAuthorizationUrl() {
        return createAuthorizationUrl(providerConfiguration.getApplicationRootEndpoint());
    }

    @Override
    public String createAuthorizationUrl(String urlAfterLogin) {
        String stateParameter = createStateParameter(urlAfterLogin);
        StringBuilder urlBuilder = new StringBuilder(providerConfiguration.getAuthorizationEndpoint())
                .append("?response_type=code")
                .append("&redirect_uri=").append(getFullAuthenticationCallbackUrl())
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
    public AuthenticationProviderStateData verifyStateAndExtractCode(@NonNull String queryString) {
        Map<String, String> queryParams = parseQueryParameters(queryString);
        String state = queryParams.get("state");
        String code = queryParams.get("code");
        if (state == null || state.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty 'state' parameter in the query string");
        }
        StateParameterPayload stateParameterPayload = verifyStateParameter(state);
        return new AuthenticationProviderStateData(code, stateParameterPayload);
    }

    @Override
    public AuthenticationProviderTokenResponse requestTokens(@NonNull String code) {
        String url = providerConfiguration.getTokenEndpoint();
        StringBuilder requestBody = new StringBuilder()
                .append("grant_type=authorization_code")
                .append("&code=").append(code)
                .append("&redirect_uri=").append(providerConfiguration.getCallbackUrl());
        if (StringUtils.isNotEmpty(providerConfiguration.getClientId())) {
            requestBody.append("&client_id=").append(providerConfiguration.getClientId());
        }
        if (StringUtils.isNotEmpty(providerConfiguration.getClientSecret())) {
            requestBody.append("&client_secret=").append(providerConfiguration.getClientSecret());
        }

        HttpURLConnection connection = connectionFactory.createPostConnection(url, requestBody.toString());
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String body = readErrorStream(connection);
                throw new RuntimeException("Failed to request tokens: " + responseCode + " - " + body);
            }
            String responseBody = new String(connection.getInputStream().readAllBytes());
            return gson.fromJson(responseBody, AuthenticationProviderTokenResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to request tokens", e);
        } finally {
            connection.disconnect();
        }
    }

    @Override
    public String getProviderId() {
        return providerConfiguration.getAuthenticationProviderId();
    }

    private String getFullAuthenticationCallbackUrl() {
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
        if (queryStart < 0) {
            queryStart = 0;
        }
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

    private StateParameterPayload verifyStateParameter(@NonNull String stateParameter) {
        String[] parts = stateParameter.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid state parameter format");
        }
        String encodedPayload = parts[0];
        String signature = parts[1];
        String expectedSignature = SecurityUtil.signHmacSHA256(encodedPayload, stateSignatureKey);
        if (!expectedSignature.equals(signature)) {
            throw new IllegalArgumentException("Invalid state parameter signature");
        }
        String payloadJson = new String(SecurityUtil.decodeBase64UrlSafe(encodedPayload));
        StateParameterPayload payload;
        try {
            payload = gson.fromJson(payloadJson, StateParameterPayload.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid state parameter payload", e);
        }
        if (payload.getCsrf() == null || payload.getCsrf().isEmpty()) {
            throw new IllegalArgumentException("Missing CSRF token in state parameter");
        }
        if (payload.getRedirect() == null || payload.getRedirect().isEmpty()) {
            throw new IllegalArgumentException("Missing redirect URI in state parameter");
        }
        long iat = payload.getIat();
        long currentTime = System.currentTimeMillis() / 1000;
        if (iat <= 0 || iat > currentTime) {
            throw new IllegalArgumentException("Invalid issued at time in state parameter");
        }
        long expiresAt = payload.getExpiresAtSeconds();
        if (expiresAt <= 0 || expiresAt <= iat || expiresAt < currentTime) {
            throw new IllegalArgumentException("State parameter has expired");
        }
        return payload;
    }


    private String createStateParameter(String urlAfterLogin) {
        StateParameterPayload payload = createStateParameterPayload(urlAfterLogin);
        String payloadJson = gson.toJson(payload);
        String encodedPayload = SecurityUtil.encodeBase64UrlSafe(payloadJson);
        String signature = SecurityUtil.signHmacSHA256(encodedPayload, stateSignatureKey);
        return encodedPayload + "." + signature;
    }

    private StateParameterPayload createStateParameterPayload(String urlAfterLogin) {
        StateParameterPayload payload = new StateParameterPayload();
        payload.setCsrf(SecurityUtil.createRandomKey(32));
        payload.setRedirect(urlAfterLogin);
        payload.setIat(System.currentTimeMillis() / 1000);
        payload.setExpiresAtSeconds(payload.getIat() + STATE_PARAMETER_EXPIRATION.getSeconds());
        return payload;
    }


}
