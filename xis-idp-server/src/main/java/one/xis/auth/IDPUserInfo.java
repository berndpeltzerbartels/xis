package one.xis.auth;

public interface IDPUserInfo {

    String getUserId();

    /**
     * Returns the IDP client ID associated with this user info. We need this in case there are more than one IDP client
     * registered for this IDP, so we can distinguish which client the user is authenticated
     *
     * @return the client ID
     */
    String getClientId();
}
