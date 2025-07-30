package one.xis.auth.idp;


import lombok.RequiredArgsConstructor;
import one.xis.auth.JsonWebKey;
import one.xis.auth.JsonWebKeyProvider;
import one.xis.auth.ipdclient.IDPClientFactory;
import one.xis.context.XISComponent;
import one.xis.context.XISEventListener;
import one.xis.server.LocalUrlAssignedEvent;
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
public class ExternalIDPServices implements JsonWebKeyProvider {

    private final Collection<ExternalIDPConfig> authenticationProviderConfigurations;
    private final IDPClientFactory idpClientFactory;
    private final LocalUrlHolder localUrlHolder;
    private final Map<String, ExternalIDPService> externalIDPServiceMap = new HashMap<>();
    private final Map<String, Collection<JsonWebKey>> keysForIssuer = new HashMap<>();

    /**
     * Initializes the authentication provider services based on the provided configurations.
     */
    @XISEventListener
    public void initialize(LocalUrlAssignedEvent event) {
        for (ExternalIDPConfig providerConfiguration : authenticationProviderConfigurations) {
            var idpClient = idpClientFactory.createConfiguredIDPClient(providerConfiguration, event.localUrl());
            ExternalIDPService service = new ExternalIDPServiceImpl(idpClient, providerConfiguration, localUrlHolder);
            externalIDPServiceMap.put(service.getProviderId(), service);
            keysForIssuer.put(idpClient.getIssuer(), idpClient.fetchPublicKeys().getKeys());
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


    @Override
    public Map<String, Collection<JsonWebKey>> getKeysForIssuer(String issuer) {
        return keysForIssuer;
    }
}
