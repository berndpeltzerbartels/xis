package one.xis.auth.token;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

public record TokenCreationAttributes(String userId, Collection<String> roles, Map<String, Object> claims,
                                      Duration expiresIn) {
}
