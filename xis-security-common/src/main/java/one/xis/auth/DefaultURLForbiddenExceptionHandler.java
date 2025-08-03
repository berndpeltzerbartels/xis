package one.xis.auth;

import one.xis.context.XISDefaultComponent;
import one.xis.http.ControllerExceptionHandler;
import one.xis.http.ResponseEntity;

import java.lang.reflect.Method;

@XISDefaultComponent
class DefaultURLForbiddenExceptionHandler implements ControllerExceptionHandler<URLForbiddenException> {
    @Override
    public ResponseEntity<?> handleException(Method method, Object[] args, URLForbiddenException exception) {
        return ResponseEntity.status(401).body("Authentication failed: " + exception.getMessage());
    }
}
