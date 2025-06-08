package one.xis.security;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

public record TokenRequest(String userId, Collection<String> roles, Map<String, Object> claims,
                           Duration tokenAliveTime,
                           Duration renewTokenAliveTime) {
}
