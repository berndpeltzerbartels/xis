package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.context.AppContext;
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
    private final AppContext appContext;

    @Override
    public ResponseEntity<?> handleException(Method method, Object[] args, URLForbiddenException exception) {
        return ResponseEntity.status(401)
                .addHeader("Location", loginUrl(exception.getUrl()))
                .body("Authentication failed: " + exception.getMessage());
    }

    private String loginUrl(String url) {
        if (externalIDPServices.getExternalIDPServices().size() == 1 && !hasCustomUserInfoService()) {
            // If exactly one external IDP service is available and UserInfoService is not available,
            // we redirect to the login URL of that service
            var service = externalIDPServices.getExternalIDPServices().iterator().next();
            return service.createLoginUrl(url);
        }
        // If multiple IDP services are available or UserInfoService is present, we redirect to the default login page.
        // This allows the user to choose which IDP to use for authentication or use a local login, in case UserInfoService is available.
        return "/login.html?redirect_uri=" + URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    private boolean hasCustomUserInfoService() {
        return appContext.getOptionalSingleton(UserInfoService.class)
                .filter(service -> !(service instanceof UserServicePlaceholder))
                .isPresent();
    }
}
