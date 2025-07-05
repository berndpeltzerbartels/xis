package one.xis.idp;

import one.xis.auth.token.ApiTokensAndUrl;

/**
 * Binds controller endpoints of a framework to the IDP service. These are the endpoints in case
 * Xis is used as an iDP (Identity Provider) for authentication.
 */
public interface IDPFrontendService {

    public static final String CALLBACK_URL = "/xis/auth/callback";

    ApiTokensAndUrl authenticationCallback(String provider, String queryString);

    String createLoginFormUrl(String provider, String redirectUri);

}
