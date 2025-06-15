package one.xis.security;

import one.xis.server.ApiTokens;

/**
 * Interface for handling user authentication.
 * Provides a method to log in with a username and password.
 * <p>
 * Is only present in case we have an instance of one.xis.security.UserService.
 */
public interface LocalAuthentication {

    ApiTokens login(String username, String password) throws AuthenticationException;
}
