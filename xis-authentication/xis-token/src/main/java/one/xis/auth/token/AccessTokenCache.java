package one.xis.auth.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.NonNull;
import one.xis.context.XISComponent;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;


@XISComponent
public class AccessTokenCache {

    private final TokenService tokenService;
    private final Cache<String, TokenAttributes> cache;

    AccessTokenCache(TokenService tokenService) {
        this.tokenService = tokenService;
        this.cache = Caffeine.newBuilder()
                .expireAfter(new TokenExpiry())
                .build();
    }

    public TokenAttributes getAttributes(String token) {
        return cache.get(token, tokenService::decodeToken);
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
            return currentDuration;
        }
    }
}