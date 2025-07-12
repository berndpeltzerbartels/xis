package one.xis.auth.token;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

public record TokenAttributes(String userId, Collection<String> roles, Map<String, Object> claims, Instant expiresAt) {

}
