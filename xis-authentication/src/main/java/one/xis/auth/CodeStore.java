package one.xis.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import one.xis.context.XISComponent;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@XISComponent
public class CodeStore {

    private final Duration expiry = Duration.ofMinutes(15);

    private final Cache<String, String> codeToUserId = CacheBuilder.newBuilder()
            .expireAfterWrite(expiry.toMinutes(), TimeUnit.MINUTES)
            .build();

    public void store(String code, String userId) { // TODO Duration, here ?
        codeToUserId.put(code, userId);
    }

    public String getUserIdForCode(String code) {
        return codeToUserId.getIfPresent(code);
    }

    public void invalidate(String code) {
        codeToUserId.invalidate(code);
    }
}
