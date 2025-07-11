package one.xis.idp;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISDefaultComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.server.LocalUrlHolder;

import java.util.Collection;
import java.util.stream.Collectors;

@XISDefaultComponent
@RequiredArgsConstructor
class IDPConfig {
    private final IDPLoginController loginController;
    private final Collection<ExternalIDPConfig> externalIDPConfigs;
    private final ExternalIDPConnectionFactory connectionFactory;
    private final LocalUrlHolder localUrlHolder;

    @XISInject(optional = true)
    private IDPAuthenticationService authenticationService;

    @XISInit
    void init() {
        loginController.setIdpAuthenticationService(authenticationService);
    }

    @XISInit
    public void initializeExternalIDPs() {
        loginController.setExternalIDPServices(externalIDPConfigs.stream()
                .map(providerConfiguration -> new ExternalIDPServiceImpl(providerConfiguration, connectionFactory, localUrlHolder))
                .collect(Collectors.toList()));

    }


}
