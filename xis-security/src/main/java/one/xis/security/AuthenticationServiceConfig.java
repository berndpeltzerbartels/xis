package one.xis.security;


import one.xis.context.XISBean;
import one.xis.context.XISComponent;
import one.xis.context.XISInject;
import one.xis.idp.IDPService;
import one.xis.ipdclient.IDPClientFactory;
import one.xis.server.LocalUrlHolder;

@XISComponent
class AuthenticationServiceConfig {

    @XISInject(optional = true)
    private AuthenticationConfig authenticationConfig;

    @XISInject(optional = true)
    private IDPService idpService;


    @XISBean
    AuthenticationService createLocalAuthenticationConfig(LocalUrlHolder localUrlHolder, IDPClientFactory idpClientFactory) {
        if (authenticationConfig != null) {
            return new AuthenticationServiceImpl(idpClientFactory, authenticationConfig, localUrlHolder);
        }
        if (idpService != null) {
            var config = new LocalAuthenticationConfig(idpService, localUrlHolder);
            return new AuthenticationServiceImpl(idpClientFactory, config, localUrlHolder);
        }
        return new NoopAuthenticationService();
    }
}
