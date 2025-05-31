package one.xis.security;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

public record TokenAttributes(String userId, Collection<String> roles, Map<String, String> claims, Instant expiresAt) {

}
