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
     * The password of the user. This is typically not stored in the token but may be used for local authentication.
     * It is included here for convenience, but should be handled securely and not exposed unnecessarily.
     */
    private String password;

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