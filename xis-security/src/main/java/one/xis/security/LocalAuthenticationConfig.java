package one.xis.security;

import lombok.Data;
import one.xis.idp.IDPClientInfo;
import one.xis.idp.IDPService;
import one.xis.server.LocalUrlHolder;
import one.xis.server.UrlHolder;

import java.util.Set;

@Data
public class LocalAuthenticationConfig implements AuthenticationConfig {
    private final IDPService idpService;
    private final LocalUrlHolder localUrlHolder;
    private IDPClientInfo clientInfo;

    LocalAuthenticationConfig(IDPService idpService, LocalUrlHolder localUrlHolder) {
        this.idpService = idpService;
        this.localUrlHolder = localUrlHolder;
        this.localUrlHolder.addUrlAssignmentListener(this::createClientInfo);
    }

    @Override
    public UrlHolder getIdpUrl() {
        return localUrlHolder;
    }

    @Override
    public String getClientId() {
        return clientInfo.getClientId();
    }

    @Override
    public String getClientSecret() {
        return clientInfo.getClientSecret();
    }

    private void createClientInfo(String url) {
        // This method should interact with the IDPService to create a client info
        // based on the provided redirect URIs.
        this.clientInfo = idpService.createClientInfo(Set.of(url));
    }
}
