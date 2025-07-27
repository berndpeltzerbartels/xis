package one.xis.auth.idp;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import one.xis.auth.IDTokenClaims;

@Data
@EqualsAndHashCode(callSuper = true)
public class IDPIDTokenClaims extends IDTokenClaims {

    @Delegate
    private final IDTokenClaims idTokenClaims;

    @SerializedName("iss")
    private String issuer;

    @SerializedName("sub")
    private String userId;

    @SerializedName("exp")
    private Long expiresAt;

    @SerializedName("iat")
    private Long issuedAt;

    @SerializedName("auth_time")
    private Long authenticationTime;
}
