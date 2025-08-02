package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.UserContextImpl;
import one.xis.auth.*;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.auth.idp.ExternalIDPTokens;
import one.xis.auth.token.SecurityAttributes;
import one.xis.auth.token.TokenStatus;
import one.xis.auth.token.UserSecurityService;
import one.xis.context.AppContext;
import one.xis.context.XISComponent;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;

@XISComponent
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
            return;
        }
        if (isAccessTokenValid(tokenStatus)) {
            updateUserContextWithValidAccessToken(tokenStatus, (SecurityAttributesImpl) securityAttributes);
            return;
        }
        String issuer = tokenService.extractIssuer(tokenStatus.getAccessToken());
        if ("local".equals(issuer)) {
            renewLocalTokensAndUpdateContext(tokenStatus, (UserContext) securityAttributes);
        } else {
            renewExternalTokensAndUpdateContext(tokenStatus, (UserContext) securityAttributes, issuer);
        }
    }

    private boolean isAccessTokenValid(TokenStatus tokenStatus) {
        try {
            AccessTokenClaims claims = accessTokenCache.getClaims(tokenStatus.getAccessToken(), this::decodeAccessToken);
            return claims.getExpiresAtSeconds() > Instant.now().getEpochSecond();
        } catch (TokenExpiredException e) {
            return false;
        }
    }

    private void updateUserContextWithValidAccessToken(TokenStatus tokenStatus, SecurityAttributesImpl securityAttributes) {
        AccessTokenClaims claims = accessTokenCache.getClaims(tokenStatus.getAccessToken(), this::decodeAccessToken);
        securityAttributes.setUserId(claims.getUserId());
        securityAttributes.setRoles(Optional.ofNullable(claims.getResourceAccess())
                .map(AccessTokenClaims.ResourceAccess::getAccount)
                .map(AccessTokenClaims.ResourceAccess.Account::getRoles)
                .map(HashSet::new)
                .orElse(new HashSet<>()));
    }

    private void renewLocalTokensAndUpdateContext(TokenStatus tokenStatus, UserContext userContext) {
        String userId = tokenService.extractUserId(tokenStatus.getAccessToken());
        var userInfoService = appContext.getOptionalSingleton(UserInfoService.class)
                .orElseThrow(() -> new IllegalStateException("UserInfoService not found in AppContext"));
        var userInfo = (UserInfo) userInfoService.getUserInfo(userId).orElseThrow();
        var tokens = localTokenService.renewTokens(tokenStatus.getRenewToken());
        tokenStatus.setAccessToken(tokens.getAccessToken());
        tokenStatus.setRenewToken(tokens.getRenewToken());
        tokenStatus.setExpiresIn(tokens.getAccessTokenExpiresIn());
        tokenStatus.setRenewExpiresIn(tokens.getRenewTokenExpiresIn());
        UserContextImpl contextImpl = (UserContextImpl) userContext;
        ((SecurityAttributesImpl) contextImpl.getSecurityAttributes()).setUserId(userId);
        ((SecurityAttributesImpl) contextImpl.getSecurityAttributes()).setRoles(userInfo.getRoles());
    }

    private void renewExternalTokensAndUpdateContext(TokenStatus tokenStatus, UserContext userContext, String issuer) {
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
        UserContextImpl contextImpl = (UserContextImpl) userContext;
        ((SecurityAttributesImpl) contextImpl.getSecurityAttributes()).setUserId(renewTokenClaims.getUserId());
        ((SecurityAttributesImpl) contextImpl.getSecurityAttributes()).setRoles(new HashSet<>(accessTokenClaims.getRoles()));
    }

    private AccessTokenClaims decodeAccessToken(String token) {
        var issuer = tokenService.extractIssuer(token);
        if (issuer.equals("local")) {
            return localTokenService.decodeAccessToken(token);
        }
        var externalIdpService = externalIDPServices.getServiceForIssuer(issuer);
        if (externalIdpService == null) {
            throw new AuthenticationException("No external IDP service found for issuer: " + issuer);
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
