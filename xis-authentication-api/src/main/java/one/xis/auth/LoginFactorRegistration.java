package one.xis.auth;

/**
 * Optional registration link shown on the local login page for an additional login factor.
 *
 * @param url        target URL for the registration flow
 * @param messageKey message key resolved from classpath {@code messages*.properties}
 */
public record LoginFactorRegistration(String url, String messageKey) {
}
