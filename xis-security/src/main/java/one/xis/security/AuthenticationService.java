package one.xis.security;

import lombok.NonNull;


// TODO Die interfaces sind für den User nicht sinnvoll zusammengefasst.
// TODO Es sollte eine Interface für den LocalAuthenticationProvider geben und eines für Authentication
public interface AuthenticationService {
    /**
     * Creates an authorization URL for the authentication provider.
     * This URL is used to redirect users to the provider's login page.
     *
     * @return The authorization URL as a String.
     */
    String createAuthorizationUrl();


    /**
     * Creates an authorization URL for the authentication provider.
     * This URL is used to redirect users to the provider's login page.
     *
     * @param providerLoginFormUrl The landing page to redirect to after successful login.
     * @return The authorization URL as a String.
     */
    String createLoginUrl(String providerLoginFormUrl);


    /**
     * Verifies the state parameter and extracts the authorization code from the redirect URL.
     * This method is called after the user has logged in and been redirected back to the application.
     *
     * @param queryString The query-string to verify, which should contain the state and code parameters.
     * @return The extracted authorization code as a String.
     */
    AuthenticationProviderStateData verifyAndDecodeCodeAndStateQuery(@NonNull String queryString);

    /**
     * Verifies the state and code parameters.
     * This method checks if the provided state matches the expected state
     * and if the code is valid.
     *
     * @param state
     */
    StateParameterPayload verifyState(@NonNull String state);

    /**
     * Requests tokens from the authentication provider using the provided authorization code.
     * This method exchanges the authorization code for access and refresh tokens.
     *
     * @param code The authorization code received from the authentication provider.
     * @return An instance of {@link AuthenticationProviderTokens} containing the tokens.
     */
    AuthenticationProviderTokens requestTokens(@NonNull String code, @NonNull String state);

    String createStateParameter(String urlAfterLogin);

    String getProviderId();

}
