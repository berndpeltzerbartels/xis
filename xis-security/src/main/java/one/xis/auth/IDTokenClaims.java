package one.xis.auth;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class IDTokenClaims implements TokenClaims {
    
    @SerializedName("aud")
    private String audience;

    @SerializedName("nonce")
    private String nonce;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("email_verified")
    private Boolean emailVerified;

    @SerializedName("preferred_username")
    private String preferredUsername;

    @SerializedName("given_name")
    private String givenName;

    @SerializedName("family_name")
    private String familyName;

    @SerializedName("locale")
    private String locale;

    @SerializedName("picture")
    private String pictureUrl;


}
