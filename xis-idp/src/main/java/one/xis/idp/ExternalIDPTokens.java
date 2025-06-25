package one.xis.idp;

import lombok.Data;

import java.time.Duration;

@Data
public class ExternalIDPTokens {
    private String accessToken;
    private Duration expiresIn;
    private String refreshToken;
    private String scope;
    private String tokenType;
    private String idToken;
    private Duration refreshExpiresIn;
}
