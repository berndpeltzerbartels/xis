package one.xis.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for an authentication provider, such as OIDC (OpenID Connect).
 * This class holds the necessary details to connect to an authentication provider,
 * including client credentials, endpoints, and application root URL.
 * <p>
 * For each instance in context an {@link AuthenticationProviderService} is created
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationProviderConfiguration {

    /**
     * The client id is used to identify the client application with the authentication provider.
     * It is a public value and can be shared with the authentication provider.
     */
    private String clientId;

    /**
     * The client secret is used to authenticate the client application with the authentication provider.
     * It is a sensitive value and should be kept secret.
     */
    private String clientSecret;

    /**
     * The id of the authentication provider, like "oidc", "oidc-google", "oidc-github" or "oidc-azure".
     * It is used to identify the provider in the system. Name is of free choice, but should be unique.
     * We append this to authentication callback url, so it is used to identify the provider in the system.
     */
    private String authenticationProviderId;

    /**
     * The url of the authentication provider we redirect to for login.
     * It might end with context path, too.
     */
    private String authorizationEndpoint;


    /**
     * This url is, where we submit the code, the provider returns after successful login
     * and the state parameter. It will start with providers server url, like <a href="https://oidc.example.com/token">https://oidc.example.com/token</a>.
     * It is used to exchange the code for access and renew tokens.
     */
    private String tokenEndpoint;


    /**
     * This url is used to retrieve the user information from authentication provider after successful login.
     * It will start with providers server url, like <a href="https://oidc.example.com/userinfo">https://oidc.example.com/userinfo</a>.
     * It is used to get the user id, roles and other attributes.
     */
    private String userInfoEndpoint;

    public static final String CALLBACK_URL = "/xis/auth";


    /**
     * The root URL of this application like <a href="http://localhost:8080">http://localhost:8080</a>
     * or <a href="https://example.com">.https://example.co</a>. It might end with context path, too.
     */
    private String applicationRootEndpoint = "";


    /**
     * Returns the callback URL for the authentication provider.
     * This URL is used to redirect users back to the application after successful authentication.
     *
     * @return The callback URL as a String.
     */
    public String getCallbackUrl() {
        return applicationRootEndpoint + CALLBACK_URL;
    }
}
