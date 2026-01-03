package one.xis.auth.ipdclient;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.http.client.RestClientFactory;

@Component
@RequiredArgsConstructor
class IDPClientFactoryImpl implements IDPClientFactory {

    private final RestClientFactory restClientFactory;
    private final Gson gson;

    private static final String CALLBACK_PATH = "/xis/auth/callback"; // TODO: Change it in all controllers

    @Override
    public IDPClient createConfiguredIDPClient(IDPClientConfig idpClientConfig, String localUrl) throws Exception {
        var client = new IDPClientImpl(restClientFactory.createRestClient(), idpClientConfig, authenticationCallbackUrl(idpClientConfig.getIdpId(), localUrl), gson);
        client.loadOpenIdConfig();
        client.loadPublicKeys();
        return client;
    }

    private String authenticationCallbackUrl(String idpId, String localUrl) {
        return localUrl + CALLBACK_PATH + "/" + idpId;
    }
}
