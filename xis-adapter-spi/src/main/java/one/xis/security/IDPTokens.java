package one.xis.security;

import lombok.Data;

import java.time.Duration;

@Data
public class IDPTokens {
    private String accessToken;
    private String refreshToken;
    private Duration expiresIn;
    private Duration refreshTokenExpiresIn;
    private String state;
}
