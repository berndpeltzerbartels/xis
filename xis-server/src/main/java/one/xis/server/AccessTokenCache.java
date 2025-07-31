package one.xis.server;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.NonNull;
import one.xis.auth.AccessTokenClaims;
import one.xis.context.XISComponent;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


@XISComponent
public class AccessTokenCache {

    private final Cache<String, AccessTokenClaims> cache;

    AccessTokenCache() {
        this.cache = Caffeine.newBuilder()
                .expireAfter(new TokenExpiry())
                .build();
    }

    public AccessTokenClaims getClaims(String token, Function<String, AccessTokenClaims> decoderFunction) {
        return cache.get(token, decoderFunction);
    }

    public void putClaims(String token, AccessTokenClaims claims) {
        cache.put(token, claims);
    }


    private static class TokenExpiry implements Expiry<String, AccessTokenClaims> {
        @Override
        public long expireAfterCreate(@NonNull String key, @NonNull AccessTokenClaims value, long currentTime) {
            long seconds = value.getExpiresAtSeconds() - Instant.now().getEpochSecond();
            return TimeUnit.SECONDS.toNanos(Math.max(0, seconds));
        }

        @Override
        public long expireAfterUpdate(@NonNull String key, @NonNull AccessTokenClaims value, long currentTime, long currentDuration) {
            return expireAfterCreate(key, value, currentTime);
        }

        @Override
        public long expireAfterRead(@NonNull String key, @NonNull AccessTokenClaims value, long currentTime, long currentDuration) {
            return currentDuration;
        }
    }
}