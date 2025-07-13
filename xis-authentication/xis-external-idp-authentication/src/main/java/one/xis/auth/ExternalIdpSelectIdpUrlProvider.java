package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.auth.idp.ExternalIDPService;
import one.xis.context.XISDefaultComponent;

import java.util.ArrayList;
import java.util.Collection;

@XISDefaultComponent
@RequiredArgsConstructor
class ExternalIdpSelectIdpUrlProvider implements LoginUrlProvider {

    static final String SELECT_IDP_URL = "/select-idp.html"; // TODO
    private final Collection<ExternalIDPService> externalIDPServices;
    private final ExternalIDPService externalIDPService;

    @Override
    public String loginUrl(String redirectUri) {
        return switch (externalIDPServices.size()) {
            case 0 ->
                    throw new IllegalStateException("No external IDP configured. You have to provide at least one instance of ExternalIDPConfig.");
            case 1 -> new ArrayList<>(externalIDPServices).get(0).createLoginUrl(redirectUri);
            default -> SELECT_IDP_URL;
        };
    }
}
