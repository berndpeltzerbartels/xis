package one.xis.auth;

import one.xis.auth.token.ApiTokensAndUrl;

/**
 * Service interface for handling local authentication processes.
 * Login procedure itself is located in IDPLoginController.
 */
public interface LocalAuthenticationService {

    /**
     * Generates a login URL for the user to authenticate.
     *
     * @param redirectUrl The URL to redirect to after successful login.
     * @return The login URL.
     */
    String loginUrl(String redirectUrl);

    /**
     * Handles the authentication callback after the user has authenticated.
     *
     * @param code  The authorization code returned by the authentication provider.
     * @param state The state parameter to verify the authenticity of the request.
     * @return An object containing API tokens and a URL to redirect to after successful authentication.
     */
    ApiTokensAndUrl authenticationCallback(String code, String state);


}
