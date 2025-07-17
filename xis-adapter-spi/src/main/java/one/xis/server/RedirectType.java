package one.xis.server;

public enum RedirectType {
    AJAX, // Redirect via AJAX, typically used for single-page applications (SPAs)
    BROWSER_REDIRECT, // Standard HTTP redirect, used for full page reloads
}
