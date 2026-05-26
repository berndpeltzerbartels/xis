package one.xis.validation;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Signals that an action could not be completed because a business validation rule failed.
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

    public ValidationFailedException addGlobalMessage(@NonNull String messageKey) {
        return addGlobalMessage(messageKey, Map.of());
    }

    public ValidationFailedException addGlobalMessage(@NonNull String messageKey, @NonNull Map<String, Object> messageParameters) {
        globalMessages.add(new ValidationMessage(messageKey, messageParameters));
        return this;
    }

    public ValidationFailedException addFieldMessage(@NonNull String field, @NonNull String messageKey) {
        return addFieldMessage(field, messageKey, Map.of());
    }

    public ValidationFailedException addFieldMessage(@NonNull String field, @NonNull String messageKey, @NonNull Map<String, Object> messageParameters) {
        fieldMessages.put(field, new ValidationMessage(messageKey, messageParameters));
        return this;
    }

    public record ValidationMessage(String messageKey, Map<String, Object> messageParameters) {
        public ValidationMessage {
            messageParameters = Map.copyOf(messageParameters);
        }
    }
}
