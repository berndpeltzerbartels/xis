package one.xis.security;

/**
 * Interface for Identity Provider (IDP) services that handle user authentication,
 * issue tokens, and manage user sessions.
 * This service is designed to use XIS for identity management and authentication.
 */
public interface IDPService {

    /**
     * Logs in a user with the provided credentials.
     *
     * @param login the login credentials containing username and password and state
     * @return a unique code representing the login session
     * @throws InvalidCredentialsException if the credentials are invalid
     */
    String login(IDPLogin login) throws InvalidCredentialsException;

    /**
     * Issues an authentication token based on the provided code and state.
     *
     * @param code  the unique code obtained from login
     * @param state an optional state parameter for maintaining session state
     * @return a response containing access and refresh tokens
     * @throws AuthenticationException if the code is invalid or expired
     */
    IDPTokens issueToken(String code, String state) throws AuthenticationException;

    /**
     * Refreshes the authentication token using the provided refresh token.
     *
     * @param refreshToken the refresh token to use for obtaining a new access token
     * @return a response containing a new access token and possibly a new refresh token
     * @throws InvalidTokenException   if the refresh token is invalid or expired
     * @throws AuthenticationException if there is an error during the refresh process
     */
    IDPTokens refresh(String refreshToken) throws InvalidTokenException, AuthenticationException;

    /**
     * Retrieves user information based on the provided access token.
     *
     * @param accessToken the access token to verify and extract user information
     * @return a LocalUserInfo object containing user details
     * @throws InvalidTokenException if the access token is invalid or expired
     */
    LocalUserInfo getUserInfo(String accessToken) throws InvalidTokenException;

    void checkRedirectUrl(String redirectUrl) throws InvalidRedirectUrlException;

}
