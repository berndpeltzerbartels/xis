package one.xis.auth;

import one.xis.context.Component;
import one.xis.http.ControllerExceptionHandler;
import one.xis.http.ResponseEntity;

import java.lang.reflect.Method;

@Component
class AuthenticationExceptionHandler implements ControllerExceptionHandler<AuthenticationException> {
    @Override
    public ResponseEntity<?> handleException(Method method, Object[] args, AuthenticationException exception) {
        return ResponseEntity.status(401)
                .body("Authentication failed: " + exception.getMessage());
    }
}
