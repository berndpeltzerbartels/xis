package one.xis.security;

import one.xis.ImportInstances;
import one.xis.auth.InvalidTokenException;
import one.xis.auth.UserInfo;

/**
 * UserInfoService provides methods to manage user information and validate user credentials.
 * It is used for user authentication and profile management. Creating an implementation of this interface
 * will activate local authentication and user profile management in the XIS security context.
 */
@ImportInstances
public interface UserInfoService<U extends UserInfo> {

    /**
     * Checks if the provided user credentials are valid.
     *
     * @param userId   the ID of the user
     * @param password the password of the user
     * @return true if the credentials are valid, false otherwise
     */// TODO überflüssig ? Sollte im IDP gemacht werden
    boolean checkCredentials(String userId, String password);

    /**
     * Retrieves the user information for a given user ID.
     *
     * @param userId the ID of the user
     * @return the user information
     * @throws InvalidTokenException if the token is invalid or expired
     */
    U getUserInfo(String userId) throws InvalidTokenException;

    /**
     * Saves the user information. This methode is used for creating or updating user profiles.
     *
     * @param userInfo the user information to save
     * @param idpId    the ID of the Identity Provider (IDP) associated with the user
     */
    void saveUserInfo(U userInfo, String idpId);
}
