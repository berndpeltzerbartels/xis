package one.xis.server;

import lombok.Data;

import java.time.Instant;

@Data
public class BearerTokens {
    private String accessToken;
    private Instant accessTokenExpiresAt;
    private String renewToken;
}
