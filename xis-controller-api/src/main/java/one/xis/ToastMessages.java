package one.xis;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows controller methods to enqueue toast messages for the current frontend response.
 */
@Getter
public class ToastMessages {
    private final List<ToastMessage> messages = new ArrayList<>();

    public ToastMessages show(@NonNull String message) {
        return show(message, ToastLevel.INFO);
    }

    public ToastMessages show(@NonNull String message, @NonNull ToastLevel level) {
        messages.add(new ToastMessage(message, level));
        return this;
    }
}
