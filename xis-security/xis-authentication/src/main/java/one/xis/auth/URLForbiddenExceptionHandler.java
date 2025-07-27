package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.context.XISComponent;
import one.xis.http.ControllerExceptionHandler;
import one.xis.http.ResponseEntity;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@XISComponent
@RequiredArgsConstructor
class URLForbiddenExceptionHandler implements ControllerExceptionHandler<URLForbiddenException> {

    private final ExternalIDPServices externalIDPServices;

    @Override
    public ResponseEntity<?> handleException(Method method, Object[] args, URLForbiddenException exception) {
        return ResponseEntity.status(401)
                .addHeader("Location", loginUrl(exception.getUrl()))
                .body("Authentication failed: " + exception.getMessage());
    }

    private String loginUrl(String url) {
        if (externalIDPServices.getExternalIDPServices().size() == 1) {
            var service = externalIDPServices.getExternalIDPServices().iterator().next();
            return service.createLoginUrl(url);
        }
        return "/login.html?redirect_uri=" + URLEncoder.encode(url, StandardCharsets.UTF_8);
    }
}
