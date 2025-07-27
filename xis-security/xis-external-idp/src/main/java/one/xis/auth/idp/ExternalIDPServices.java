package one.xis.auth.idp;


import lombok.RequiredArgsConstructor;
import one.xis.auth.ipdclient.IDPClientFactory;
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
    private final IDPClientFactory idpClientFactory;
    private final LocalUrlHolder localUrlHolder;
    private final Map<String, ExternalIDPService> externalIDPServiceMap = new HashMap<>();

    /**
     * Initializes the authentication provider services based on the provided configurations.
     */
    @XISInit
    public void initialize() {
        for (ExternalIDPConfig providerConfiguration : authenticationProviderConfigurations) {
            var idpClient = idpClientFactory.createConfiguredIDPClient(providerConfiguration, providerConfiguration.getIdpServerUrl());
            ExternalIDPService service = new ExternalIDPServiceImpl(idpClient, providerConfiguration, localUrlHolder);
            externalIDPServiceMap.put(service.getProviderId(), service);
        }
    }

    /**
     * Returns the authentication provider service for the given provider ID.
     *
     * @param providerId the ID of the authentication provider
     * @return the authentication provider service
     */
    public ExternalIDPService getExternalIDPService(String providerId) {
        return externalIDPServiceMap.get(providerId);
    }

    /**
     * Returns a collection of all available authentication provider services.
     *
     * @return a collection of authentication provider services
     */
    public Collection<ExternalIDPService> getExternalIDPServices() {
        return externalIDPServiceMap.values();
    }
}
