package one.xis.auth.ipdclient;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.http.client.HttpClientException;
import one.xis.http.client.RestClientFactory;

@XISComponent
@RequiredArgsConstructor
class IDPClientFactoryImpl implements IDPClientFactory {

    private final RestClientFactory restClientFactory;
    private final Gson gson;

    private static final String CALLBACK_PATH = "/xis/auth/callback"; // TODO: Change it in all controllers

    @Override
    public IDPClient createConfiguredIDPClient(IDPClientConfig idpClientConfig, String url) {
        try {
            return new IDPClientImpl(restClientFactory.createRestClient(idpClientConfig.getIdpServerUrl()), idpClientConfig, authenticationCallbackUrl(idpClientConfig.getIdpId(), url), gson);
        } catch (HttpClientException e) {
            throw new RuntimeException(e);
        }
    }

    private String authenticationCallbackUrl(String idpId, String localUrl) {
        return localUrl + CALLBACK_PATH + "/" + idpId;
    }
}
