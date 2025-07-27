package one.xis.auth.token;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record TokenAttributes(String userId, String issuer, Collection<String> roles, Map<String, String> claims,
                              Instant expiresAt) {


    public Map<String, String> asClaims() {
        Map<String, String> claimsMap = new HashMap<>(claims);
        claimsMap.put("sub", userId);
        claimsMap.put("exp", String.valueOf(expiresAt.getEpochSecond()));
        claimsMap.put("iss", issuer);
        if (roles != null && !roles.isEmpty()) {
            claimsMap.put("roles", String.join(",", roles));
        }
        return claimsMap;
    }

}
