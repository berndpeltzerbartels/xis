package one.xis.ipdclient;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Configuration class for OpenID Connect settings to be deserialized from a JSON file with gson.
 * It contains just those fields that are needed for the IDP client. Additional fields are ignored anf do not
 * throw an error.
 */
@Data
public class IDPWellKnownOpenIdConfig {
    private String issuer;

    /**
     * The URL of the OpenID Connect Provider's authorization endpoint.
     * This is the url of the html-form, where the client will redirect users to authenticate.
     */
    @SerializedName("authorization_endpoint")
    private String authorizationEndpoint;

    /**
     * The URL of the OpenID Connect Provider's token endpoint.
     * This is the url where the client will send the authorization code to exchange it for tokens.
     */
    @SerializedName("token_endpoint")
    private String tokenEndpoint;

    /**
     * The URL of the OpenID Connect Provider's userinfo endpoint.
     * This is the url where the client can retrieve user information after authentication.
     */
    @SerializedName("userinfo_endpoint")
    private String userInfoEndpoint;

    /**
     * The URL of the OpenID Connect Provider's JSON Web Key Set (JWKS) endpoint.
     * This is the url where the client can retrieve the public keys used to verify JWT signatures.
     */
    @SerializedName("jwks_uri")
    private String jwksUri;

    // Additional fields can be added here if needed, but they will be ignored during deserialization
}