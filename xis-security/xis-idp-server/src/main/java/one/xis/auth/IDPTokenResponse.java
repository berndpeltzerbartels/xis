package one.xis.auth;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class IDPTokenResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private long expiresIn;

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
