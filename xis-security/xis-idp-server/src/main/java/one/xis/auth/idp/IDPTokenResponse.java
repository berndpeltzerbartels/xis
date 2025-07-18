package one.xis.auth.idp;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
class IDPTokenResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private long expiresIn; // Geändert zu long

    @SerializedName("refresh_token")
    private String refreshToken;

    private String scope;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("id_token")
    private String idToken;

    @SerializedName("refresh_expires_in")
    private long refreshExpiresIn; // Geändert zu long
}
