package one.xis.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiTokens {
    private String accessToken;
    private Duration accessTokenExpiresIn;
    private String renewToken;
    private Duration renewTokenExpiresIn;
}
