package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.context.AppContext;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

import java.util.List;
import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class LocalAuthenticationProviderServiceConfig {

    private final AppContext context;
    private final AuthenticationProviderConnectionFactory connectionFactory;
    private final List<LocalUserInfoService> localUserInfoServices;


    @XISBean
    Optional<LocalAuthenticationProviderService> localAuthenticationProviderService() {
        return switch (localUserInfoServices.size()) {
            case 0 -> Optional.empty();
            case 1 ->
                    Optional.of(new LocalAuthenticationProviderServiceImpl(localAuthenticationService(), localUserInfoServices.get(0)));
            default ->
                    throw new IllegalStateException("Multiple LocalUserInfoService instances found: " + localUserInfoServices.size());
        };
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
