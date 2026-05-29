package one.xis.validation;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Signals that an action could not be completed because a business validation rule failed.
 *
 * <p>Throw this exception from an {@code @Action} method when the failure should
 * be rendered as ordinary validation feedback instead of as a technical server
 * error. It can carry global form messages, field-bound messages, or both.</p>
 *
 * <p>This is a special validation path for action logic. Annotation-based
 * validation should still use {@link Validator} and {@link ValidatorException}.</p>
 */
@Getter
public class ValidationFailedException extends RuntimeException {
    private final List<ValidationMessage> globalMessages = new ArrayList<>();
    private final Map<String, ValidationMessage> fieldMessages = new LinkedHashMap<>();

    public ValidationFailedException(@NonNull String globalMessageKey) {
        addGlobalMessage(globalMessageKey);
    }

    public ValidationFailedException(@NonNull String field, @NonNull String fieldMessageKey) {
        addFieldMessage(field, fieldMessageKey);
    }

    /**
     * Adds a global validation message key.
     */
    public ValidationFailedException addGlobalMessage(@NonNull String messageKey) {
        return addGlobalMessage(messageKey, Map.of());
    }

    /**
     * Adds a global validation message key with interpolation parameters.
     */
    public ValidationFailedException addGlobalMessage(@NonNull String messageKey, @NonNull Map<String, Object> messageParameters) {
        globalMessages.add(new ValidationMessage(messageKey, messageParameters));
        return this;
    }

    /**
     * Adds a field-bound validation message key.
     */
    public ValidationFailedException addFieldMessage(@NonNull String field, @NonNull String messageKey) {
        return addFieldMessage(field, messageKey, Map.of());
    }

    /**
     * Adds a field-bound validation message key with interpolation parameters.
     */
    public ValidationFailedException addFieldMessage(@NonNull String field, @NonNull String messageKey, @NonNull Map<String, Object> messageParameters) {
        fieldMessages.put(field, new ValidationMessage(messageKey, messageParameters));
        return this;
    }

    /**
     * Message key plus interpolation parameters for one validation message.
     */
    public record ValidationMessage(String messageKey, Map<String, Object> messageParameters) {
        public ValidationMessage {
            messageParameters = Map.copyOf(messageParameters);
        }
    }
}
