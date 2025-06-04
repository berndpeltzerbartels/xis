package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.context.AppContext;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;
import one.xis.resource.Resource;
import one.xis.resource.Resources;

import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class LocalAuthenticationServiceConfig {

    private final Resources resources;
    private final AppContext context;


    @XISBean
    Optional<LocalAuthenticationService> localAuthenticationService() {
        return context.getOptionalSingleton(UserService.class)
                .map(LocalAuthenticationServiceImpl::new);
    }


    private Optional<Resource> getLoginFormResource() {
        try {
            return Optional.of(resources.getByPath("/login.html"));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
