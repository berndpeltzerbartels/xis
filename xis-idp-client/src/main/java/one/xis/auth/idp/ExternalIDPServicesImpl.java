package one.xis.auth.idp;


import one.xis.auth.ipdclient.IDPClient;
import one.xis.auth.ipdclient.IDPClientFactory;
import one.xis.context.XISComponent;
import one.xis.server.LocalUrlHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Service for managing authentication provider services.
 * This class initializes authentication provider services for each instance of
 * AuthenticationProviderConfiguration in context and provides access to these services.
 */

@XISComponent
public class ExternalIDPServicesImpl implements ExternalIDPServices {

    private final List<ExternalIDPConfig> idpConfigs;
    private final IDPClientFactory idpClientFactory;
    private final LocalUrlHolder localUrlHolder;
    private final Map<String, ExternalIDPService> externalIDPServices = new ConcurrentHashMap<>();

    public ExternalIDPServicesImpl(Collection<ExternalIDPConfig> idpConfigs, IDPClientFactory idpClientFactory, LocalUrlHolder localUrlHolder) {
        this.idpConfigs = new CopyOnWriteArrayList<>(idpConfigs);
        this.idpClientFactory = idpClientFactory;
        this.localUrlHolder = localUrlHolder;
    }

    @Override
    public synchronized ExternalIDPService getServiceForIssuer(String issuer) {
        tryToLoadExternalIDPServices();
        return externalIDPServices.get(issuer);
    }


    private void tryToLoadExternalIDPServices() {
        for (var config : new ArrayList<>(idpConfigs)) {
            try {
                IDPClient client = idpClientFactory.createConfiguredIDPClient(config, localUrlHolder.getUrl());
                idpConfigs.remove(config);
                var service = new ExternalIDPServiceImpl(client, config, localUrlHolder);
                externalIDPServices.put(service.getIssuer(), service);
            } catch (Exception e) {
                // TODO log
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the authentication provider service for the given provider ID.
     *
     * @param issuer the provider ID (issuer) of the authentication provider
     * @return the authentication provider service
     */
    @Override
    public ExternalIDPService getExternalIDPService(String issuer) {
        tryToLoadExternalIDPServices();
        return externalIDPServices.get(issuer);
    }

    /**
     * Returns a collection of all available authentication provider services.
     *
     * @return a collection of authentication provider services
     */
    @Override
    public Collection<ExternalIDPService> getExternalIDPServices() {
        tryToLoadExternalIDPServices();
        return externalIDPServices.values();
    }

}
