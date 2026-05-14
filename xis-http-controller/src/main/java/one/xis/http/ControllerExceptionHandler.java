package one.xis.http;

import java.lang.reflect.Method;

public interface ControllerExceptionHandler<T extends Throwable> {

    ResponseEntity<?> handleException(Method method, Object[] args, T exception);

}
