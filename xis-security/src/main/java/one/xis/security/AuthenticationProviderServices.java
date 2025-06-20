package one.xis.security;


import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Service for managing authentication provider services.
 * This class initializes authentication provider services for each instance of
 * AuthenticationProviderConfiguration in context and provides access to these services.
 */
@XISComponent
@RequiredArgsConstructor
public class AuthenticationProviderServices {

    private final Collection<AuthenticationProviderConfig> authenticationProviderConfigurations;
    private final AuthenticationProviderConnectionFactory connectionFactory;
    private final Map<String, AuthenticationService> authenticationProviderServices = new HashMap<>();

    /**
     * Initializes the authentication provider services based on the provided configurations.
     */
    @XISInit
    public void initialize() {
        for (AuthenticationProviderConfig providerConfiguration : authenticationProviderConfigurations) {
            AuthenticationService service = new AuthenticationServiceImpl(providerConfiguration, connectionFactory);
            authenticationProviderServices.put(service.getProviderId(), service);
        }
    }

    /**
     * Returns the authentication provider service for the given provider ID.
     *
     * @param providerId the ID of the authentication provider
     * @return the authentication provider service
     */
    public AuthenticationService getAuthenticationProviderService(String providerId) {
        return authenticationProviderServices.get(providerId);
    }
}
