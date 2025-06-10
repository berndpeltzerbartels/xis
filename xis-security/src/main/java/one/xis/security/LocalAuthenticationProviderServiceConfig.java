package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.context.AppContext;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class LocalAuthenticationProviderServiceConfig {

    private final AppContext context;
    private final AuthenticationProviderConnectionFactory connectionFactory;


    @XISBean
    Optional<LocalAuthenticationProviderService> localAuthenticationProviderService() {
        return context.getOptionalSingleton(LocalUserInfoService.class)
                .map(userService -> new LocalAuthenticationProviderServiceImpl(localAuthenticationService(), userService));
    }


    private AuthenticationService localAuthenticationService() {
        return new AuthenticationServiceImpl(localAuthenticationProviderConfiguration(), connectionFactory);
    }

    private AuthenticationProviderConfiguration localAuthenticationProviderConfiguration() {
        var configuration = new AuthenticationProviderConfiguration();
        configuration.setApplicationRootEndpoint("/"); // Set the root endpoint for the local authentication provider
        configuration.setAuthenticationProviderId("local");
        configuration.setLoginFormUrl("/login.html"); // Set the authorization endpoint for local authentication
        configuration.setTokenEndpoint("/token"); // Set the token endpoint for local authentication
        configuration.setUserInfoEndpoint("/userinfo"); // Set the user info endpoint for local authentication
        return null;
    }
}
