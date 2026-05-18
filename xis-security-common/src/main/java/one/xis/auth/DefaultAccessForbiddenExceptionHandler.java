package one.xis.auth;

import one.xis.context.DefaultComponent;
import one.xis.http.ControllerExceptionHandler;
import one.xis.http.ResponseEntity;

import java.lang.reflect.Method;

@DefaultComponent
class DefaultAccessForbiddenExceptionHandler implements ControllerExceptionHandler<AccessForbiddenException> {
    @Override
    public ResponseEntity<?> handleException(Method method, Object[] args, AccessForbiddenException exception) {
        return ResponseEntity.status(401).body("Authentication failed: " + exception.getMessage());
    }
}
