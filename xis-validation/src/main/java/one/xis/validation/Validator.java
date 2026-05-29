package one.xis.validation;

import lombok.NonNull;
import one.xis.ImportInstances;
import one.xis.UserContext;

import java.lang.reflect.AnnotatedElement;

/**
 * Implements validation logic for a custom validation annotation.
 *
 * <p>Validators are imported by XIS through {@link ImportInstances}. A validator
 * usually reads metadata from {@code annotatedElement}, checks the submitted
 * value, and throws {@link ValidatorException} when the value is invalid.</p>
 *
 * @param <T> value type accepted by this validator
 */
@ImportInstances
public interface Validator<T> {

    /**
     * Validates one submitted value.
     *
     * @param value value to validate
     * @param annotatedElement field, record component, or parameter carrying the
     *                         validation annotation
     * @param userContext current user locale, zone, and authentication context
     * @throws ValidatorException when validation fails
     */
    void validate(@NonNull T value, @NonNull AnnotatedElement annotatedElement, @NonNull UserContext userContext) throws ValidatorException;
}
