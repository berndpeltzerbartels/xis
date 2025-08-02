package one.xis.auth.idp;

import lombok.NonNull;
import one.xis.auth.JsonWebKey;


// TODO Die interfaces sind für den User nicht sinnvoll zusammengefasst.
// TODO Es sollte eine Interface für den LocalAuthenticationProvider geben und eines für Authentication
public interface ExternalIDPService {


    /**
     * Creates an authorization URL for the authentication provider.
     * This URL is used to redirect users to the provider's login page.
     *
     * @param postLoginRedirectUrl The URL to redirect to after the user has logged in.
     * @return The authorization URL as a String.
     */
    String createLoginUrl(String postLoginRedirectUrl);

    /**
     * Requests tokens from the authentication provider using the provided authorization code.
     * This method exchanges the authorization code for access and refresh tokens.
     *
     * @param code The authorization code received from the authentication provider.
     * @return An instance of {@link ExternalIDPTokens} containing the tokens.
     */
    ExternalIDPTokens fetchTokens(@NonNull String code);

    ExternalIDPTokens fetchRenewedTokens(@NonNull String refreshToken);

    String createStateParameter(String urlAfterLogin);

    String getProviderId();

    String getIssuer();

    JsonWebKey getJsonWebKey(String kid);
}
