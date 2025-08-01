package one.xis.auth.ipdclient;

/**
 * Factory interface for creating instances of IDPClientService.
 * This allows for the creation of both a local IDP client service and any other configured IDP client services.
 */
public interface IDPClientFactory {

    /**
     * Creates a configured IDP client service based on the provided configuration.
     *
     * @param localUrl the local URL of the server, used for constructing the IDP client.
     * @return an instance of IDPClientService configured with the provided settings.
     */
    IDPClient createConfiguredIDPClient(IDPClientConfig idpClientConfig, String localUrl) throws Exception;

}
