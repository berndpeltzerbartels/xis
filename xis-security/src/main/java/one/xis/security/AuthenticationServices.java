package one.xis.security;


import lombok.NonNull;
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
public class AuthenticationServices {

    private final Collection<AuthenticationProviderConfiguration> authenticationProviderConfigurations;
    private final AuthenticationProviderConnectionFactory connectionFactory;
    private final Map<String, AuthenticationService> authenticationProviderServices = new HashMap<>();

    /**
     * Initializes the authentication provider services based on the provided configurations.
     */
    @XISInit
    public void initialize() {
        for (AuthenticationProviderConfiguration providerConfiguration : authenticationProviderConfigurations) {
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
    @NonNull
    public AuthenticationService getAuthenticationProviderService(String providerId) {
        var service = authenticationProviderServices.get(providerId);
        if (service == null) {
            throw new IllegalArgumentException("No authentication provider service found for provider ID: " + providerId);
        }
        return service;
    }
}
