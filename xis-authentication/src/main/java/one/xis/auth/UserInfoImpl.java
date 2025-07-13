package one.xis.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * Represents user information, typically decoded from a JWT but it is not intended
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoImpl implements UserInfo {

    /**
     * The unique identifier for the user (Subject). Corresponds to the 'sub' claim.
     */
    private String userId;
    

    /**
     * The roles assigned to the user (e.g., 'admin', 'user').
     * Extracted from custom claims like 'roles', 'groups', or Keycloak's 'realm_access'.
     */
    private Set<String> roles;

    /**
     * A map containing all claims from the token.
     * Useful for accessing custom or non-standard claims from the IDP.
     */
    private Map<String, Object> claims;
}