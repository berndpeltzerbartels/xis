package one.xis.idp;

import lombok.Data;

import java.time.Duration;

@Data
public class LocalIDPTokens {
    private String accessToken;
    private String refreshToken;
    private Duration expiresIn;
    private Duration refreshTokenExpiresIn;
    private String state;
}
