package one.xis.auth.idp;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class IDPTokenRequest {

    @SerializedName("grant_type")
    private String grantType;
    @SerializedName("code")
    private String code;
    @SerializedName("redirect_uri")
    private String redirectUri;
    @SerializedName("client_id")
    private String clientId;
    @SerializedName("client_secret")
    private String clientSecret;
}
