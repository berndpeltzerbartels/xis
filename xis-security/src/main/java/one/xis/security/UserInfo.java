package one.xis.security;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * Repräsentiert Benutzerinformationen, die typischerweise aus einem JWT dekodiert
 * oder vom User-Info-Endpunkt eines IDP abgerufen werden.
 * Diese Klasse enthält Standard-OIDC-Claims sowie eine Map für alle benutzerdefinierten Claims.
 */
@Data
@Builder
public class UserInfo {

    /**
     * Die eindeutige Kennung des Benutzers (Subject). Entspricht dem 'sub'-Claim.
     */
    private String userId;

    /**
     * Der vollständige Name des Benutzers. Entspricht dem 'name'-Claim.
     */
    private String name;

    /**
     * Der Vorname des Benutzers. Entspricht dem 'given_name'-Claim.
     */
    private String givenName;

    /**
     * Der Nachname des Benutzers. Entspricht dem 'family_name'-Claim.
     */
    private String familyName;

    /**
     * Die bevorzugte E-Mail-Adresse des Benutzers. Entspricht dem 'email'-Claim.
     */
    private String email;

    /**
     * Gibt an, ob die E-Mail-Adresse des Benutzers verifiziert wurde. Entspricht dem 'email_verified'-Claim.
     */
    private Boolean emailVerified;

    /**
     * Die dem Benutzer zugewiesenen Rollen (z.B. 'admin', 'user').
     * Extrahiert aus benutzerdefinierten Claims wie 'roles', 'groups' oder Keycloaks 'realm_access'.
     */
    private Set<String> roles;

    /**
     * Eine Map, die alle Claims aus dem Token enthält.
     * Nützlich für den Zugriff auf benutzerdefinierte oder nicht standardisierte Claims des IDP.
     */
    private Map<String, Object> claims;
}