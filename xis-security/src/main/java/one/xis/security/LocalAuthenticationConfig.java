package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.context.AppContext;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class LocalAuthenticationConfig {

    private final ApiTokenManager tokenManager;
    private final AppContext context;

    @XISBean
    Optional<LocalAuthentication> localAuthentication() {
        return context.getOptionalSingleton(UserService.class).map(userService -> new LocalAuthenticationImpl(userService, tokenManager));
    }
}
