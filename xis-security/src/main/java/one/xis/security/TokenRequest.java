package one.xis.security;

import java.util.Collection;
import java.util.Map;

public record TokenRequest(String userId, Collection<String> roles, Map<String, String> claims,
                           long tokenAliveTimeSeconds,
                           long renewTokenAliveTimeSeconds) {
}
