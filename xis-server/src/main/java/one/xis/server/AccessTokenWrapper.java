package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.auth.AccessTokenClaims;
import one.xis.auth.token.AccessToken;
import one.xis.utils.lang.StringUtils;

import java.time.Instant;
import java.util.Collection;

@RequiredArgsConstructor
public class AccessTokenWrapper implements AccessToken {
    private final String accessToken;
    private final AccessTokenCache accessTokenCache;
    private AccessTokenClaims accessTokenClaims;

    @Override
    public String getToken() {
        return accessToken;
    }

    @Override
    public boolean isAuthenticated() {
        return StringUtils.isNotEmpty(getAccessTokenClaims().getUsername());
    }

    @Override
    public String getUserId() {
        return getAccessTokenClaims().getUsername();
    }

    @Override
    public Instant getExpiresAt() {
        var seconds = getAccessTokenClaims().getExpiresAtSeconds();
        return seconds == null ? null : Instant.ofEpochSecond(seconds);
    }

    @Override
    public Collection<String> getRoles() {
        return getAccessTokenClaims().getResourceAccess().getAccount().getRoles();
    }

    @Override
    public boolean isExpired() {
        return getExpiresAt() == null || Instant.now().isAfter(getExpiresAt());
    }

    private synchronized AccessTokenClaims getAccessTokenClaims() {
        if (accessTokenClaims == null) {
            if (StringUtils.isNotEmpty(accessToken)) {
                accessTokenClaims = accessTokenCache.getClaims(accessToken);
            } else {
                accessTokenClaims = new AccessTokenClaims();
            }
        }
        return accessTokenClaims;
    }
}
