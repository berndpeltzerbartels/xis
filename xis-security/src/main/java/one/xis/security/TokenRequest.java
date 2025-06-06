package one.xis.security;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

public record TokenRequest(String userId, Collection<String> roles, Map<String, String> claims,
                           Duration tokenAliveTime,
                           Duration renewTokenAliveTime) {
}
