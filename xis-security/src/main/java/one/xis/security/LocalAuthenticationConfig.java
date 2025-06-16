package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

import java.util.List;
import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class LocalAuthenticationConfig {

    private final ApiTokenManager tokenManager;
    private final List<LocalUserInfoService> localUserInfoServices;

    @XISBean
    Optional<Authentication> localAuthentication() {
        return localUserInfoService().map(userService -> new AuthenticationImpl(userService, tokenManager));
    }

    private Optional<LocalUserInfoService> localUserInfoService() {
        return switch (localUserInfoServices.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(localUserInfoServices.get(0));
            default ->
                    throw new IllegalStateException("Multiple " + LocalUserInfoService.class.getSimpleName() + " instances found: " + localUserInfoServices.size());
        };
    }
}
