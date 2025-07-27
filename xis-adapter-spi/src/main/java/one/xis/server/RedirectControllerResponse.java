package one.xis.server;

/**
 * Represents a response that indicates a redirect to a different URL.
 * <p>
 * This is a special case for controller responses. Unlike other responses that might load new content
 * into the existing page skeleton (e.g., via AJAX), this response signifies a "real" HTTP redirect.
 * <p>
 * Its primary intended use is for internal purposes, especially within authentication and authorization flows,
 * like redirecting a user to an identity provider (IDP) login page.
 * <p>
 * If an action method (annotated by @Action) returns an implementation of this interface, the framework will give it priority
 * and execute the HTTP redirect immediately, bypassing the standard content rendering process.
 */
public interface RedirectControllerResponse {

    /**
     * Gets the full URL to which the client should be redirected.
     *
     * @return The redirect URL.
     */
    String getRedirectUrl();


}