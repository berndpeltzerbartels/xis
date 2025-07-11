package one.xis.idp;


import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.server.LocalUrlHolder;

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
public class ExternalIDPServices {

    private final Collection<ExternalIDPConfig> authenticationProviderConfigurations;
    private final ExternalIDPConnectionFactory connectionFactory;
    private final LocalUrlHolder localUrlHolder;
    private final Gson gson;
    private final Map<String, ExternalIDPService> externalIDPServiceMap = new HashMap<>();

    /**
     * Initializes the authentication provider services based on the provided configurations.
     */
    @XISInit
    public void initialize() {
        for (ExternalIDPConfig providerConfiguration : authenticationProviderConfigurations) {
            ExternalIDPService service = new ExternalIDPServiceImpl(providerConfiguration, connectionFactory, localUrlHolder, gson);
            externalIDPServiceMap.put(service.getProviderId(), service);
        }
    }

    /**
     * Returns the authentication provider service for the given provider ID.
     *
     * @param providerId the ID of the authentication provider
     * @return the authentication provider service
     */
    public ExternalIDPService getAuthenticationProviderService(String providerId) {
        return externalIDPServiceMap.get(providerId);
    }
}
