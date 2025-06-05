package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.context.AppContext;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;
import one.xis.resource.Resources;

@XISComponent
@RequiredArgsConstructor
class LocalAuthenticationProviderServiceConfig {

    private final Resources resources;
    private final AppContext context;


    @XISBean
    LocalAuthenticationProviderService localAuthenticationService() {
        return context.getOptionalSingleton(UserService.class)
                .map(LocalAuthenticationProviderServiceImpl::new)
                .map(LocalAuthenticationProviderService.class::cast)
                .orElseGet(LocalAuthenticationNoopService::new);
    }
}
