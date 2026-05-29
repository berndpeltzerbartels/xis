package one.xis.http;

import java.lang.reflect.Method;

/**
 * Handles exceptions thrown by plain HTTP controller methods.
 *
 * <p>Register an implementation as a XIS component when an application wants to
 * convert a specific exception type into an HTTP response body, status, or
 * headers.</p>
 *
 * @param <T> exception type handled by this handler
 */
public interface ControllerExceptionHandler<T extends Throwable> {

    /**
     * Converts a controller exception into a response.
     *
     * @param method controller method that failed
     * @param args arguments passed to the controller method
     * @param exception thrown exception
     * @return HTTP response to send to the client
     */
    ResponseEntity<?> handleException(Method method, Object[] args, T exception);

}
