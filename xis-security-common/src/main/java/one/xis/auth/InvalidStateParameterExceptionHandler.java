package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.http.ControllerExceptionHandler;
import one.xis.http.ResponseEntity;

import java.lang.reflect.Method;


@Component
@RequiredArgsConstructor
class InvalidStateParameterExceptionHandler implements ControllerExceptionHandler<InvalidStateParameterException> {
    
    @Override
    public ResponseEntity<?> handleException(Method method, Object[] args, InvalidStateParameterException exception) {
        if (exception.getStateParameterPayload() != null) {
            var issuer = exception.getStateParameterPayload().getIssuer();
            if ("local".equals(issuer)) {
                return ResponseEntity.redirect("/login.html?error=invalid_state");
            } else {
                return ResponseEntity.status(400, "Invalid state parameter for external issuer: " + exception.getMessage())
                        .addHeader("Location", "/login.html?error=invalid_state&issuer=" + issuer);
            }
        }
        return null;
    }
}
