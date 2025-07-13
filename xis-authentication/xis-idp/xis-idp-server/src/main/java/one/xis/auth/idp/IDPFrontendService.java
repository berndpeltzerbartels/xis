package one.xis.auth.idp;

import one.xis.auth.AuthenticationException;
import one.xis.auth.JsonWebKey;
import one.xis.auth.token.ApiTokensAndUrl;
import one.xis.idp.IDPResponse;

/**
 * Binds controller endpoints of a framework to the IDP service. These are the endpoints in case
 * Xis is used as an iDP (Identity Provider) for authentication.
 */
public interface IDPFrontendService { // TODO remove ?

    /**
     * Handles the authentication callback from the IDP after the user has authenticated.
     * <p>
     * The `queryString` contains the parameters returned by the IDP, such as the authorization code and state.
     * The `provider` is the name of the authentication provider (e.g., "local", "google", etc.).
     *
     * @param provider    The name of the authentication provider.
     * @param queryString The query string containing parameters returned by the IDP.
     * @return An object containing API tokens and a URL to redirect to after successful authentication.
     */
    ApiTokensAndUrl authenticationCallback(String provider, String queryString);

    /**
     * Creates a URL for the login form of the specified provider.
     * <p>
     * The `redirectUri` is the URL where the user will be redirected after successful authentication.
     * It should be a valid URL that the IDP recognizes as a valid redirect URI for the client.
     *
     * @param provider    The name of the authentication provider (e.g., "local", "google", etc.).
     * @param redirectUri The URL to redirect to after successful authentication.
     * @return The URL to the login form of the specified provider.
     */
    String createLoginFormUrl(String provider, String redirectUri);

    /**
     * Returns the OpenID Connect configuration for the IDP, which includes endpoints for authorization,
     * token exchange, user info retrieval, and public keys.
     * <p>
     *
     * @return The OpenID Connect configuration.
     */
    String getOpenIdConfigJson();

    /**
     * Returns the public key used to verify JWT signatures.
     *
     * @return
     */
    JsonWebKey getPublicKey();


    IDPResponse provideTokens(String tokenRequestPayload) throws AuthenticationException;
}
