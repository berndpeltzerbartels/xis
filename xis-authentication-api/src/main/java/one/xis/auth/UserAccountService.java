package one.xis.auth;

import one.xis.ImportInstances;

import java.util.Optional;

/**
 * Stores and loads local application accounts.
 * <p>
 * XIS uses this service after external OpenID Connect logins when an application wants to persist, enrich, approve, or
 * assign roles to externally authenticated users before XIS creates its local application token. Local password
 * validation is handled separately by {@link LocalCredentialService}.
 */
@ImportInstances
public interface UserAccountService<U extends UserAccount> {

    /**
     * Retrieves the user information for a given user ID.
     *
     * @param userId the ID of the user
     * @return the user information
     */
    Optional<U> getUserAccount(String userId);

    /**
     * Stores or updates user information after an external OpenID Connect login.
     * <p>
     * XIS calls this method during the external login callback when the application provides a custom
     * {@code UserAccountService}. The primary purpose is mapping community or provider accounts to local application
     * accounts: store profile data, attach approval state, or assign application roles before XIS issues its local
     * application token. Applications may also call the method directly for their own user management flows.
     *
     * @param userAccount user information decoded from the external identity token and optionally enriched by the service
     */
    void saveUserAccount(U userAccount);

}
