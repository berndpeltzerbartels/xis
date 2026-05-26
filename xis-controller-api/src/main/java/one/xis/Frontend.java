package one.xis;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows a controller method to add extra values for the next frontend refresh
 * without turning those values into the method's primary return value.
 */
@Getter
public class Frontend {
    private final Map<String, Object> modelData = new HashMap<>();
    private final Map<String, Object> formData = new HashMap<>();
    private final List<ToastMessage> toastMessages = new ArrayList<>();

    public Frontend addModelData(@NonNull String key, Object value) {
        modelData.put(key, value);
        return this;
    }

    public Frontend addFormData(@NonNull String key, @NonNull Object value) {
        formData.put(key, value);
        return this;
    }

    public Frontend showToast(@NonNull String message) {
        return showToast(message, ToastLevel.INFO);
    }

    public Frontend showToast(@NonNull String message, @NonNull ToastLevel level) {
        toastMessages.add(new ToastMessage(message, level));
        return this;
    }
}
