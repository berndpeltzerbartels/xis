package one.xis.security;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AuthenticationProviderTokenResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private int expiresInSeconds;

    @SerializedName("refresh_token")
    private String refreshToken;
    private String scope;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("id_token")
    private String idToken;

}
