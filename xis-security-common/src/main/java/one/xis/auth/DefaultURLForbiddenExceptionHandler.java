package one.xis.auth;

import one.xis.context.DefaultComponent;
import one.xis.http.ControllerExceptionHandler;
import one.xis.http.ResponseEntity;

import java.lang.reflect.Method;

@DefaultComponent
class DefaultURLForbiddenExceptionHandler implements ControllerExceptionHandler<URLForbiddenException> {
    @Override
    public ResponseEntity<?> handleException(Method method, Object[] args, URLForbiddenException exception) {
        return ResponseEntity.status(401).body("Authentication failed: " + exception.getMessage());
    }
}
