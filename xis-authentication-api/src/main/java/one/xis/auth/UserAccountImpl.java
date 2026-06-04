package one.xis.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.sql.JsonColumn;
import one.xis.sql.OptionalColumn;

import java.util.Set;

/**
 * Default {@link UserAccount} implementation for local and OpenID Connect users.
 *
 * <p>{@code userId} is the required stable local user id. The profile fields follow
 * common OpenID Connect claims such as {@code email}, {@code name}, and
 * {@code preferred_username}. After an external login, XIS maps those claims from
 * the provider's {@code id_token} into {@code UserAccountImpl} before it creates local
 * XIS tokens, so applications can persist or enrich provider profile data in their
 * {@link UserAccountService}.</p>
 *
 * <p>The profile fields are optional for SQL mapping, so applications can persist
 * only the columns they need. {@code roles} is mapped as an optional JSON column
 * when XIS SQL is used; applications may also load roles separately in their
 * {@link UserAccountService}.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountImpl implements UserAccount {

    /**
     * The unique identifier for the user (Subject). Corresponds to the 'sub' claim.
     */
    private String userId;


    /**
     * The roles assigned to the user (e.g., 'admin', 'user').
     * Extracted from custom claims like 'roles', 'groups', or Keycloak's 'realm_access'.
     */
    @JsonColumn
    @OptionalColumn
    private Set<String> roles;

    @OptionalColumn
    private String name;

    @OptionalColumn
    private String email;

    @OptionalColumn
    private boolean emailVerified;

    @OptionalColumn
    private String preferredUsername;

    @OptionalColumn
    private String givenName;

    @OptionalColumn
    private String familyName;

    @OptionalColumn
    private String locale;

    @OptionalColumn
    private String pictureUrl;

}
