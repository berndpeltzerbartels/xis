package one.xis.auth;

import one.xis.ImportInstances;

import java.util.Optional;

/**
 * UserInfoService provides methods to manage user information and validate user credentials.
 * It is used for user authentication and profile management. By default, creating an implementation of this interface
 * activates local authentication and user profile management in the XIS security context.
 */
@ImportInstances
public interface UserInfoService<U extends UserInfo> {

    /**
     * Returns whether this service supports local username/password login.
     * <p>
     * Override this method and return {@code false} when the service is only used to store or load users that were
     * authenticated by an external OpenID Connect provider. XIS can then still create local application tokens from
     * external identities without rendering or selecting the local login form.
     *
     * @return true if XIS should offer local username/password login
     */
    default boolean supportsLocalLogin() {
        return true;
    }

    /**
     * Validates the user credentials against the stored user information.
     *
     * @param userId   the ID of the user
     * @param password the password of the user
     * @return true if the credentials are valid, false otherwise
     */
    boolean validateCredentials(String userId, String password);

    /**
     * Retrieves the user information for a given user ID.
     *
     * @param userId the ID of the user
     * @return the user information
     */
    Optional<U> getUserInfo(String userId);

    /**
     * Stores or updates user information after an external OpenID Connect login.
     * <p>
     * XIS calls this method during the external login callback when the application provides a custom
     * {@code UserInfoService}. The primary purpose is mapping community or provider accounts to local application
     * accounts: store profile data, attach approval state, or assign application roles before XIS issues its local
     * application token. Applications may also call the method directly for their own user management flows.
     *
     * @param userInfo user information decoded from the external identity token and optionally enriched by the service
     */
    void saveUserInfo(U userInfo);

}
