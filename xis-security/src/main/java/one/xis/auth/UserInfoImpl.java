package one.xis.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String name;

    private String email;

    private boolean emailVerified;

    private String preferredUsername;

    private String givenName;

    private String familyName;

    private String locale;
    private String pictureUrl;

}