package one.xis.auth.idp;


import one.xis.auth.ipdclient.IDPClient;
import one.xis.auth.ipdclient.IDPClientFactory;
import one.xis.context.XISComponent;
import one.xis.server.LocalUrlHolder;

import java.util.*;


/**
 * Service for managing authentication provider services.
 * This class initializes authentication provider services for each instance of
 * AuthenticationProviderConfiguration in context and provides access to these services.
 */

@XISComponent
public class ExternalIDPServices {

    private final List<ExternalIDPConfig> idpConfigs;
    private final IDPClientFactory idpClientFactory;
    private final LocalUrlHolder localUrlHolder;
    private final Map<String, ExternalIDPService> externalIDPServices = new HashMap<>();

    public ExternalIDPServices(Collection<ExternalIDPConfig> idpConfigs, IDPClientFactory idpClientFactory, LocalUrlHolder localUrlHolder) {
        this.idpConfigs = new ArrayList<>(idpConfigs);
        this.idpClientFactory = idpClientFactory;
        this.localUrlHolder = localUrlHolder;
    }

    public synchronized ExternalIDPService getServiceForIssuer(String issuer) {
        if (!externalIDPServices.containsKey(issuer)) {
            tryToLoadExternalIDPServices();
        }
        return externalIDPServices.get(issuer);
    }


    private void tryToLoadExternalIDPServices() {
        for (Iterator<ExternalIDPConfig> it = idpConfigs.iterator(); it.hasNext(); ) {
            ExternalIDPConfig config = it.next();
            try {
                IDPClient client = idpClientFactory.createConfiguredIDPClient(config, localUrlHolder.getUrl());
                it.remove();
                var service = new ExternalIDPServiceImpl(client, config, localUrlHolder);
                externalIDPServices.put(service.getIssuer(), service);
            } catch (Exception e) {
                // TODO log
            }
        }
    }

    /**
     * Returns the authentication provider service for the given provider ID.
     *
     * @param providerId the ID of the authentication provider
     * @return the authentication provider service
     */
    public ExternalIDPService getExternalIDPService(String providerId) {
        return externalIDPServices.get(providerId);
    }

    /**
     * Returns a collection of all available authentication provider services.
     *
     * @return a collection of authentication provider services
     */
    public Collection<ExternalIDPService> getExternalIDPServices() {
        return externalIDPServices.values();
    }

}
