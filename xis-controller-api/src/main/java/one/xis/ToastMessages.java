package one.xis;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows controller methods to enqueue toast messages for the current frontend response.
 *
 * <p>Declare this type as an action method parameter and call {@link #show(String)}
 * or {@link #show(String, ToastLevel)} while handling the action. XIS adds the
 * collected messages to the response and the browser displays them with the
 * configured toast UI.</p>
 */
@Getter
public class ToastMessages {
    private final List<ToastMessage> messages = new ArrayList<>();

    /**
     * Adds an informational toast message.
     */
    public ToastMessages show(@NonNull String message) {
        return show(message, ToastLevel.INFO);
    }

    /**
     * Adds a toast message with the given visual severity.
     */
    public ToastMessages show(@NonNull String message, @NonNull ToastLevel level) {
        messages.add(new ToastMessage(message, level));
        return this;
    }
}
