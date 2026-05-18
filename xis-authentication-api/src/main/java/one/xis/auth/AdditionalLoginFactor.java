package one.xis.auth;

import one.xis.ImportInstances;

import java.util.Optional;

/**
 * Optional extension point for authentication modules that add another local login factor after username/password
 * validation.
 * <p>
 * Implementations are regular XIS components. If a module such as {@code xis-totp} is on the classpath, the local login
 * form asks the factor whether an additional field is needed for the submitted user and verifies the submitted value
 * before XIS creates a login code.
 */
@ImportInstances
public interface AdditionalLoginFactor {

    /**
     * Technical name used by the login form data object.
     *
     * @return form field name, for example {@code totpCode}
     */
    String fieldName();

    /**
     * Returns whether this factor is required for the user currently attempting to log in.
     *
     * @param userId validated local user id
     * @return true when the factor value must be provided and verified
     */
    boolean isRequired(String userId);

    /**
     * Verifies the submitted factor value.
     *
     * @param userId validated local user id
     * @param value  submitted factor value; may be null or blank
     * @return true when the value is accepted
     */
    boolean verify(String userId, String value);

    /**
     * Optional link to a registration flow for this factor.
     *
     * @return registration metadata, or empty when the login page should not show a registration link
     */
    default Optional<LoginFactorRegistration> registration() {
        return Optional.empty();
    }
}
