package one.xis.auth.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.NonNull;
import one.xis.auth.InvalidTokenException;
import one.xis.auth.JsonWebKey;
import one.xis.auth.TokenClaims;
import one.xis.auth.UserInfo;
import one.xis.context.XISComponent;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@XISComponent
public class CachingTokenService implements TokenService {

    private final TokenService delegate;
    private final Cache<String, TokenAttributes> cache;

    public CachingTokenService(TokenService delegate) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .expireAfter(new TokenExpiry())
                .build();
    }

    @Override
    public ApiTokens newTokens(UserInfo userInfo) {
        return delegate.newTokens(userInfo);
    }

    @Override
    public JsonWebKey getPublicJsonWebKey() {
        return delegate.getPublicJsonWebKey();
    }

    @Override
    public TokenAttributes decodeToken(String token) {
        return cache.get(token, delegate::decodeToken);
    }

    @Override
    public ApiTokens renewTokens(String token, Duration tokenExpiresIn, Duration renewTokenExpiresIn) throws InvalidTokenException {
        return delegate.renewTokens(token, tokenExpiresIn, renewTokenExpiresIn);
    }

    @Override
    public String createToken(TokenClaims claims) {
        return delegate.createToken(claims);
    }

    private static class TokenExpiry implements Expiry<String, TokenAttributes> {
        @Override
        public long expireAfterCreate(@NonNull String key, @NonNull TokenAttributes value, long currentTime) {
            long millis = Duration.between(Instant.now(), value.expiresAt()).toMillis();
            return TimeUnit.MILLISECONDS.toNanos(Math.max(0, millis));
        }

        @Override
        public long expireAfterUpdate(@NonNull String key, @NonNull TokenAttributes value, long currentTime, long currentDuration) {
            return expireAfterCreate(key, value, currentTime);
        }

        @Override
        public long expireAfterRead(@NonNull String key, @NonNull TokenAttributes value, long currentTime, long currentDuration) {
            return currentDuration; // Don't change expiry on read
        }
    }
}