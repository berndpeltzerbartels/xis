package one.xis.ipdclient;

import one.xis.security.UserInfo;

/**
 * An optional service that can be implemented by the application
 * to create or update users in the local database (provisioning)
 * after successful authentication with an external IDP.
 * <p>
 * If this service is present in the context, its {@link #provisionUser} method
 * will be called automatically after the user information has been successfully
 * validated and extracted from the IDP.
 */
public interface UserInfoProvisioningService {

    /**
     * Called after a user has successfully authenticated via an IDP.
     * Implement this method to create or update a user in your local database
     * based on the information received from the IDP.
     *
     * @param userInfo The user information received and extracted from the IDP.
     * @param idpId    The ID of the Identity Provider through which the authentication occurred.
     * @return The potentially updated or locally enriched user information.
     * Typically, the input {@code userInfo} object is returned, unless it is
     * to be enriched with additional information for the remainder of the request.
     */
    UserInfo provisionUser(UserInfo userInfo, String idpId);
}