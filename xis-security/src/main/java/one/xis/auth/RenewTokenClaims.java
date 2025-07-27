package one.xis.auth;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class RenewTokenClaims implements TokenClaims {

    @SerializedName("sub")
    private final String userId;

    @SerializedName("exp")
    private final Long expiresAtSeconds;

}
