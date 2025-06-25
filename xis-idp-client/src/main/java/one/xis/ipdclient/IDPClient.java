package one.xis.ipdclient;

import lombok.NonNull;
import one.xis.security.AuthenticationException;
import one.xis.security.UserInfo;
import one.xis.server.ApiTokens;

public interface IDPClient {
    /**
     * Tauscht einen Autorisierungscode gegen API-Tokens (Access- und Refresh-Token) aus.
     * Dies ist typischerweise der letzte Schritt im OIDC Authorization Code Flow.
     *
     * @param code Der Autorisierungscode, der vom IDP nach einer erfolgreichen Benutzeranmeldung empfangen wurde.
     * @return Die API-Tokens, die den Access- und Refresh-Token enthalten.
     * @throws AuthenticationException wenn der Code ungültig oder abgelaufen ist oder ein anderer Fehler beim Token-Austausch auftritt.
     */
    ApiTokens requestTokens(@NonNull String code) throws AuthenticationException;

    /**
     * Erneuert die API-Tokens unter Verwendung eines Refresh-Tokens.
     * Dies ermöglicht den Erhalt eines neuen Access-Tokens, ohne dass sich der Benutzer erneut anmelden muss.
     *
     * @param refreshToken Der zuvor ausgestellte Refresh-Token.
     * @return Ein neuer Satz von API-Tokens.
     * @throws AuthenticationException wenn der Refresh-Token ungültig, widerrufen oder abgelaufen ist.
     */
    ApiTokens renewTokens(@NonNull String refreshToken) throws AuthenticationException;

    /**
     * Ruft Benutzerinformationen vom UserInfo-Endpunkt des IDP unter Verwendung eines gültigen Access-Tokens ab.
     *
     * @param accessToken Der Access-Token zur Autorisierung der Anfrage.
     * @return Die Informationen des Benutzers.
     * @throws AuthenticationException wenn der Access-Token ungültig oder abgelaufen ist.
     */
    UserInfo getUserInfo(@NonNull String accessToken) throws AuthenticationException;

    /**
     * Holt die öffentlichen Schlüssel vom IDP, typischerweise von einem JWKS (JSON Web Key Set) Endpunkt.
     * Diese Schlüssel werden verwendet, um die Signatur von JWTs zu überprüfen, die vom IDP ausgestellt wurden.
     *
     * @return Ein Objekt, das die öffentlichen Schlüssel enthält.
     * @throws AuthenticationException wenn die Schlüssel nicht abgerufen werden können.
     */
    IDPPublicKeyResponse getPublicKeys() throws AuthenticationException;

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
}
