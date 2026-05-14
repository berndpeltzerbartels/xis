package one.xis.auth.idp;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.auth.JsonWebKey;
import one.xis.auth.StateParameter;
import one.xis.auth.ipdclient.IDPClient;
import one.xis.security.SecurityUtil;
import one.xis.server.LocalUrlHolder;

import java.time.Duration;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


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
                "&client_id=" + providerConfiguration.getClientId() +
                "&scope=" + URLEncoder.encode(providerConfiguration.getScope(), StandardCharsets.UTF_8);
    }

    @Override
    public ExternalIDPTokens fetchTokens(@NonNull String code) {
        var idpResponse = idpClient.fetchNewTokens(code);
        var externalTokens = new ExternalIDPTokens();
        externalTokens.setAccessToken(idpResponse.getApiTokens().getAccessToken());
        externalTokens.setRefreshToken(idpResponse.getApiTokens().getRenewToken());
        externalTokens.setIdToken(idpResponse.getIdToken());
        externalTokens.setExpiresInSeconds(toSeconds(idpResponse.getApiTokens().getAccessTokenExpiresIn()));
        externalTokens.setRefreshExpiresInSeconds(toSeconds(idpResponse.getApiTokens().getRenewTokenExpiresIn()));
        return externalTokens;
    }

    @Override
    public ExternalIDPTokens fetchRenewedTokens(@NonNull String refreshToken) {
        var apiTokens = idpClient.fetchRenewedTokens(refreshToken);
        var externalTokens = new ExternalIDPTokens();
        externalTokens.setAccessToken(apiTokens.getAccessToken());
        externalTokens.setRefreshToken(apiTokens.getRenewToken());
        externalTokens.setExpiresInSeconds(toSeconds(apiTokens.getAccessTokenExpiresIn()));
        externalTokens.setRefreshExpiresInSeconds(toSeconds(apiTokens.getRenewTokenExpiresIn()));
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

    private long toSeconds(Duration duration) {
        return duration == null ? 0 : duration.getSeconds();
    }

}
