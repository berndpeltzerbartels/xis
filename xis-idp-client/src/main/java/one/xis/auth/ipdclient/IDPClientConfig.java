package one.xis.auth.ipdclient;


import one.xis.ImportInstances;

@ImportInstances
public interface IDPClientConfig {

    String getIdpId();

    String getIdpServerUrl();

    String getClientId();

    String getClientSecret();

}
