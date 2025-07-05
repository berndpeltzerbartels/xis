package one.xis.idp;


import one.xis.ImportInstances;

@ImportInstances
public interface ExternalIDPConfig {
    String getCallbackUrl();

    String getClientId();

    String getClientSecret();

    String getIdpId();

    String getLoginFormUrl();

    String getTokenEndpoint();

    String getRenewTokenEndpoint();

    String getUserInfoEndpoint();

}
