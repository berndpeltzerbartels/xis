package one.xis.server;

import lombok.Data;

import java.time.Duration;

@Data
public class BearerTokens {
    private String accessToken;
    private Duration accessTokenExpiresIn;
    private String renewToken;
    private Duration renewTokenExpiresIn;
}
