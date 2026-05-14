package one.xis.auth;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Represents a single JSON Web Key (JWK) with readable field names.
 * The @SerializedName annotation maps these fields to the compact JSON property names.
 */
@Data
public class JsonWebKey {
    @SerializedName("kty")
    private String keyType;

    @SerializedName("use")
    private String publicKeyUse;

    @SerializedName("kid")
    private String keyId;

    @SerializedName("alg")
    private String algorithm;

    @SerializedName("n")
    private String rsaModulus;

    @SerializedName("e")
    private String rsaExponent;
}