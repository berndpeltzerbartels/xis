package one.xis.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@AllArgsConstructor
@NoArgsConstructor
// TODO check if this is the right place for this class
public class ApiTokens {
    private String accessToken;
    private Duration accessTokenExpiresIn;
    private String renewToken;
    private Duration renewTokenExpiresIn;
}
