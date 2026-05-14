package one.xis.auth.ipdclient;


import one.xis.ImportInstances;

@ImportInstances
public interface IDPClientConfig {

    /**
     * Stable provider id used by XIS for login links and callback URLs.
     *
     * @return the provider id
     */
    String getIdpId();

    /**
     * Base URL of the OpenID Connect issuer. XIS reads the discovery document below this URL.
     *
     * @return the issuer base URL
     */
    String getIdpServerUrl();

    /**
     * Client id registered at the OpenID Connect provider.
     *
     * @return the client id
     */
    String getClientId();

    /**
     * Client secret registered at the OpenID Connect provider.
     *
     * @return the client secret
     */
    String getClientSecret();

    /**
     * Scopes requested during the authorization code flow.
     * <p>
     * The default is {@code openid}. Some providers require additional scopes for the claims an application needs. For
     * example, Keycloak role-protected applications commonly use {@code openid roles}; Google profile login commonly
     * uses {@code openid profile email}.
     *
     * @return the space-separated scope string
     */
    default String getScope() {
        return "openid";
    }

}
