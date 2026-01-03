package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.auth.*;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.auth.idp.ExternalIDPTokens;
import one.xis.auth.token.SecurityAttributes;
import one.xis.auth.token.TokenStatus;
import one.xis.auth.token.UserSecurityService;
import one.xis.context.AppContext;
import one.xis.context.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserSecurityServiceImpl implements UserSecurityService {

    private final TokenService tokenService;
    private final LocalTokenService localTokenService;
    private final ExternalIDPServices externalIDPServices;
    private final AppContext appContext;


    private final AccessTokenCache accessTokenCache;

    @Override
    public void update(TokenStatus tokenStatus, SecurityAttributes securityAttributes) {
        if (tokenStatus.getAccessToken() == null || tokenStatus.getAccessToken().isEmpty()) {
            securityAttributes.setRoles(new HashSet<>());
            return;
        }

        try {
            // Attempt to use the existing access token
            updateUserContextWithValidAccessToken(tokenStatus, securityAttributes);
        } catch (TokenExpiredException e) {
            // If expired, renew it
            renewTokensAndUpdateContext(tokenStatus, securityAttributes);
        } catch (InvalidTokenException e) {
            // Token is invalid (e.g., bad signature after key rotation).
            // Treat as an anonymous user without throwing an exception.
            securityAttributes.setUserId(null);
            securityAttributes.setRoles(new HashSet<>());
        }
    }

    private void updateUserContextWithValidAccessToken(TokenStatus tokenStatus, SecurityAttributes securityAttributes) {
        AccessTokenClaims claims = accessTokenCache.getClaims(tokenStatus.getAccessToken(), this::decodeAccessToken);
        securityAttributes.setUserId(claims.getUserId());
        securityAttributes.setRoles(Optional.ofNullable(claims.getResourceAccess())
                .map(AccessTokenClaims.ResourceAccess::getAccount)
                .map(AccessTokenClaims.ResourceAccess.Account::getRoles)
                .map(HashSet::new)
                .orElse(new HashSet<>()));
    }

    private void renewTokensAndUpdateContext(TokenStatus tokenStatus, SecurityAttributes securityAttributes) {
        String issuer = tokenService.extractIssuer(tokenStatus.getAccessToken());
        if ("local".equals(issuer)) {
            renewLocalTokens(tokenStatus, securityAttributes);
        } else {
            renewExternalTokens(tokenStatus, securityAttributes, issuer);
        }
    }

    private void renewLocalTokens(TokenStatus tokenStatus, SecurityAttributes securityAttributes) {
        String userId = tokenService.extractUserId(tokenStatus.getAccessToken());
        var userInfoService = appContext.getOptionalSingleton(UserInfoService.class)
                .orElseThrow(() -> new IllegalStateException("UserInfoService not found in AppContext"));
        var userInfo = (UserInfo) userInfoService.getUserInfo(userId).orElseThrow();
        var tokens = localTokenService.renewTokens(tokenStatus.getRenewToken());
        tokenStatus.setAccessToken(tokens.getAccessToken());
        tokenStatus.setRenewToken(tokens.getRenewToken());
        tokenStatus.setExpiresIn(tokens.getAccessTokenExpiresIn());
        tokenStatus.setRenewExpiresIn(tokens.getRenewTokenExpiresIn());
        securityAttributes.setUserId(userId);
        securityAttributes.setRoles(userInfo.getRoles());
    }

    private void renewExternalTokens(TokenStatus tokenStatus, SecurityAttributes securityAttributes, String issuer) {
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
        var renewTokenClaims = decodeRenewToken(tokens.getRefreshToken());
        var accessTokenClaims = decodeAccessToken(tokens.getAccessToken());
        tokenStatus.setAccessToken(tokens.getAccessToken());
        tokenStatus.setRenewToken(tokens.getRefreshToken());
        tokenStatus.setExpiresIn(Duration.between(Instant.now(), Instant.ofEpochSecond(accessTokenClaims.getExpiresAtSeconds())));
        tokenStatus.setRenewExpiresIn(Duration.between(Instant.now(), Instant.ofEpochSecond(renewTokenClaims.getExpiresAtSeconds())));
        securityAttributes.setUserId(renewTokenClaims.getUserId());
        securityAttributes.setRoles(new HashSet<>(accessTokenClaims.getRoles()));
    }

    private AccessTokenClaims decodeAccessToken(String token) {
        var issuer = tokenService.extractIssuer(token);
        if (issuer.equals("local")) {
            return localTokenService.decodeAccessToken(token);
        }
        var externalIdpService = externalIDPServices.getServiceForIssuer(issuer);
        if (externalIdpService == null) {
            throw new InvalidTokenException("No external IDP service found for issuer: " + issuer);
        }
        var keyId = tokenService.extractKeyId(token);
        var publicKey = externalIdpService.getJsonWebKey(keyId);
        return tokenService.decodeAccessToken(token, publicKey);
    }


    private RenewTokenClaims decodeRenewToken(String token) {
        var issuer = tokenService.extractIssuer(token);
        if (issuer.equals("local")) {
            return localTokenService.decodeRenewToken(token);
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