package one.xis.server;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.UserContextImpl;
import one.xis.auth.*;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.auth.idp.ExternalIDPTokens;
import one.xis.auth.token.TokenManager;
import one.xis.auth.token.TokenStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;

@RequiredArgsConstructor
public class TokenManagerImpl implements TokenManager {

    private final TokenService tokenService;
    private final ExternalIDPServices externalIDPServices;


    private final AccessTokenCache accessTokenCache;

    @Override
    public void updateUserContext(TokenStatus tokenStatus, UserContext userContext) {
        if (tokenStatus.getAccessToken() == null || tokenStatus.getAccessToken().isEmpty()) {
            return;
        }
        UserContextImpl context = (UserContextImpl) userContext;
        AccessTokenClaims accessTokenClaims = validAccessTokenClaims(tokenStatus);
        context.setUserId(accessTokenClaims.getUsername());
        context.setRoles(new HashSet<>(accessTokenClaims.getResourceAccess().getAccount().getRoles()));
    }


    private AccessTokenClaims validAccessTokenClaims(@NonNull TokenStatus tokenStatus) throws TokenExpiredException {
        AccessTokenClaims claims;
        try {
            claims = accessTokenCache.getClaims(tokenStatus.getAccessToken(), this::decodeAccessToken);
            if (claims.getExpiresAtSeconds() > Instant.now().getEpochSecond()) {
                return claims;
            }
        } catch (TokenExpiredException e) {
            // NOOP
        }
        String issuer = tokenService.extractIssuer(tokenStatus.getAccessToken());
        if (issuer.equals("local")) {
            throw new TokenExpiredException();
        }
        var externalIdpService = externalIDPServices.getServiceForIssuer(issuer);
        if (externalIdpService == null) {
            throw new IllegalStateException("No external IDP service found for issuer: " + issuer);
        }
        ExternalIDPTokens tokens;
        try {
            tokens = externalIdpService.fetchRenewedTokens(tokenStatus.getRenewToken());
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
        claims = accessTokenCache.getClaims(tokens.getAccessToken(), this::decodeAccessToken);
        var renewTokenClaims = decodeRenewToken(tokens.getRefreshToken());
        tokenStatus.setAccessToken(tokens.getAccessToken());
        tokenStatus.setRenewToken(tokens.getRefreshToken());
        tokenStatus.setExpiresIn(Duration.between(Instant.now(), Instant.ofEpochSecond(claims.getExpiresAtSeconds())));
        tokenStatus.setRenewExpiresIn(Duration.between(Instant.now(), Instant.ofEpochSecond(renewTokenClaims.getExpiresAtSeconds())));
        return claims;
    }

    private AccessTokenClaims decodeAccessToken(String token) {
        var issuer = tokenService.extractIssuer(token);
        if (issuer.equals("local")) {
            return tokenService.decodeAccessToken(token);
        }
        var externalIdpService = externalIDPServices.getServiceForIssuer(issuer);
        if (externalIdpService == null) {
            throw new IllegalStateException("No external IDP service found for issuer: " + issuer);
        }
        var keyId = tokenService.extractKeyId(token);
        var publicKey = externalIdpService.getJsonWebKey(keyId);
        return tokenService.decodeAccessToken(token, publicKey);
    }


    private RenewTokenClaims decodeRenewToken(String token) {
        var issuer = tokenService.extractIssuer(token);
        if (issuer.equals("local")) {
            throw new IllegalArgumentException("Renew token cannot be decoded for local issuer");
        }
        var externalIdpService = externalIDPServices.getServiceForIssuer(issuer);
        if (externalIdpService == null) {
            throw new IllegalStateException("No external IDP service found for issuer: " + issuer);
        }
        var keyId = tokenService.extractKeyId(token);
        var publicKey = externalIdpService.getJsonWebKey(keyId);
        return tokenService.decodeRenewToken(token, publicKey);
    }


}
