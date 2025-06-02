package one.xis.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
