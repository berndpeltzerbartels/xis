package one.xis.idp;

public class LocalIDPConfig extends AuthenticationProviderConfig {

    public LocalIDPConfig(String idpServerUrl, String applicationRootUrl) {
        this.setAuthenticationProviderId("xis-authentication-provider-idp");
        this.setApplicationRootEndpoint(applicationRootUrl);
        this.setClientId("xis-authentication-client-idp"); // TODO: is ignored in IDP
        this.setClientSecret("secret"); // TODO: is ignored in IDP
        this.setTokenEndpoint(idpServerUrl + "/xis/idp/tokens");
        this.setRenewTokenEndpoint(idpServerUrl + "/xis/idp/renew"); // TODO: implement
        this.setUserInfoEndpoint(idpServerUrl + "/xis/idp/userinfo"); // TODO: implement
        this.setLoginFormUrl(idpServerUrl + "/idp/login.html");
    }
}
