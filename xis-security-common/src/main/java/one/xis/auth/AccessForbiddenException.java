package one.xis.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Signals that the current request is not allowed to access the requested resource.
 * <p>
 * The URL is not the security problem itself. It is the browser location XIS can use as
 * redirect target after the user has authenticated again.
 */
@Getter
@RequiredArgsConstructor
public class AccessForbiddenException extends RuntimeException {
    private final String url;
}
