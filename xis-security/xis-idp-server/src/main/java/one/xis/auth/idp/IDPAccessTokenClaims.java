package one.xis.auth.idp;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import one.xis.auth.AccessTokenClaims;

@Data
@EqualsAndHashCode(callSuper = true)
public class IDPAccessTokenClaims extends AccessTokenClaims {

    @Delegate
    private final AccessTokenClaims accessTokenClaims;

    @SerializedName("iss")
    private String issuer;

    @SerializedName("sub")
    private String userId;

    @SerializedName("exp")
    private Long expiresAtSeconds;

    @SerializedName("iat")
    private Long issuedAt;

    @SerializedName("nbf")
    private Long notBefore;

    @SerializedName("client_id")
    private String clientId;

}
