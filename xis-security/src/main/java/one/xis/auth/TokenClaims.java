package one.xis.auth;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class TokenClaims {

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @SerializedName("sub")
    private String userId;

    /**
     * The issuer of the token, typically the authorization server's URL.
     * Is set by the authorization server when the token is issued.
     */
    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    @SerializedName("iss")
    private String issuer;


    /**
     * The expiration time of the token in seconds since the epoch.
     * Is set by the authorization server when the token is issued and has not to be set in implementations
     * of IDPService
     */
    @Setter(AccessLevel.PACKAGE)
    @Getter
    @SerializedName("exp")
    private Long expiresAtSeconds;

    /**
     * The time the token was issued in seconds since the epoch.
     * Is set by the authorization server when the token is issued and has not to be set in implementations
     * of IDPService
     */
    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    @SerializedName("iat")
    private Long issuedAtSeconds;

    /**
     * The time before which the token must not be accepted for processing, in seconds since the epoch.
     * Is set by the authorization server when the token is issued and has not to be set in implementations
     * of IDPService
     */
    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    @SerializedName("nbf")
    private Long notBeforeSeconds;

    /**
     * The client ID of the application that requested the token.
     * Is set by the authorization server when the token is issued and has not to be set in implementations
     * of IDPService
     */
    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    @SerializedName("client_id")
    private String clientId;

}
