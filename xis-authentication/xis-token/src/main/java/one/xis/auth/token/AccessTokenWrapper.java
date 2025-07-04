package one.xis.auth.token;


import lombok.RequiredArgsConstructor;
import one.xis.auth.InvalidTokenException;
import one.xis.utils.lang.StringUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class AccessTokenWrapper implements AccessToken {
    private final String accessToken;
    private final TokenService accessTokenDecoder;
    private TokenAttributes tokenAttributes;


    private synchronized TokenAttributes getTokenAttributes() {
        if (tokenAttributes == null) {
            if (StringUtils.isNotEmpty(accessToken)) {
                try {
                    tokenAttributes = accessTokenDecoder.decodeToken(accessToken);
                } catch (InvalidTokenException e) {
                    throw new RuntimeException(e);
                }
            } else {
                tokenAttributes = new TokenAttributes(null, Collections.emptySet(), Map.of(), null);
            }
        }
        return tokenAttributes;
    }

    @Override
    public String getToken() {
        return accessToken;
    }

    @Override
    public boolean isAuthenticated() {
        return StringUtils.isNotEmpty(getTokenAttributes().userId());
    }

    @Override
    public String getUserId() {
        return getTokenAttributes().userId();
    }

    @Override
    public Instant getExpiresAt() {
        return getTokenAttributes().expiresAt();
    }

    @Override
    public Collection<String> getRoles() {
        return getTokenAttributes().roles();
    }

    @Override
    public boolean isExpired() {
        return getTokenAttributes().expiresAt() == null || getTokenAttributes().expiresAt().isBefore(Instant.now());
    }
}
