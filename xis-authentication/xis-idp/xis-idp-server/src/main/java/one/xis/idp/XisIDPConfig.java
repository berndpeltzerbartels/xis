package one.xis.idp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Configuration for an authentication provider, such as OIDC (OpenID Connect).
 * This class holds the necessary details to connect to an authentication provider,
 * including client credentials, endpoints, and application root URL.
 * <p>
 * For each instance in context an {@link ExternalIDPService} is created
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XisIDPConfig implements ExternalIDPConfig {

    private static final String CLIENT_ID = "xis-idp-client";
    private static final String CLIENT_SECRET = UUID.randomUUID().toString();


    public static final String IDP_LOGIN_URL = "/idp/login.html";

    private String url;

    /**
     * The client id is used to identify the client application with the authentication provider.
     * It is a public value and can be shared with the authentication provider.
     */
    @Override
    public String getClientId() {
        return CLIENT_ID;
    }

    /**
     * The client secret is used to authenticate the client application with the authentication provider.
     * It is a sensitive value and should be kept secret.
     */
    @Override
    public String getClientSecret() {
        return CLIENT_SECRET;
    }

    /**
     * The id of the authentication provider, like "oidc", "oidc-google", "oidc-github" or "oidc-azure".
     * It is used to identify the provider in the system. Name is of free choice, but should be unique.
     * We append this to authentication callback url, so it is used to identify the provider in the system.
     */
    private final String idpId = "xis-idp";


    /**
     * The url of the authentication provider we redirect to for login.
     * It might end with context path, too.
     */
    private final String loginFormUrl = IDP_LOGIN_URL;


    /**
     * This url is, where we submit the code, the provider returns after successful login
     * and the state parameter. It will start with providers server url, like <a href="https://oidc.example.com/token">https://oidc.example.com/token</a>.
     * It is used to exchange the code for access and renew tokens.
     */
    private final String tokenEndpoint = "/xis/idp/tokens";

    /**
     * This url is used to renew the access token.
     * It will start with providers server url, like <a href="https://oidc.example.com/renew">https://oidc.example.com/renew</a>.
     * It is used to renew the access token using the refresh token.
     * It is optional, if the provider does not support token renewal.
     */
    private final String renewTokenEndpoint = "/xis/idp/token/renew"; // TODO constants for well known  - config and here


    /**
     * This url is used to retrieve the user information from authentication provider after successful login.
     * It will start with providers server url, like <a href="https://oidc.example.com/userinfo">https://oidc.example.com/userinfo</a>.
     * It is used to get the user id, roles and other attributes.
     */
    private final String userInfoEndpoint = "/xis/idp/userinfo";

    /**
     * Returns the callback URL for the authentication provider.
     * This URL is used to redirect users back to the application after successful authentication.
     *
     * @return The callback URL as a String.
     */
    @Override
    public String getCallbackUrl() {
        StringBuilder callbackUrl = new StringBuilder();
        if (url != null && !url.isEmpty()) {
            callbackUrl.append(url);
        }
        return callbackUrl.append("/xis/auth/").append(idpId).toString();
    }
}
