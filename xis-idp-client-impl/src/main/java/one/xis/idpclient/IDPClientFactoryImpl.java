package one.xis.idpclient;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.http.HttpClientException;
import one.xis.http.RestClientFactory;
import one.xis.ipdclient.IDPClient;
import one.xis.ipdclient.IDPClientConfig;
import one.xis.ipdclient.IDPClientFactory;
import one.xis.ipdclient.LocalIDPClientConfig;
import one.xis.server.LocalUrlHolder;

@XISComponent
@RequiredArgsConstructor
class IDPClientFactoryImpl implements IDPClientFactory {

    private final RestClientFactory restClientFactory;
    private final LocalUrlHolder localUrlHolder;

    private static final String CALLBACK_PATH = "/xis/authentication/callback"; // TODO: Change it in all controllers

    @Override
    public IDPClient createLocalIDPClient() {
        var localIdpConfig = new LocalIDPClientConfig(localUrlHolder.getLocalUrl());
        try {
            return new IDPClientImpl(restClientFactory.createRestClient(localUrlHolder.getLocalUrl()), localIdpConfig, CALLBACK_PATH + "/local");
        } catch (HttpClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IDPClient createConfiguredIDPClient(IDPClientConfig idpClientConfig) {
        try {
            return new IDPClientImpl(restClientFactory.createRestClient(idpClientConfig.getServerUrl()), idpClientConfig, CALLBACK_PATH + "/" + idpClientConfig.getIdpId());
        } catch (HttpClientException e) {
            throw new RuntimeException(e);
        }
    }


}
