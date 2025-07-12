package one.xis.auth;

import one.xis.ImportInstances;

@ImportInstances
public interface LoginUrlProvider {

    /**
     * Returns the login URL for the given redirect URI.
     *
     * @param redirectUri the URI to redirect to after login
     * @return the login URL
     */
    String loginUrl(String redirectUri);
}
