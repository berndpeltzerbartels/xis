package one.xis.auth.ipdclient;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.auth.*;
import one.xis.http.client.HttpClientException;
import one.xis.http.client.RestClient;
import one.xis.idp.IDPResponse;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
class IDPClientImpl implements IDPClient {

    @Getter
    private final RestClient restClient;
    private final IDPClientConfig idpClientConfig;
    private final String redirectUri;
    private final Gson gson;
    @Getter
    private Collection<JsonWebKey> publicKeys;
    @Getter
    private IDPWellKnownOpenIdConfig openIdConfig;

    @Override
    public IDPResponse fetchNewTokens(@NonNull String code) throws AuthenticationException {
        try {
            String formBody = "grant_type=authorization_code" +
                    "&code=" + encode(code, UTF_8) +
                    "&redirect_uri=" + encode(redirectUri, UTF_8) +
                    "&client_id=" + encode(idpClientConfig.getClientId(), UTF_8) + // TODO scope ? Not required by OpenID Connect, but might be useful for IDP-specific scopes.
                    "&client_secret=" + encode(idpClientConfig.getClientSecret(), UTF_8);

            var httpClient = restClient.getHttpClient();
            var headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            headers.put("Accept", "application/json");

            var response = httpClient.doPost(getOpenIdConfig().getTokenEndpoint(), formBody, headers);

            if (response.getStatusCode() != 200) {
                throw new AuthenticationException("Failed to request tokens from IDP. Status: " + response.getStatusCode() + ", Body: " + response.getContent());
            }
            return IDPResponse.fromOAuth2Json(response.getContent());
        } catch (HttpClientException e) {
            throw new AuthenticationException("Failed to request tokens from IDP", e);
        }
    }

    @Override
    public ApiTokens fetchRenewedTokens(@NonNull String refreshToken) throws AuthenticationException {
        try {
            String formBody = "grant_type=refresh_token" +
                    "&refresh_token=" + encode(refreshToken, UTF_8) +
                    "&client_id=" + encode(idpClientConfig.getClientId(), UTF_8) +
                    "&client_secret=" + encode(idpClientConfig.getClientSecret(), UTF_8);

            var httpClient = restClient.getHttpClient();
            var headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            headers.put("Accept", "application/json");

            var response = httpClient.doPost(getOpenIdConfig().getTokenEndpoint(), formBody, headers);

            if (response.getStatusCode() != 200) {
                throw new AuthenticationException("Failed to renew tokens from IDP. Status: " + response.getStatusCode() + ", Body: " + response.getContent());
            }
            return gson.fromJson(response.getContent(), ApiTokens.class);
        } catch (HttpClientException e) {
            throw new AuthenticationException("Failed to renew tokens from IDP", e);
        }
    }

    @Override
    public UserInfoImpl fetchUserInfo(@NonNull String accessToken) throws AuthenticationException {
        try {
            var httpClient = restClient.getHttpClient();
            var headers = new HashMap<String, String>();
            headers.put("Authorization", "Bearer " + accessToken);
            headers.put("Accept", "application/json");

            var response = httpClient.doGet(getOpenIdConfig().getUserInfoEndpoint(), headers);

            if (response.getStatusCode() != 200) {
                throw new AuthenticationException("Failed to fetch user info from IDP. Status: " + response.getStatusCode() + ", Body: " + response.getContent());
            }

            return gson.fromJson(response.getContent(), UserInfoImpl.class);
        } catch (HttpClientException e) {
            throw new AuthenticationException("Failed to fetch user info from IDP", e);
        }
    }

    @Override
    public String getIdpId() {
        return idpClientConfig.getIdpId();
    }


    @Override
    public String getAuthorizationEndpoint() {
        return getOpenIdConfig().getAuthorizationEndpoint();
    }

    @Override
    public String getIssuer() {
        return getOpenIdConfig().getIssuer();
    }


    @Override
    public void loadOpenIdConfig() throws Exception {
        openIdConfig = restClient.get(idpClientConfig.getIdpServerUrl() + "/.well-known/openid-configuration", IDPWellKnownOpenIdConfig.class);
    }

    @Override
    public void loadPublicKeys() throws AuthenticationException {
        this.publicKeys = fetchPublicKeys().getKeys();
    }

    private IDPPublicKeyResponse fetchPublicKeys() throws AuthenticationException {
        try {
            var httpClient = restClient.getHttpClient();
            var headers = new HashMap<String, String>();
            headers.put("Accept", "application/json");

            var response = httpClient.doGet(getOpenIdConfig().getJwksUri(), headers);

            if (response.getStatusCode() != 200) {
                throw new AuthenticationException("Failed to fetch public keys (JWKS) from IDP. Status: " + response.getStatusCode() + ", Body: " + response.getContent());
            }

            return new IDPPublicKeyResponse(Arrays.asList(gson.fromJson(response.getContent(), JsonWebKey[].class)));
        } catch (HttpClientException e) {
            throw new AuthenticationException("Failed to fetch public keys (JWKS) from IDP", e);
        }
    }

}
