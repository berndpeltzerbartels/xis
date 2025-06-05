package one.xis.security;

import lombok.Data;

@Data
public class LocalAuthenticationTokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresInSeconds;
    private String state;
}
