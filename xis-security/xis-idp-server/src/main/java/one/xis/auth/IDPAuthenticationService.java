package one.xis.auth;

import one.xis.ImportInstances;

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
     * Refreshes the authentication token using the provided refresh token.
     *
     * @param refreshToken the refresh token to use for obtaining a new access token
     * @return a response containing a new access token and possibly a new refresh token
     * @throws InvalidTokenException   if the refresh token is invalid or expired
     * @throws AuthenticationException if there is an error during the refresh process
     */
    IDPTokenResponse refresh(String refreshToken) throws InvalidTokenException, AuthenticationException;

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

    JsonWebKey getPublicJsonWebKey();
}
