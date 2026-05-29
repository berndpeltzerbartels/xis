package one.xis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Immutable toast notification sent to the browser as part of a controller
 * response.
 *
 * <p>Application code usually creates messages through {@link ToastMessages}
 * instead of constructing this class directly.</p>
 */
@Getter
@RequiredArgsConstructor
public class ToastMessage {
    private final String message;
    private final ToastLevel level;
}
