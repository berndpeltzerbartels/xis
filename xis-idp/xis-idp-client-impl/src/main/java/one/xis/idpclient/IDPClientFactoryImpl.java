package one.xis.idpclient;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.http.HttpClientException;
import one.xis.http.RestClientFactory;
import one.xis.ipdclient.IDPClient;
import one.xis.ipdclient.IDPClientConfig;
import one.xis.ipdclient.IDPClientFactory;
import one.xis.server.LocalUrlHolder;

@XISComponent
@RequiredArgsConstructor
class IDPClientFactoryImpl implements IDPClientFactory {

    private final RestClientFactory restClientFactory;
    private final LocalUrlHolder localUrlHolder;
    private final Gson gson;

    private static final String CALLBACK_PATH = "/xis/auth/callback"; // TODO: Change it in all controllers

    @Override
    public IDPClient createConfiguredIDPClient(IDPClientConfig idpClientConfig) {
        try {
            return new IDPClientImpl(restClientFactory.createRestClient(idpClientConfig.getIdpServerUrl()), idpClientConfig, authenticationCallbackUrl(idpClientConfig.getIdpId()), gson);
        } catch (HttpClientException e) {
            throw new RuntimeException(e);
        }
    }

    private String authenticationCallbackUrl(String idpId) {
        return localUrlHolder.getUrl() + CALLBACK_PATH + "/" + idpId;
    }
}
