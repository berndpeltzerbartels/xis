package one.xis.security;

import lombok.Data;

import java.time.Duration;

@Data
public class AuthenticationProviderTokenResponse {
    private String accessToken;
    private Duration expiresInSeconds;
    private String refreshToken;
    private String scope;
    private String tokenType;
    private String idToken;
    private Duration refreshExpiresIn;
}
