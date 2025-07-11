package one.xis.security;

import one.xis.ImportInstances;
import one.xis.auth.InvalidTokenException;
import one.xis.auth.UserInfo;

import java.util.Optional;

/**
 * UserInfoService provides methods to manage user information and validate user credentials.
 * It is used for user authentication and profile management. Creating an implementation of this interface
 * will activate local authentication and user profile management in the XIS security context.
 */
@ImportInstances
public interface UserInfoService<U extends UserInfo> {

    /**
     * Retrieves the user information for a given user ID.
     *
     * @param userId the ID of the user
     * @return the user information
     * @throws InvalidTokenException if the token is invalid or expired
     */
    Optional<U> getUserInfo(String userId) throws InvalidTokenException;

    /**
     * Saves the user information. This methode is used for creating or updating user profiles.
     *
     * @param userInfo the user information to save
     */
    void saveUserInfo(U userInfo);
}
