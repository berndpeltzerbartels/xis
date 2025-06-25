package one.xis.ipdclient;

/**
 * Factory interface for creating instances of IDPClientService.
 * This allows for the creation of both a local IDP client service and any other configured IDP client services.
 */
public interface IDPClientFactory {

    /**
     * Creates a local IDP client service.
     *
     * @return an instance of IDPClientService for the local IDP client.
     */
    IDPClient createLocalIDPClient();


    /**
     * Creates a configured IDP client service based on the provided configuration.
     *
     * @param idpClientConfig the configuration for the IDP client.
     * @return an instance of IDPClientService configured with the provided settings.
     */
    IDPClient createConfiguredIDPClient(IDPClientConfig idpClientConfig);
}
