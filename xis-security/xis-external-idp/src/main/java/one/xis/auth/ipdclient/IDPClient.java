package one.xis.auth.ipdclient;

import lombok.NonNull;
import one.xis.auth.*;

import java.util.Collection;

public interface IDPClient {
    /**
     * Tauscht einen Autorisierungscode gegen API-Tokens (Access- und Refresh-Token) aus.
     * Dies ist typischerweise der letzte Schritt im OIDC Authorization Code Flow.
     *
     * @param code Der Autorisierungscode, der vom IDP nach einer erfolgreichen Benutzeranmeldung empfangen wurde.
     * @return Die API-Tokens, die den Access- und Refresh-Token enthalten.
     * @throws AuthenticationException wenn der Code ungültig oder abgelaufen ist oder ein anderer Fehler beim Token-Austausch auftritt.
     */
    ApiTokens fetchNewTokens(@NonNull String code) throws AuthenticationException;

    /**
     * Erneuert die API-Tokens unter Verwendung eines Refresh-Tokens.
     * Dies ermöglicht den Erhalt eines neuen Access-Tokens, ohne dass sich der Benutzer erneut anmelden muss.
     *
     * @param refreshToken Der zuvor ausgestellte Refresh-Token.
     * @return Ein neuer Satz von API-Tokens.
     * @throws AuthenticationException wenn der Refresh-Token ungültig, widerrufen oder abgelaufen ist.
     */
    ApiTokens fetchRenewedTokens(@NonNull String refreshToken) throws AuthenticationException;

    /**
     * Ruft Benutzerinformationen vom UserInfo-Endpunkt des IDP unter Verwendung eines gültigen Access-Tokens ab.
     *
     * @param accessToken Der Access-Token zur Autorisierung der Anfrage.
     * @return Die Informationen des Benutzers.
     * @throws AuthenticationException wenn der Access-Token ungültig oder abgelaufen ist.
     */
    UserInfoImpl fetchUserInfo(@NonNull String accessToken) throws AuthenticationException;

    /**
     * Returns the IDP's unique identifier.
     *
     * @return the IDP identifier
     */
    String getIdpId();


    /**
     * Returns the URL of the IDP's authorization endpoint. This normally
     * is a login form URL.
     *
     * @return the authorization endpoint URL
     */
    String getAuthorizationEndpoint();

    /**
     * Returns the URL of the IDP's token endpoint, which is used to exchange
     * authorization codes for access tokens.
     *
     * @return the token endpoint URL
     */
    String getIssuer();

    IDPWellKnownOpenIdConfig getOpenIdConfig();

    void loadOpenIdConfig() throws Exception;

    void loadPublicKeys() throws AuthenticationException;

    Collection<JsonWebKey> getPublicKeys();
}
