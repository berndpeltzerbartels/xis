package one.xis.auth.idp;

import java.util.Collection;

public interface ExternalIDPServices {
    ExternalIDPService getServiceForIssuer(String issuer);

    ExternalIDPService getExternalIDPService(String issuer);

    Collection<ExternalIDPService> getExternalIDPServices();
}
