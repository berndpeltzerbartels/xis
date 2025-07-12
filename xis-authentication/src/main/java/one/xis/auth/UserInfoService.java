package one.xis.auth;

import one.xis.ImportInstances;

import java.util.Optional;

/**
 * UserInfoService provides methods to manage user information and validate user credentials.
 * It is used for user authentication and profile management. Creating an implementation of this interface
 * will activate local authentication and user profile management in the XIS security context.
 */
@ImportInstances
public interface UserInfoService<U extends UserInfo> {


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
     * Saves the user information. This methode is used for creating or updating user profiles.
     *
     * @param userInfo the user information to save
     */
    void saveUserInfo(U userInfo);
}
