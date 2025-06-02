package one.xis.security;

import lombok.Data;


/**
 * Configuration for the authentication module.
 * This class holds the root URL of the application and provides a method to get the callback URL.
 * It does not contain provider-specific configurations, as those are handled by the individual authentication providers.
 */
@Data
public class AuthenticationConfiguration {


    public static final String CALLBACK_URL = "/xis/auth/callback";


    /**
     * The root URL of this application like <a href="http://localhost:8080">http://localhost:8080</a>
     * or <a href="https://example.com">.https://example.co</a>. It might end with context path, too.
     */
    private String applicationRootEndpoint;


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
