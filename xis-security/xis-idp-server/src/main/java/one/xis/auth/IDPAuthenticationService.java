package one.xis.auth;

import one.xis.ImportInstances;

import java.util.Collection;

/**
 * Interface for Identity Provider (IDP) services that handle user authentication,
 * issue tokens, and manage user sessions.
 * This service is designed to use XIS for identity management and authentication.
 */
@ImportInstances
public interface IDPAuthenticationService {

    /**
     * Logs in a user with the provided credentials.
     *
     * @param login the login credentials containing username and password and state
     * @return a unique code representing the login session
     * @throws InvalidCredentialsException if the credentials are invalid
     */
    String login(IDPServerLogin login) throws InvalidCredentialsException;

    /**
     * Checks if the provided redirect URL is valid and safe for redirection.
     *
     * @param userId      the ID of the user requesting the redirect
     * @param redirectUrl the URL to check
     * @throws InvalidRedirectUrlException if the redirect URL is invalid or unsafe
     */
    void checkRedirectUrl(String userId, String redirectUrl) throws InvalidRedirectUrlException;

    IDPWellKnownOpenIdConfig getOpenIdConfigJson();

    IDPTokenResponse provideTokens(IDPTokenRequest request);

    Collection<JsonWebKey> getPublicJsonWebKeys();
}
